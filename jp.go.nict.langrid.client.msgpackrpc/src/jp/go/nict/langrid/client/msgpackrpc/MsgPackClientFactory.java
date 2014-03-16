/*
* Copyright (C) 2014 Information Analysis Laboratory, NICT
*
* RaSC is free software: you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 2.1 of the License, or (at
* your option) any later version.
*
* RaSC is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
* General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * This is a program for Language Grid Core Node. This combines multiple language resources and provides composite language services.
 * Copyright (C) 2005-2012 NICT Language Grid Project.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package jp.go.nict.langrid.client.msgpackrpc;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.URL;

import jp.go.nict.langrid.client.ClientFactory;
import jp.go.nict.langrid.commons.rpc.ArrayElementsNotifier;
import jp.go.nict.langrid.commons.rpc.ArrayElementsReceiver;

import org.msgpack.rpc.ClientEx;
import org.msgpack.rpc.loop.EventLoop;
import org.msgpack.rpc.loop.netty.NettyEventLoopFactoryEx;
import org.msgpack.type.Value;

/**
 * MsgPackClientFactoryクラス.<br>
 * MsgPack用のクライアントファクトリー実装クラス.<br>
 * 
 * @author kishimoto
 *
 */
public class MsgPackClientFactory implements ClientFactory, AutoCloseable{

	/**
	 * コンストラクタ
	 */
	public MsgPackClientFactory(){
		this.timeout = 300;
	}

	/**
	 * コンストラクタ
	 * @param timeout タイムアウト値
	 */
	public MsgPackClientFactory(int timeout){
		this.timeout = timeout;
	}

	/**
	 * サービス呼び出し用,InvocationHandler実装クラス.
	 * @author kishimoto
	 *
	 */
	private class MsgPackRpcInvocationHandler implements InvocationHandler, ArrayElementsNotifier{

		/* (非 Javadoc)
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Class<?> clz = method.getDeclaringClass();
			if (clz.equals(ArrayElementsNotifier.class)) {
				return method.invoke(this, args);
			} else {
				resultType = method.getReturnType();

				if (resultType.isArray()) {
					resultType = resultType.getComponentType();
				}

				return method.invoke(clientInvoker, args);
			}
		}

		/* (非 Javadoc)
		 * @see jp.go.nict.langrid.commons.rpc.ArrayElementsNotifier#setReceiver(jp.go.nict.langrid.commons.rpc.ArrayElementsReceiver)
		 */
		@Override
		public void setReceiver(ArrayElementsReceiver receiver) {
			rcv = receiver;
		}
	}

	/**
	 * Msgpack用のサービスを生成する.
	 * 
	 * @param interfaceClass サービスクラス
	 * @param address msgpackサーバのアドレス
	 * @return サービスクラス
	 */
	public <T> T create(Class<T> interfaceClass, InetSocketAddress address) {
		ClientEx client = getClient(address);
		return client.proxy(interfaceClass);
	}

	//	@Override
	//	public <T> T create(Class<T> interfaceClass, URL url) {
	//		ClientEx client = getClient(new InetSocketAddress(url.getHost(), url.getPort()));
	//		return client.proxy(interfaceClass);
	//	}

	/* (非 Javadoc)
	 * @see jp.go.nict.langrid.client.ClientFactory#create(java.lang.Class<T>, java.net.URL, java.lang.String, java.lang.String)
	 */
	@Override
	public <T> T create(Class<T> interfaceClass, URL url, String userId, String password) {
		return create(interfaceClass, url);
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.langrid.client.ClientFactory#create(java.lang.Class<T>, java.net.URL)
	 */
	@Override
	public <T> T create(Class<T> interfaceClass, URL url) {
		ClientEx client = getClient(new InetSocketAddress(url.getHost(), url.getPort()));
		clientInvoker = client.proxy(interfaceClass);

		return interfaceClass.cast(Proxy.newProxyInstance(
			Thread.currentThread().getContextClassLoader()
			, new Class<?>[] {
				interfaceClass, ArrayElementsNotifier.class }
			, new MsgPackRpcInvocationHandler()
			));
	}

	/**
	 * msgpackクライアントを取得
	 * @param address サーバアドレス
	 * @return msgpackクライアント
	 */
	private synchronized ClientEx getClient(InetSocketAddress address) {
		if (client != null)
			return client;
		EventLoop.setFactory(new NettyEventLoopFactoryEx());
		client = new ClientEx(address) {

			@Override
			public void onResponseData(int msgid, Value result, Value error) {
				if (rcv != null) {
					Object recv = result;
					/*
					 * Note: MsgpackはValue型なので、サービス連携に持っていく時は、
					 * キャストが必要かも・・・・入れるならココらへんで！
					 * */
					if (resultType != null) {
						if (client != null) {
							try {
								/* Register済みのクラス情報から変換する */
								recv = client.getEventLoop().getMessagePack().convert(result, resultType);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					rcv.receive(recv);
				}
			}

		};//変更
		client.setRequestTimeout(timeout);

		return client;
	}

	//Method 追加
	/**
	 * msgpackの切断処理
	 */
	public void shutdown() {
		if (client != null) {
			client.close();
			client.getEventLoop().shutdown();
			try {
				client.getEventLoop().join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			EventLoop.setDefaultEventLoop(null);
		}
	}

	/* (非 Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		shutdown();
	}

	private int timeout = 300;
	private ClientEx client;
	private ArrayElementsReceiver rcv = null;
	private Object clientInvoker = null;
	private Class<?> resultType = null;

}

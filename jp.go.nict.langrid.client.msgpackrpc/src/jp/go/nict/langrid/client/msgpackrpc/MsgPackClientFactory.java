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

import java.io.Closeable;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.URL;

import jp.go.nict.langrid.client.ClientFactory;
import jp.go.nict.langrid.commons.rpc.ArrayElementsNotifier;

/**
 * MsgPackClientFactoryクラス.<br>
 * MsgPack用のクライアントファクトリー実装クラス.<br>
 * 
 * @author kishimoto
 *
 */
public class MsgPackClientFactory implements ClientFactory {

	private int timeout = 300;

	/**
	 * コンストラクタ
	 */
	public MsgPackClientFactory() {
		this.timeout = 300;
	}

	/**
	 * コンストラクタ
	 * @param timeout タイムアウト値
	 */
	public MsgPackClientFactory(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * 通信を切断する.
	 */
	@Deprecated
	public void close() {
		shutdown();
	}

	/**
	 * Msgpack用のサービスを生成する.
	 * 
	 * @param interfaceClass サービスクラス
	 * @param address msgpackサーバのアドレス
	 * @return サービスクラス
	 */
	public <T> T create(Class<T> interfaceClass, InetSocketAddress address) {
		return interfaceClass.cast(Proxy.newProxyInstance(
				Thread.currentThread().getContextClassLoader(), new Class<?>[]
				{
						interfaceClass, ArrayElementsNotifier.class, Closeable.class
				},
				new MsgPackRpcInvocationHandler<T>(address, timeout, interfaceClass)
				));
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.langrid.client.ClientFactory#create(java.lang.Class<T>, java.net.URL)
	 */
	@Override
	public <T> T create(Class<T> interfaceClass, URL url) {
		return create(interfaceClass, new InetSocketAddress(url.getHost(), url.getPort()));

	}

	/* (非 Javadoc)
	 * @see jp.go.nict.langrid.client.ClientFactory#create(java.lang.Class<T>, java.net.URL, java.lang.String, java.lang.String)
	 */
	@Override
	public <T> T create(Class<T> interfaceClass, URL url, String userId, String password) {
		return create(interfaceClass, url);
	}

	
	/**
	 * 終了処理.
	 */
	@Deprecated
	public void shutdown() {
		MsgPackClientPool.getInstance().shutdownAllClient();
	}

}

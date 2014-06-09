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

package jp.go.nict.langrid.client.msgpackrpc;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import jp.go.nict.langrid.commons.rpc.ArrayElementsNotifier;
import jp.go.nict.langrid.commons.rpc.ArrayElementsReceiver;

import org.msgpack.MessagePack;
import org.msgpack.rpc.ClientEx;
import org.msgpack.rpc.ResponseDataListener;
import org.msgpack.type.Value;

/**
 * サービス呼び出し用,InvocationHandler実装クラス.
 * @author kishimoto
 *
 */
class MsgPackRpcInvocationHandler<T> implements InvocationHandler, Closeable, ArrayElementsNotifier {

	private Object proxyInvocation = null;
	private ClientEx client = null;
	private Class<T> interfaceClass = null;
	private InetSocketAddress address = null;
	private final ThreadLocal<ArrayElementsReceiver<Object>> rcv = new ThreadLocal<>();

	public MsgPackRpcInvocationHandler() {
	}

	public MsgPackRpcInvocationHandler(Class<T> clsz) {
		this();
		interfaceClass = clsz;

	}

	public MsgPackRpcInvocationHandler(InetSocketAddress address, Class<T> clsz) {
		this(address, 300, clsz);//default time out is 300 sec
	}

	public MsgPackRpcInvocationHandler(InetSocketAddress address, int timeOut, Class<T> clsz) {
		this(clsz);//default
		this.address = address;
		client = MsgPackClientPool.getInstance().getClient(address, timeOut);
		proxyInvocation = client.proxy(clsz);
	}

	/* (非 Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		Class<?> clz = method.getDeclaringClass();

		//RPC-ServiceのInterface
		if (clz.equals(interfaceClass)) {
			final Class<?> resultType = method.getReturnType();
			final Class<?> convertType = (resultType.isArray()) ? resultType.getComponentType() : resultType;

			//ResultType is Array
			if (resultType.isArray()) {
				final MessagePack mp = client.getEventLoop().getMessagePack(); //result desrialize
				
				/* Streaming over msgpack-rpc enabled.*/
				if (rcv.get() != null) {
					// receiverが登録されている場合は、それを利用する。
					final ArrayElementsReceiver<Object> receiver = rcv.get();

					client.AddListener(method.getName(), new ResponseDataListener() {
						@Override
						public void onResponseData(int msgid, Value result, Value error) {
							Object recv = null;
							try {
								recv = mp.convert(result, convertType);
							} catch (IOException e) {
								e.printStackTrace();
							}
							receiver.receive(recv);
						}
					});
					//invoke
					return method.invoke(proxyInvocation, args);
				} else {
					//receiverが無い場合には、デフォルトを用意して、格納しておく
					final List<Object> defList = new ArrayList<>();
					final List<Value> resValue = new ArrayList<>();

					client.AddListener(method.getName(), new ResponseDataListener() {
						@Override
						public void onResponseData(int msgid, Value result, Value error) {
							resValue.add(result);
							
//							Object recv = null;
//							
//							try {
////								recv = mp.convert(result, convertType);
////								defList.add(recv);
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
						}
					});

					//invoke
					Object result = method.invoke(proxyInvocation, args);
					int nStreamCnt = resValue.size();
					int nResultCnt = 0;
					
					if (result.getClass().equals(resultType)) {
						if (result.getClass().getComponentType().equals(convertType)) {
							nResultCnt = Array.getLength(result);
						}
					}
					//詰め直し
					int maxSize = nStreamCnt + nResultCnt;
					int index = 0;
					Object arrObj = Array.newInstance(convertType, maxSize);
					
					//Streaming
					for(Value v:resValue){
						Array.set(arrObj, index++, mp.convert(v, convertType));
					}
					
					//result
					System.arraycopy(result, 0, arrObj, index, nResultCnt);
					return arrObj;
				}

			} else {
				return method.invoke(proxyInvocation, args);
			}
		} else {
			return method.invoke(this, args);
		}
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.langrid.commons.rpc.ArrayElementsNotifier#setReceiver(jp.go.nict.langrid.commons.rpc.ArrayElementsReceiver)
	 */
	@SuppressWarnings("unchecked")
	public void setReceiver(@SuppressWarnings("rawtypes") ArrayElementsReceiver receiver) {
		rcv.remove();
		rcv.set(receiver);
	}

	@Override
	public void close() throws IOException {
		if (client != null) {
			MsgPackClientPool.getInstance().close(client);
			client = null;
		}
	}

}

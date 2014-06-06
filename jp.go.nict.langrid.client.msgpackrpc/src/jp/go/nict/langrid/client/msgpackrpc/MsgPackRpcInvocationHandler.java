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

import org.msgpack.rpc.ClientEx;
import org.msgpack.rpc.ResponseDataListener;
import org.msgpack.type.Value;

/**
 * サービス呼び出し用,InvocationHandler実装クラス.
 * @author kishimoto
 *
 */
class MsgPackRpcInvocationHandler<T> implements InvocationHandler, Closeable, ArrayElementsNotifier {

	//	private Class<?> resultType = null;
	//	private Class<?> primitiveResultType = null;
	private Object proxyInvocation = null;
	//	private ArrayElementsReceiver<Object> rcv = null;
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
		//		client = new ClientEx(address, new NettyEventLoopEx(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), Executors.newScheduledThreadPool(2), new MessagePack()));
		client = MsgPackClientPool.getInstance().getClient(address, timeOut);
		proxyInvocation = client.proxy(clsz);
		//		client.setRequestTimeout(timeOut);
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
				/* Streaming over msgpack-rpc enabled.*/

				if (rcv.get() != null) {
					// receiverが登録されている場合は、それを利用する。
					final ArrayElementsReceiver<Object> receiver = rcv.get();

					client.AddListener(method.getName(), new ResponseDataListener() {
						@Override
						public void onResponseData(int msgid, Value result, Value error) {
							Object recv = null;
							try {
								recv = client.getEventLoop().getMessagePack().convert(result, convertType);
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
					
					client.AddListener(method.getName(), new ResponseDataListener() {
						@Override
						public void onResponseData(int msgid, Value result, Value error) {
							Object recv = null;
							try {
								recv = client.getEventLoop().getMessagePack().convert(result, convertType);
								defList.add(recv);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				
					//invoke
					Object result = method.invoke(proxyInvocation, args);
					if(result.getClass().equals(resultType)){
						if(result.getClass().getComponentType().equals(convertType)){
							Object[] o = (Object[])result;
							for(Object in:o){
								defList.add(in);
							}
						}
					}
					//詰め直し
					int maxSize = defList.size();
					Object arrObj = Array.newInstance(convertType, defList.size());
					for(int i = 0; i < maxSize ;i++){
						Array.set(arrObj, i, convertType.cast(defList.get(i)));
					}
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
			//			client.close();
			//			client.getEventLoop().shutdown();
			//			try {
			//				client.getEventLoop().join();
			//			} catch (InterruptedException e) {
			//				e.printStackTrace();
			//			}
			MsgPackClientPool.getInstance().close(client);
			client = null;
		}
	}

}

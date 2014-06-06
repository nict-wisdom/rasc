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

package jp.go.nict.ial.webapps.wisdom.proxy;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jp.go.nict.isp.wisdom2013.api.balancer.EndpointFactory;
import jp.go.nict.langrid.client.ClientFactory;
import jp.go.nict.langrid.client.msgpackrpc.MsgPackClientFactory;
import jp.go.nict.langrid.commons.rpc.ArrayElementsNotifier;
import jp.go.nict.langrid.commons.rpc.ArrayElementsReceiver;
import jp.go.nict.langrid.servicecontainer.executor.StreamingNotifier;
import jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver;
import jp.go.nict.langrid.servicecontainer.handler.RIProcessor;

/**
 * Proxyサービスの実装クラス.<br>
 * Webアプリケーションとして起動し、サービスXMLに記載したサービスを呼び出す。<br>
 * また、負荷分散も実施する
 * 
 * @author kishimoto
 * @see jp.go.nict.isp.wisdom2013.api.balancer.EndpointFactory
 * 
 */
public class ProxyService implements InvocationHandler, ProxyServiceName, StreamingNotifier<Object> {

//	private static Map<URL, Object> msgpackServices = null;

	private ClientFactory clientFactory = null;

	private EndpointFactory endpointFactory = null;

	private String msgpackPort = null;

	private String serviceMapping = null;

	private String serviceName = null;

	private StreamingReceiver<Object> sr = null;

	/**
	 * コンストラクタ
	 */
	public ProxyService() {
//		checkStart();

	}

	/**
	 * MsgPackサービス初期化処理
	 */
//	protected synchronized void checkStart() {
//		if (msgpackServices == null) {
//			msgpackServices = new ConcurrentHashMap<URL, Object>();
//		}
//	}

	/**
	 * ClientFactoryを取得する
	 * @return ClientFactor
	 */
	public ClientFactory getClientFactory() {
		return clientFactory;
	}

	/**
	 * EndpointFactoryを取得する
	 * @return EndpointFactory,nullの場合には、設定なし。
	 */
	public EndpointFactory getEndpointFactory() {
		return endpointFactory;
	}

	/**
	 * MsgPackサービスのポート番号を取得する
	 * @return Msgpack用ポート番号
	 */
	public String getMsgpackPort() {
		return msgpackPort;
	}

	/**
	 * サービス呼び出し用のマッピング情報を取得する
	 * @return マッピング情報
	 */
	public String getServiceMapping() {
		return serviceMapping;
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.ial.webapps.wisdom.proxy.ProxyServiceName#getServiceName()
	 */
	@Override
	public String getServiceName() {

		return this.serviceName;
	}

	/* (非 Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		if ((clientFactory == null) || (endpointFactory == null)) {
			return null;
		}

		/* WEB-INF のパス*/
		endpointFactory.setRealPath(RIProcessor.getCurrentServiceContext().getRealPath("/WEB-INF/"));

		Class<?> service = method.getDeclaringClass();

		if (serviceMapping == null) {
			return null;
		}

		List<String> list = endpointFactory.create(new ArrayList<String>(), getServiceName());

		if (list.size() <= 0) {
			return null;
		}

		String endpoint = list.get(0) + serviceMapping;
		System.out.println(endpoint);
		URL url = new URL(endpoint);

		Object ob = null;
		final StreamingReceiver<Object> fsr = sr;

	//	if (clientFactory.getClass().equals(MsgPackClientFactory.class)) {
	//		url = new URL(String.format("http://%s:%s/", url.getHost(), getMsgpackPort()));
	//		synchronized (msgpackServices) {
	//			if (msgpackServices.containsKey(url)) {
	//				ob = msgpackServices.get(url);
	//			} else {
					ob = new MsgPackClientFactory().create(service, url);
	//				msgpackServices.put(url, ob);
	//			}
	//		}
	//	} else {
			ob = clientFactory.create(service, url);
	//	}

		final Class<?> resutType = method.getReturnType();
		final Queue<Object> que = new ConcurrentLinkedQueue<Object>();
		boolean isStreaming = false;

		if (ob instanceof ArrayElementsNotifier) {
			isStreaming = true;
			((ArrayElementsNotifier) ob).setReceiver(new ArrayElementsReceiver() {
				@Override
				public void receive(Object result) {
					if (fsr != null) {
						fsr.receive(result);
					} else {
						que.add(resutType.getComponentType().cast(result));
					}

				}
			});
		}

		Object o = null;

		if (isStreaming) {
			Object r = method.invoke(ob, args);
			
			if(r != null){
				if((r.getClass().isArray()) && (r.getClass().equals(resutType))){
					Object[] arr = (Object[])r;
					for(Object a :arr){
						que.add(resutType.getComponentType().cast(a));
					}
				}
			}
			if (resutType.isArray()) {
				int size = que.size();
				if (size != 0) {
					o = Array.newInstance(resutType.getComponentType(), size);
					System.arraycopy(que.toArray(), 0, o, 0, size);
				}
			}
		} else {
			o = method.invoke(ob, args);
		}

		sr = null;
		return o;

	}

	/**
	 * ClientFactoryを設定する。
	 * @param clientFactory ClientFactory
	 */
	public void setClientFactory(ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	/**
	 * EndpointFactoryを設定する。
	 * @param endpointFactory EndpointFactory
	 */
	public void setEndpointFactory(EndpointFactory endpointFactory) {
		this.endpointFactory = endpointFactory;
	}

	/**
	 * Msgpackサービスのポート番号（文字列）を設定する
	 * @param msgpackPort ポート番号
	 */
	public void setMsgpackPort(String msgpackPort) {
		this.msgpackPort = msgpackPort;
	}

	/**
	 * サービスマッピング情報を設定する。
	 * @param serviceMapping サービスマッピング情報
	 */
	public void setServiceMapping(String serviceMapping) {
		this.serviceMapping = serviceMapping;
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.ial.webapps.wisdom.proxy.ProxyServiceName#setServiceName(java.lang.String)
	 */
	@Override
	public void setServiceName(String name) {
		this.serviceName = name;

	}

	/* (非 Javadoc)
	 * @see jp.go.nict.langrid.servicecontainer.executor.StreamingNotifier#setReceiver(jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver<T>)
	 */
	@Override
	public void setReceiver(StreamingReceiver<Object> receiver) {
		System.out.printf("setReceiver %s \n", receiver.getClass().toString());
		sr = receiver;
	}

}

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

package jp.go.nict.ial.servicecontainer.handler.websocketjson;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.xml.soap.MimeHeaders;

import jp.go.nict.ial.websocket.Connection;
import jp.go.nict.ial.websocket.ConnectionListener;
import jp.go.nict.ial.websocket.WebSocketHandler;
import jp.go.nict.langrid.commons.beanutils.Converter;
import jp.go.nict.langrid.commons.beanutils.ConverterForJsonRpc;
import jp.go.nict.langrid.commons.lang.ClassUtil;
import jp.go.nict.langrid.commons.lang.StringUtil;
import jp.go.nict.langrid.commons.rpc.RpcFault;
import jp.go.nict.langrid.commons.rpc.RpcFaultUtil;
import jp.go.nict.langrid.commons.rpc.RpcHeader;
import jp.go.nict.langrid.commons.rpc.json.JsonRpcRequest;
import jp.go.nict.langrid.commons.rpc.json.JsonRpcResponse;
import jp.go.nict.langrid.commons.ws.RpcServiceContext;
import jp.go.nict.langrid.commons.ws.ServiceContext;
import jp.go.nict.langrid.commons.ws.ServletConfigServiceContext;
import jp.go.nict.langrid.repackaged.net.arnx.jsonic.JSON;
import jp.go.nict.langrid.servicecontainer.executor.StreamingNotifier;
import jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver;
import jp.go.nict.langrid.servicecontainer.handler.RIProcessor;
import jp.go.nict.langrid.servicecontainer.handler.ServiceFactory;
import jp.go.nict.langrid.servicecontainer.handler.ServiceLoader;

/**
 * WebSocket用ハンドラーの実装クラス.<br>
 * jp.go.nict.ial.websocket.WebSocketHandlerを実装する.
 *
 */
public class WebSocketJsonRpcHandler implements WebSocketHandler {

	/* (非 Javadoc)
	 * @see jp.go.nict.ial.websocket.WebSocketHandler#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) {
		this.config = config;
		this.classLoader = Thread.currentThread().getContextClassLoader();
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.ial.websocket.WebSocketHandler#destroy()
	 */
	@Override
	public void destroy() {
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.ial.websocket.WebSocketHandler#createConnectionListener()
	 */
	@Override
	public ConnectionListener createConnectionListener() {

		final ExecutorService ex = Executors.newCachedThreadPool();
		final ConcurrentLinkedQueue<String> abortQue = new ConcurrentLinkedQueue<String>();

		return new ConnectionListener() {
			@Override
			public void onOpen(Connection connection) {
				this.connection = connection;
				URI uri = connection.getRequestUri();
				System.out.println("connect: " + uri);
				String[] paths = uri.getPath().split("\\/");
				serviceName = paths[paths.length - 1];

				abortQue.clear();
/*
				ServiceContext sc = new ServletConfigServiceContext(config);
				ServiceFactory factory = new ServiceLoader(sc).loadServiceFactory(
						classLoader, serviceName);
				service = factory.createService(
						classLoader, sc, factory.getInterfaces().iterator().next());
				interfaces = factory.getInterfaces();
*/
			}

			@Override
			public void onClose(int status) {
			}

			@Override
			public void onTextMessage(CharSequence message) {
				final JsonRpcRequest req = JSON.decode(message.toString(), JsonRpcRequest.class);
				if (req.getHeaders() == null) {
					req.setHeaders(new RpcHeader[] {});
				}

				final ServiceContext sc = new RpcServiceContext(new ServletConfigServiceContext(config),req.getHeaders());
				final ServiceFactory factory = new ServiceLoader(sc).loadServiceFactory(classLoader, serviceName);
				final Object service = factory.createService(classLoader, sc, factory.getInterfaces().iterator().next());
				final Iterable<Class<?>> interfaces = factory.getInterfaces();

				ex.execute(new Runnable() {

					@Override
					public void run() {
						Method method = null;
						JsonRpcResponse res = new JsonRpcResponse();
						try {
							int paramLength = req.getParams() == null ? 0 : req.getParams().length;
							for (Class<?> clz : interfaces) {
								method = ClassUtil.findMethod(clz, req.getMethod(), paramLength);
								if (method == null)
									continue;
								break;
							}
							if (method == null) {

								if(req.getMethod().equals("websock_abort")){
									/* 中断処理 */
									abortQue.add(req.getId());

								}

								logger.warning(String.format(
										"method \"%s(%s)\" not found in service \"%s\"."
										, req.getMethod(), StringUtil.repeatedString("arg", paramLength, ",")
										, serviceName));
								connection.send(create404(req.getId()));
								return;
							}
							Object result = null;
							List<RpcHeader> resHeaders = new ArrayList<RpcHeader>();
							RIProcessor.start(sc);
							try {
								// Currently only array("[]") is supported, while JsonRpc accepts Object("{}")
								Class<?>[] ptypes = method.getParameterTypes();
								Object[] params = req.getParams();
								Object[] args = new Object[ptypes.length];
								for (int i = 0; i < args.length; i++) {
									if (params[i].equals("")) {
										if (ptypes[i].isPrimitive()) {
											args[i] = ClassUtil.getDefaultValueForPrimitive(ptypes[i]);
										} else {
											args[i] = null;
										}
									} else {
										args[i] = converter.convert(params[i], ptypes[i]);
									}
								}
								if (service instanceof StreamingNotifier<?>) {
									((StreamingNotifier<Object>) service).setReceiver(new StreamingReceiver<Object>() {
										@Override
										public boolean receive(Object result) {
											JsonRpcResponse r = new JsonRpcResponse();
											r.setId(req.getId() + "-ae");
											r.setResult(result);
											try {
												/* キューに入っていたら送信を中止 */
												if(!abortQue.contains(req.getId())){
													connection.send(JSON.encode(r));
												}
											} catch (Exception e) {
												throw new RuntimeException(e);
											}
											return true;
										}
									});
								}

								Thread.currentThread().setContextClassLoader(classLoader);

								result = method.invoke(service, args);
							} finally {
								MimeHeaders resMimeHeaders = new MimeHeaders();
								RIProcessor.finish(resMimeHeaders, resHeaders);
								//								MimeHeadersUtil.setToHttpServletResponse(resMimeHeaders, response);
							}
							res.setId(req.getId());
							res.setHeaders(resHeaders.toArray(new RpcHeader[] {}));
							res.setResult(result);
						} catch (InvocationTargetException e) {
							Throwable t = e.getTargetException();
							logger.log(Level.SEVERE, "failed to handle request for " + serviceName
									+ ":" + service.getClass().getName() + "#" + req.getMethod()
									, t);
							res.setError(RpcFaultUtil.throwableToRpcFault("Server.userException", t));
						} catch (Exception e) {
							logger.log(Level.SEVERE, "failed to handle request for " + serviceName
									+ ":" + service.getClass().getName() + "#" + req.getMethod()
									, e);
							res.setError(RpcFaultUtil.throwableToRpcFault("Server.userException", e));
						}
						try {
							connection.send(JSON.encode(res));
						} catch (IOException e) {
							logger.log(Level.WARNING, "IOException occurred.", e);
						}

					}
				});

				System.out.println(message);
			}

			private Connection connection;
			private String serviceName;
//			private Object service;
//			private Iterable<Class<?>> interfaces;
			private Converter converter = new ConverterForJsonRpc();
		};
	}

	/**
	 * 404エラーレスポンスを生成する.
	 * 
	 * @param requestId リクエストID
	 * @return 404レスポンス
	 */
	private String create404(String requestId) {
		JsonRpcResponse res = new JsonRpcResponse();
		res.setId(requestId);
		RpcFault f = new RpcFault();
		f.setFaultCode("404");
		f.setFaultString("method not found");
		f.setDetail("method not found");
		res.setError(f);
		return JSON.encode(res);
	}

	/**
	 * ServletConfig
	 */
	private ServletConfig config;
	/**
	 * ClassLoader
	 */
	private ClassLoader classLoader;

	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(WebSocketJsonRpcHandler.class.getName());
}

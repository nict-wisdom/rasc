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

package jp.go.nict.ial.servicecontainer.handler.msgpackrpc;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import jp.go.nict.langrid.commons.ws.LocalServiceContext;
import jp.go.nict.langrid.commons.ws.ServiceContext;
import jp.go.nict.langrid.servicecontainer.handler.RIProcessor;
import jp.go.nict.langrid.servicecontainer.handler.ServiceFactory;
import jp.go.nict.langrid.servicecontainer.handler.ServiceLoader;

import org.msgpack.rpc.Server;
import org.msgpack.rpc.ServerEx;
import org.msgpack.rpc.loop.EventLoop;
import org.msgpack.rpc.loop.netty.NettyEventLoopFactoryEx;
import org.msgpack.rpc.reflect.Reflect;

/**
 * MessagePackRPC用Handler.<BR>
 * MessagePackRPCでサービスを起動する。
 *
 */
public class MsgPackRpcServer {

	/**
	 * サンプルメイン.
	 * @param args 引数配列
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String serviceName = "HelloService";
		int port = 19999;
		if (args.length > 0)
			serviceName = args[0];
		if (args.length > 1)
			port = Integer.parseInt(args[1]);

		MsgPackRpcServer server = new MsgPackRpcServer();
		server.start(serviceName, port);
	}

	/**
	 * MsgPackRPCサーバ起動.
	 * <BR>
	 * <BR>ServiceLoaderでサービスをロードする。
	 * <BR>serviceNameは、サービス.XML のサービス名(.XMLを除去)を指定する。
	 * 
	 * <BR>WARFILE
	 * <BR>+--META-INF/
	 * <BR>+--WEB-INF/
	 * <BR>+----serviceimpl/
	 * <BR>+------service.xml
	 * 
	 * @param serviceName サービス名(サービス.XMLの.XMLを除去したもの)
	 * @param port 起動するポート番号
	 * @throws UnknownHostException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void start(String serviceName, int port) throws UnknownHostException, IOException, InterruptedException {
		start(serviceName, port, null, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * MsgPackRPCサーバ起動.
	 * <BR>
	 * <BR>ServiceLoaderでサービスをロードする。
	 * <BR>serviceNameは、サービス.XML のサービス名(.XMLを除去)を指定する。
	 * 
	 * <BR>WARFILE
	 * <BR>+--META-INF/
	 * <BR>+--WEB-INF/
	 * <BR>+----serviceimpl/
	 * <BR>+------service.xml
	 * 
	 * @param serviceName サービス名(サービス.XMLの.XMLを除去したもの)
	 * @param port 起動するポート番号
	 * @param srcPath サービスへのルートパス
	 * @param classLoader 起動に使用するClassLoader
	 * @throws UnknownHostException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void start(String serviceName, int port, final String srcPath, final ClassLoader classLoader) throws UnknownHostException, IOException, InterruptedException {

		EventLoop.setFactory(new NettyEventLoopFactoryEx());
		ServerEx server = new ServerEx();
		try {
			ServiceContext sc = new LocalServiceContext() {
				@Override
				public String getRealPath(String path) {
					return (srcPath != null) ? (srcPath + "/" + path) : path;
				}
			};
			ServiceFactory factory =
				new ServiceLoader(sc).loadServiceFactory(classLoader, serviceName);
			
			if(factory == null){
				throw new IOException(String.format("Failed to load service definition (%s). Make sure the path and service name are correct.", serviceName));
			}
			
			RIProcessor.start(sc);

			try {
				serve(classLoader, server, factory, sc);
				server.listen(port);
				server.getEventLoop().join();
			} finally {
				RIProcessor.finish();
			}
		} finally {
			server.getEventLoop().shutdown();
			server.close();
		}
	}

	/**
	 * サービスを開始する.
	 * 
	 * @param classLoader クラスローダー
	 * @param server MsgPackRPCサーバ
	 * @param factory ServiceFactory
	 * @param context コンテキスト
	 */
	private static void serve(
			ClassLoader classLoader, Server server,
			ServiceFactory factory, final ServiceContext context) {
		List<Method> methods = new ArrayList<Method>();
		Class<?> firstIntf = null;
		for (Class<?> intf : factory.getInterfaces()) {
			if (firstIntf == null)
				firstIntf = intf;
			for (Method m : intf.getMethods()) {
				methods.add(m);
			}
		}
		final Object service = factory.createService(classLoader, context, firstIntf);

		final MethodDispatcherEx dp =
				new MethodDispatcherEx(new Reflect(server.getEventLoop().getMessagePack()), Proxy.newProxyInstance(classLoader,
						factory.getInterfaces().toArray(new Class[] {}),
						new InvocationHandler() {
							@Override
							public Object invoke(Object proxy, Method method, Object[] args)
									throws Throwable {
								RIProcessor.start(context);
								try {
									return method.invoke(service, args);
								} finally {
									RIProcessor.finish();
								}
							}
						}), methods.toArray(new Method[] {}));
		server.serve(dp);
	}
}

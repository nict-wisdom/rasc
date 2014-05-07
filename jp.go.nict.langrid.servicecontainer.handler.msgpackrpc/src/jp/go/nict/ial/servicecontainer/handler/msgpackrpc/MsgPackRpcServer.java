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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jp.go.nict.langrid.commons.ws.LocalServiceContext;
import jp.go.nict.langrid.commons.ws.ServiceContext;
import jp.go.nict.langrid.servicecontainer.handler.RIProcessor;
import jp.go.nict.langrid.servicecontainer.handler.ServiceFactory;
import jp.go.nict.langrid.servicecontainer.handler.ServiceLoader;

import org.msgpack.rpc.RequestEx;
import org.msgpack.rpc.Server;
import org.msgpack.rpc.ServerEx;
import org.msgpack.rpc.loop.EventLoop;
import org.msgpack.rpc.loop.netty.NettyEventLoopFactoryEx;
import org.msgpack.rpc.reflect.Reflect;
import org.msgpack.type.ValueFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
	public void start(String serviceName, int port, final String srcPath, final ClassLoader classLoader)
			throws UnknownHostException, IOException, InterruptedException {

		EventLoop.setFactory(new NettyEventLoopFactoryEx());
		ServerEx server = new ServerEx();
		try {

			final String servicesPath = loadWebXml(srcPath + "/WEB-INF/web.xml");

			ServiceContext sc = new LocalServiceContext() {
				@Override
				public String getRealPath(String path) {
					return (srcPath != null) ? (srcPath + "/" + path) : path;
				}

				@Override
				public String getInitParameter(String param) {

					if (param.equals("servicesPath")) {
						if (servicesPath != null) {
							return servicesPath;
						} else {
							return super.getInitParameter(param);
						}
					}

					return super.getInitParameter(param);
				}

			};

			ServiceFactory factory =
					new ServiceLoader(sc).loadServiceFactory(classLoader, serviceName);

			if (factory == null) {
				throw new IOException(String.format(
						"Failed to load service definition (%s). Make sure the path and service name are correct.",
						serviceName));
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
	 * web.xmlが存在する場合には、web.xmlからservicesPathを取得してServiceContextに設定する。
	 *
	 * @param sc ServiceContext
	 */
	private String loadWebXml(String strPath) {
		File f = new File(strPath);
		String servicePath = null;

		System.out.println(strPath);
		/*存在チェック*/
		if (!f.exists()) {
			return null;
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);

		try {

			System.out.println(f.getPath());
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(f);
			XPathFactory xfactory = XPathFactory.newInstance();
			XPath xpath = xfactory.newXPath();
			NodeList nodelist = (NodeList) xpath.evaluate("/web-app/context-param", doc, XPathConstants.NODESET);

			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node = nodelist.item(i);

				String name = xpath.evaluate("./param-name/text()", node);
				System.out.println(name);
				if (name.equals("servicesPath")) {
					String value = xpath.evaluate("./param-value/text()", node);
					System.out.println(value);
					servicePath = value;
					break;
				}
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return servicePath;
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
				new MethodDispatcherEx(new Reflect(server.getEventLoop().getMessagePack()), Proxy.newProxyInstance(
						classLoader,
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

		/* 初期化(init)を呼んで見る <TEST> */
		/* この時点で、serviceとMethodDispatcherが生成されているので、基本的にはサービスを呼び出し可能なはず*/
		boolean hasServiceInitialize = false;

		for (Class<?> clsIf : factory.getInterfaces()) {
//			System.out.println(clsIf.getName());
			if (clsIf.getName().equals("jp.go.nict.wisdom.wrapper.status.ServiceInitialize")) {
				for (Method m : clsIf.getMethods()) {
//					System.out.println(m.getName());
					if (m.getName().equals("init")) {
						hasServiceInitialize = true;
					}
				}
				break;
			}
		}
		if (hasServiceInitialize) {
			try {
				dp.dispatch(new RequestEx("init",ValueFactory.createArrayValue()));
			} catch (Exception e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}

		server.serve(dp);
	}
}

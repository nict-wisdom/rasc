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

package jp.go.nict.langrid.webapps.jetty.embedded;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.go.nict.langrid.webapps.jetty.embedded.EmbeddedServer.DeployProp.deployContext;
import net.arnx.jsonic.JSON;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * 組み込みJettyサーバ実装.
 * servlet及びmsgpackのサービスを起動する.
 * @author kishimoto
 *
 */
public class EmbeddedServer {

	/**
	 * JSONファイル読み込み用Class.
	 * JSONファイルからこのクラスへリフレクションする.
	 * 
	 * @author kishimoto
	 *
	 */
	public class DeployProp {

		/**
		 * サービスコンテキスト定義用Class.
		 * @author kishimoto
		 *
		 */
		public class deployContext {

			/**
			 * コンテキストパス.
			 */
			private String contextPath;

			/**
			 * msgpack待ち受けポート番号.
			 */
			private int msgpackPort;

			/**
			 * サービス名.
			 * サービスXMLから.XMLを除去したもの.			 
			 * */
			private String serviceName;

			/**
			 * サービスのwarファイルへのフルパス.
			 */
			private String warPath;

			/**
			 * コンストラクタ.
			 */
			public deployContext() {

			}

			/**
			 * コンテキストパス取得.
			 * @return コンテキストパス
			 */
			public String getContextPath() {
				return contextPath;
			}

			/**
			 * msgpack待ち受けポート番号取得.
			 * @return msgpack待ち受けポート番号.
			 */
			public int getMsgpackPort() {
				return msgpackPort;
			}

			/**
			 * サービス名取得.
			 * @return サービス名
			 */
			public String getServiceName() {
				return serviceName;
			}

			/**
			 * Warファイルへのパス取得.
			 * @return ファイルパス.
			 */
			public String getWarPath() {
				return warPath;
			}

			/**
			 * コンテキストパス設定
			 * @param contextPath コンテキストパス
			 */
			public void setContextPath(String contextPath) {
				this.contextPath = contextPath;
			}

			/**
			 * msgpack待ち受けポート番号設定.
			 * @param msgpackPort 待ち受けポート番号
			 */
			public void setMsgpackPort(int msgpackPort) {
				this.msgpackPort = msgpackPort;
			}

			/**
			 * サービス名設定
			 * @param serviceName サービス名
			 */
			public void setServiceName(String serviceName) {
				this.serviceName = serviceName;
			}

			/**
			 * Warファイルのパス設定
			 * @param warPath Warファイルへのパス
			 */
			public void setWarPath(String warPath) {
				this.warPath = warPath;
			}

		}

		/**
		 * 制御ポート番号(現在未使用)
		 */
		private int controlPort;
		/**
		 * httpServletサービスコンテキストリスト
		 */
		private Set<deployContext> httpServices = new HashSet<deployContext>();
		/**
		 * httpServlet用のポート(Soap/ProtcolBuffers/JSON-RPC)
		 */
		private int jettyPort;
		/**
		 * MsgPackサービスコンテキストリスト
		 */
		private Set<deployContext> msgpackServices = new HashSet<deployContext>();
		/**
		 * サーバ名
		 */
		private String serverName;

		/**
		 * コンストラクタ
		 */
		public DeployProp() {

		}

		/**
		 * 制御用ポート番号取得.
		 * @return 制御用ポート番号.
		 */
		public int getControlPort() {
			return controlPort;
		}

		/**
		 * httpServletコンテキストリスト取得
		 * @return コンテキストリスト
		 */
		public Set<deployContext> getHttpServices() {
			return httpServices;
		}

		/**
		 * httpServlet用のポート(Soap/ProtcolBuffers/JSON-RPC)取得
		 * @return httpServlet用のポート
		 */
		public int getJettyPort() {
			return jettyPort;
		}

		/**
		 * msgpackサービス用コンテキストリスト取得.
		 * @return コンテキストリスト.
		 */
		public Set<deployContext> getMsgpackServices() {
			return msgpackServices;
		}

		/**
		 * サービス名取得
		 * @return サービス名
		 */
		public String getServerName() {
			return serverName;
		}

		/**
		 * 制御用ポート番号設定
		 * @param controlPort ポート番号
		 */
		public void setControlPort(int controlPort) {
			this.controlPort = controlPort;
		}

		/**
		 * httpServlet用コンテキストリスト設定
		 * @param httpServices コンテキストリスト
		 */
		public void setHttpServices(Set<deployContext> httpServices) {
			this.httpServices = httpServices;
		}

		/**
		 * httpServlet用のポート番号設定.
		 * @param jettyPort ポート番号
		 */
		public void setJettyPort(int jettyPort) {
			this.jettyPort = jettyPort;
		}

		/**
		 * msgpack用コンテキストリスト設定.
		 * @param msgpackServices コンテキストリスト
		 */
		public void setMsgpackServices(Set<deployContext> msgpackServices) {
			this.msgpackServices = msgpackServices;
		}

		/**
		 * サーバ名設定. 
		 * 
		 * @param serverName サーバ
		 */
		public void setServerName(String serverName) {
			this.serverName = serverName;
		}

	}

	private static final String KEY_ADDLIB = "-addjar";
	private static final String KEY_JSONFILE = "-json";
	private static final String KEY_TMPPATH = "-t";

	/**
	 * JettyMsgpackRPCServiceLoader実装クラス名
	 */
	private static final String JETTY_MSGPACKRPC_SERVICELOADER = "jp.go.nict.ial.jetty.msgpackrpc.serviceloader.JettyMsgpackRPCServiceLoaderImpl";

	/**
	 * Logger
	 *
	 */
	private static final Logger logger = Logger.getGlobal();

	/**
	 * ヘルプ表示.
	 */
	private static final void printHelp() {
		System.out.println("Usage");
		System.out.println(" Options:");
		System.out.println("  -json : 起動用の設定ファイル(JSON形式) ※必須");
		System.out.println("  -t : Jettyのテンポラリフォルダ（書き込み可能であること) ※必須");
		System.out.println("  -addjar : デフォルトのクラスパス以外にJarのロードを指定(省略可能) ");
		System.out.println("       -addjar xxxx.jar,yyyy.jar  (複数の場合には、,で区切る)");
	}

	/**
	 * 起動用Main
	 * @param args 起動時の引数
	 */
	public static void main(String[] args) {

		logger.setLevel(Level.INFO);// for java7( bugs fix.)
		logger.log(Level.INFO, "Start EmbeddedServer.");

		/* 組み込みJetty起動*/
		EmbeddedServer es = new EmbeddedServer(args);
		es.start();

	}

	/**
	 * 引数を保存.
	 */
	private String[] args = null;

	/**
	 * コンストラクタ.
	 */
	public EmbeddedServer() {

	}

	/**
	 * コンストラクタ.
	 * @param args 引数.
	 */
	public EmbeddedServer(String[] args) {
		this();
		this.args = args;
	}

	/**
	 * フォルダ削除.
	 * @param pathRoot 削除するフォルダ
	 */
	protected void cleanupDir(File pathRoot) {

		if (pathRoot.exists() == false) {
			return;
		}

		if (pathRoot.isFile()) {
			pathRoot.delete();
		}

		/* フォルダの場合は、再帰処理.*/
		if (pathRoot.isDirectory()) {
			for (File sub : pathRoot.listFiles()) {
				cleanupDir(sub);
			}
			pathRoot.delete();
		}
	}

	/**
	 * Warを展開する.
	 * Msgpack用にwarを展開して、tmpフォルダに格納する。
	 * ClassLoaderに展開したWarのライブラリを設定する。
	 * 
	 * @param pathRoot 展開先のパス.
	 * @param war WARファイルへのパス.
	 * @return ClassLoader(classPath設定済み)
	 */
	protected final ClassLoader UnpackWar(String pathRoot, String war) {

		File dir = new File(pathRoot);
		if (!dir.exists()) {
			dir.mkdirs();
		} else {
			/*削除してから、再構築*/
			cleanupDir(dir);
			dir.mkdirs();
		}

		List<URL> clsPath = new ArrayList<URL>();

		/* Warの展開処理 */
		try (FileInputStream fis = new FileInputStream(war)) {
			try (JarInputStream jis = new JarInputStream(fis)) {
				while (true) {
					JarEntry je = jis.getNextJarEntry();
					if (je == null) {
						break;
					}
					if (je.isDirectory()) {
						File d = new File(pathRoot + "/" + je.getName().replace("/services/", "/serviceimpl/"));
						if (!d.exists()) {
							d.mkdirs();
						}
						String ph = d.getPath();
						if (ph.contains("classes")) {
							clsPath.add(d.toURI().toURL());
						}

					} else {
						File f = new File(pathRoot + "/" + je.getName().replace("/services/", "/serviceimpl/"));
						if (!f.exists()) {
							/* folder 操作*/
							String parent = f.getParent();
							if (parent != null) {
								File mkdir = new File(parent);
								if (!mkdir.exists()) {
									mkdir.mkdirs();
								}
							}
						}
						try (FileOutputStream fos = new FileOutputStream(f)) {
							byte[] buf = new byte[256];
							int size = 0;
							while ((size = jis.read(buf)) > 0) {
								fos.write(buf, 0, size);
							}
						}

						/* .jar等はクラスパスへ設定する */
						String cp = f.getPath();
						if (cp.contains("WEB-INF")) {
							if (cp.contains(".jar")) {
								clsPath.add(f.toURI().toURL());
							}
						}
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*msgPack起動用のClassLoaderを返す*/
		return new URLClassLoader(clsPath.toArray(new URL[] {}), Thread.currentThread().getContextClassLoader());

	}

	/**
	 * 引数取得.
	 * @return 起動時の引数
	 */
	public String[] getArgs() {
		return args;
	}

	/**
	 * 起動設定ファイル（JSON）読み込み.
	 * @param file 読み込むファイル(JSON)
	 * @return サービスコンテキスト定義用Class.
	 */
	public DeployProp loadJsonfile(String file) {

		DeployProp dp = null;

		/* jsonic でデコード */
		try (FileInputStream fis = new FileInputStream(file)) {
			dp = JSON.decode(fis, DeployProp.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dp;
	}

	/**
	 * 起動引数を設定. 
	 *
	 * @param args 起動引数.
	 */
	public void setArgs(String[] args) {
		this.args = args;
	}

	/**
	 * サービス開始処理.
	 * httpServletの起動と、msgpackサービスの起動を行う.
	 */
	public void start() {

		boolean isName = true;
		String name = "";
		String value = "";

		/* 引数をmapへ変換 */
		Map<String, String> argMap = new HashMap<String, String>();

		for (String s : args) {
			if (isName) {
				name = s.trim();
				isName = false;
			} else {
				value = s.trim();
				isName = true;
				argMap.put(name, value);
			}
		}

		/* Jsonfile */
		if (!argMap.containsKey(KEY_JSONFILE)) {
			printHelp();
			return;
		}

		/* Temp Path*/
		if (!argMap.containsKey(KEY_TMPPATH)) {
			printHelp();
			return;
		}

		/* ADDJARがある場合には、クラスパスに追加する*/
		if (argMap.containsKey(KEY_ADDLIB)) {
			ClassLoader cl = ClassLoader.getSystemClassLoader();
			try {
				String pathLib = argMap.get(KEY_ADDLIB);
				List<File> files = new ArrayList<File>();
				if (pathLib.contains(",")) {
					for (String lst : pathLib.split(",")) {
						File f = new File(lst);
						if (f.exists()) {
							files.add(f);
						}
					}
				} else {
					File f = new File(pathLib);
					if (f.exists()) {
						files.add(f);
					}
				}

				/* ClassLoaderへクラスパスを設定する */
				Method methodAddUrl;
				methodAddUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);

				if (methodAddUrl != null) {
					methodAddUrl.setAccessible(true);
					for (File f : files) {
						methodAddUrl.invoke(cl, f.toURI().toURL());
						logger.log(Level.INFO, "add to classpath  from {0}", f.toPath());
					}
				}
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		/*設定ファイルデコード*/
		DeployProp dp = loadJsonfile(argMap.get(KEY_JSONFILE));

		if (dp == null) {
			System.out.printf("JSONファイル<%s>が不正です。\n");
			return;
		}

		if (dp.getJettyPort() <= 0) {
			return;
		}

		if ((dp.getHttpServices().size() <= 0) && (dp.getMsgpackServices().size() <= 0)) {
			return;

		}

		/* executor */
		ExecutorService es = Executors.newCachedThreadPool();

		final Server jetty = (dp.getHttpServices().size() > 0) ? new Server(dp.getJettyPort()) : null;
		final ArrayList<WebAppContext> wcs = new ArrayList<WebAppContext>();

		/* http servlet */
		if (dp.getHttpServices().size() > 0) {

			for (deployContext dc : dp.getHttpServices()) {
				WebAppContext wc = new WebAppContext();
				wc.setContextPath(dc.getContextPath());
				wc.setWar(dc.getWarPath());
				wc.setTempDirectory(new File(argMap.get(KEY_TMPPATH) + dc.getContextPath()));
				//				wc.setParentLoaderPriority(false);
				System.out.println(wc);
				wcs.add(wc);
			}

			if (jetty != null) {
				es.execute(new Runnable() {

					@Override
					public void run() {
						ContextHandlerCollection collection = new ContextHandlerCollection();
						collection.setHandlers(wcs.toArray(new Handler[0]));
						final HandlerCollection handlerCollection = new HandlerCollection();
						handlerCollection.setHandlers(new Handler[] { collection });
						jetty.setHandler(handlerCollection);
						try {
							jetty.start();
						} catch (Exception e) {
							e.printStackTrace();
							return;
						}
					}
				});
			}
		}

		/* http-RPCがロードされるまで待つ */
		if (jetty != null) {
			try {
				int waitCount = 100;
				while (!jetty.isStarted()) {
					Thread.sleep(1000);
					System.out.println(jetty.getServer().getState());
					waitCount--;
					if (waitCount <= 0) {
						break;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/* msgPack services */

		if (dp.getMsgpackServices().size() > 0) {
			for (deployContext dc : dp.getMsgpackServices()) {
				final String warRoot = argMap.get(KEY_TMPPATH) + "/msgpack/" + dc.contextPath;
				final int port = dc.getMsgpackPort();
				final String serviceName = dc.getServiceName();
				ClassLoader warClassLoader = UnpackWar(warRoot, dc.warPath);
				logger.log(Level.INFO, "Msgpack:起動しています {0}", serviceName);
				if (jetty != null) {
					for (WebAppContext wc : wcs) {
						if (wc.getContextPath().equals(dc.getContextPath())) {
							WebAppClassLoader wacl = (WebAppClassLoader) wc.getClassLoader();
							warClassLoader = wacl;
							break;
						}
					}
				}
				final ClassLoader cl = warClassLoader;

				es.execute(new Runnable() {

					@Override
					public void run() {
						Thread.currentThread().setContextClassLoader(cl);

						try {
							//JettyMsgpackRPCServiceLoaderImplを経由させてサービスをロードする。
							Class<?> ldclz = cl.loadClass(JETTY_MSGPACKRPC_SERVICELOADER);
							logger.log(Level.INFO, "Msgpack:ClassLoader {0}", ldclz.getClassLoader());
							Method method = ldclz.getMethod("startService", String.class,int.class,String.class,ClassLoader.class);
							method.invoke(ldclz.newInstance(), serviceName, port, warRoot, cl);
							
						} catch (ClassNotFoundException e1) {
							e1.printStackTrace();
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
						} catch (SecurityException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
		/* executor終了待ち*/
		es.shutdown();
	}
}

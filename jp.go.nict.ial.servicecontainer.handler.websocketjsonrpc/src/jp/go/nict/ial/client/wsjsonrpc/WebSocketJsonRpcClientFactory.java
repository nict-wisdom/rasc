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

package jp.go.nict.ial.client.wsjsonrpc;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import jp.go.nict.langrid.client.ClientFactory;
import jp.go.nict.langrid.client.RequestAttributes;
import jp.go.nict.langrid.client.ResponseAttributes;
import jp.go.nict.langrid.client.RpcRequestAttributes;
import jp.go.nict.langrid.client.RpcResponseAttributes;
import jp.go.nict.langrid.commons.beanutils.Converter;
import jp.go.nict.langrid.commons.beanutils.ConverterForJsonRpc;
import jp.go.nict.langrid.commons.rpc.ArrayElementsNotifier;
import jp.go.nict.langrid.commons.rpc.ArrayElementsReceiver;
import jp.go.nict.langrid.commons.rpc.RpcFaultUtil;
import jp.go.nict.langrid.commons.rpc.json.JsonRpcRequest;
import jp.go.nict.langrid.commons.rpc.json.JsonRpcResponse;
import jp.go.nict.langrid.commons.rpc.json.JsonRpcUtil;
import jp.go.nict.langrid.commons.util.Trio;
import jp.go.nict.langrid.repackaged.net.arnx.jsonic.JSON;
import jp.go.nict.langrid.repackaged.net.arnx.jsonic.JSONException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;

/**
 * WebSocketRPC用のClientFactory実装クラス.
 * <br>
 * jp.go.nict.langrid.client.ClientFactoryを実装する.<BR>
 * WebSocket経由でサービスを呼び出すクライアントを作成するファクトリークラス.
 *
 */
public class WebSocketJsonRpcClientFactory implements ClientFactory {

	/**
	 * WebSocketJsonRpc用のInvocationHandler.
	 * java.lang.reflect.InvocationHandlerの実装クラス.
	 */
	static class WebSocketJsonRpcHandler implements InvocationHandler, ArrayElementsNotifier {

		/**
		 * コンストラクタ
		 * @param session セッション
		 * @param listener リスナー
		 */
		public WebSocketJsonRpcHandler(Session session, Listener listener) {
			this.session = session;
			this.listener = listener;
		}

		/* (非 Javadoc)
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			Class<?> clz = method.getDeclaringClass();
			if (clz.equals(RequestAttributes.class)) {
				return method.invoke(reqAttrs, args);
			} else if (clz.equals(ResponseAttributes.class)) {
				return method.invoke(resAttrs, args);
			} else if (clz.equals(ArrayElementsNotifier.class)) {
				return method.invoke(this, args);
			} else {
				session.getRemote().sendString(createRequest(listener.nextRequest(), method, args));
				List<Object> results = new ArrayList<>();
				Class<?> rt = method.getReturnType();
				while (true) {
					Trio<JsonRpcResponse, ?, RpcResponseAttributes> r = listener
							.waitForResponse(method.getReturnType());
					resAttrs = r.getThird();
					if (r.getFirst().getId().endsWith("-ae")) {
						if (receiver != null) {
							receiver.receive(r.getSecond());
							continue;
						}
						results.add(r.getSecond());
						continue;
					}
					if (results.size() > 0) {
						if (!rt.isArray()) {
							throw new RuntimeException("result type is not array while array response received.");
						}
						if (r.getSecond() != null)
							results.add(r.getSecond());
						return results.toArray((Object[]) Array.newInstance(rt.getComponentType(), results.size()));
					}
					return r.getSecond();
				}
			}
		}

		/* (非 Javadoc)
		 * @see jp.go.nict.langrid.commons.rpc.ArrayElementsNotifier#setReceiver(jp.go.nict.langrid.commons.rpc.ArrayElementsReceiver<T>)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T> void setReceiver(ArrayElementsReceiver<T> receiver) {
			this.receiver = (ArrayElementsReceiver<Object>) receiver;
		}

		/**
		 * WebSocketJsonRpc用のリクエスト電文を作成する.
		 * 
		 * @param reqId リクエストID
		 * @param method メソッド
		 * @param args メソッド引数
		 * @return 要求をJSON形式に変換した文字列
		 * @throws IOException 
		 */
		protected String createRequest(String reqId, Method method, Object[] args) throws IOException {
			JsonRpcRequest req = JsonRpcUtil.createRequest(reqAttrs.getAllRpcHeaders(), method, args);
			req.setId(reqId);
			return JSON.encode(req);
		}

		/**
		 * リクエスト用RPC属性
		 */
		private RpcRequestAttributes reqAttrs = new RpcRequestAttributes();
		/**
		 * レスポンス用RPC属性
		 */
		private RpcResponseAttributes resAttrs = new RpcResponseAttributes();
		/**
		 * セッション
		 */
		private Session session;
		/**
		 * リスナー
		 */
		private Listener listener;
		/**
		 * ストリーミング用のレシーバ.
		 */
		private ArrayElementsReceiver<Object> receiver;
	}

	/**
	 * WebSocket用のリスナークラス<br>
	 * org.eclipse.jetty.websocket.api.WebSocketListenerの実装クラス.
	 */
	static class Listener implements WebSocketListener {

		/**
		 * 次リクエストIDを取得する
		 * @return リスエストID
		 */
		public String nextRequest() {
			reqId++;
			return "" + reqId;
		}

		/**
		 * レスポンス待機処理.
		 * @param returnType メソッドの戻り値型
		 * @return レスポンス
		 */
		public Trio<JsonRpcResponse, Object, RpcResponseAttributes> waitForResponse(Class<?> returnType) {
			JsonRpcResponse ret = null;
			do {
				String res = "";
				try {
					res = results.pollFirst(1000, TimeUnit.SECONDS);
					ret = JSON.decode(res, JsonRpcResponse.class);
				} catch (InterruptedException e1) {
					throw new RuntimeException(e1);
				} catch (JSONException e) {
					throw new RuntimeException("parse error for JSON:" + res, e);
				}
			} while (!(ret.getId().equals("" + reqId) || ret.getId().equals(reqId + "-ae")));
			RpcResponseAttributes resAttrs = new RpcResponseAttributes();
			if (ret.getHeaders() != null) {
				try {
					resAttrs.loadAttributes(Arrays.asList(ret.getHeaders()));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
			if (ret.getError() != null) {
				throw new RuntimeException(RpcFaultUtil.rpcFaultToThrowable(ret.getError()));
			}
			return Trio.create(
					ret,
					(Object) converter.convert(ret.getResult(),
							(ret.getId().endsWith("-ae")) ? returnType.getComponentType() : returnType
							),
					resAttrs);
		}

		/**
		 * リクエストID管理
		 */
		private int reqId;
		/**
		 * 結果格納用キュー
		 */
		private BlockingDeque<String> results = new LinkedBlockingDeque<>();

		/* (非 Javadoc)
		 * @see org.eclipse.jetty.websocket.api.WebSocketListener#onWebSocketText(java.lang.String)
		 */
		@Override
		public void onWebSocketText(String arg0) {
			results.offerLast(arg0);
		}

		/* (非 Javadoc)
		 * @see org.eclipse.jetty.websocket.api.WebSocketListener#onWebSocketError(java.lang.Throwable)
		 */
		@Override
		public void onWebSocketError(Throwable arg0) {
		}

		/* (非 Javadoc)
		 * @see org.eclipse.jetty.websocket.api.WebSocketListener#onWebSocketConnect(org.eclipse.jetty.websocket.api.Session)
		 */
		@Override
		public void onWebSocketConnect(Session arg0) {
		}

		/* (非 Javadoc)
		 * @see org.eclipse.jetty.websocket.api.WebSocketListener#onWebSocketClose(int, java.lang.String)
		 */
		@Override
		public void onWebSocketClose(int arg0, String arg1) {
		}

		/* (非 Javadoc)
		 * @see org.eclipse.jetty.websocket.api.WebSocketListener#onWebSocketBinary(byte[], int, int)
		 */
		@Override
		public void onWebSocketBinary(byte[] arg0, int arg1, int arg2) {
		}
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.langrid.client.ClientFactory#create(java.lang.Class<T>, java.net.URL)
	 */
	@Override
	public <T> T create(Class<T> interfaceClass, URL url) {
		return create(interfaceClass, url, null, null);
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.langrid.client.ClientFactory#create(java.lang.Class<T>, java.net.URL, java.lang.String, java.lang.String)
	 */
	@Override
	public <T> T create(Class<T> interfaceClass, URL url, String userId,
			String password) {
		final WebSocketClient client = new WebSocketClient();
		clients.add(client);
		try {
			client.start();
			int i = url.getProtocol().equals("http") ? 7 : 8;
			URI uri = new URI("ws://" + url.toString().substring(i));
			Listener listener = new Listener();
			Session session = client.connect(listener, uri).get();
			return interfaceClass.cast(Proxy.newProxyInstance(
					Thread.currentThread().getContextClassLoader(),
					new Class<?>[] { interfaceClass, RequestAttributes.class,
							ResponseAttributes.class, ArrayElementsNotifier.class },
					new WebSocketJsonRpcHandler(session, listener)
					));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * WebSocketの全接続を切断する.
	 */
	public void closeAll() {
		for (WebSocketClient c : clients) {
			try {
				c.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		clients.clear();
	}

	/**
	 * 結果コンバーターオブジェクト
	 */
	private static Converter converter = new ConverterForJsonRpc();
	/**
	 * クライアントリスト.
	 */
	private List<WebSocketClient> clients = new ArrayList<>();
}

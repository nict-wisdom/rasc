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

package jp.go.nict.ial.websocket.servlet;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import jp.go.nict.ial.websocket.Connection;
import jp.go.nict.ial.websocket.ConnectionListener;
import jp.go.nict.ial.websocket.WebSocketHandler;
import jp.go.nict.langrid.commons.parameter.ParameterContext;
import jp.go.nict.langrid.commons.ws.param.ServletConfigParameterContext;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Jetty向けWebSocketServletの実装クラス.<br>
 * JettyのWebSocket実装に合わせた実装のServletクラス<BR>
 * org.eclipse.jetty.websocket.servlet.WebSocketServletを実装する.
 */
@SuppressWarnings("serial")
@WebSocket(maxMessageSize = 64 * 1024)
public class JettyWebSocketServlet extends WebSocketServlet {
	/* (非 Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ParameterContext pc = new ServletConfigParameterContext(config);
		try {
			handler = (WebSocketHandler) Class.forName(
					pc.getString("handlerClass", "jp.go.nict.ial.websocket.NullWebSocketHandler")).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new ServletException(e);
		}
		handler.init(config);
	}

	/* (非 Javadoc)
	 * @see org.eclipse.jetty.websocket.servlet.WebSocketServlet#destroy()
	 */
	@Override
	public void destroy() {
		System.out.println("destroying websocket servlet");
		if (handler != null)
			handler.destroy();
		super.destroy();
	}

	/* (非 Javadoc)
	 * @see org.eclipse.jetty.websocket.servlet.WebSocketServlet#configure(org.eclipse.jetty.websocket.servlet.WebSocketServletFactory)
	 */
	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.setCreator(new WebSocketCreator() {
			@Override
			public Object createWebSocket(UpgradeRequest arg0, UpgradeResponse arg1) {
				final URI uri = arg0.getRequestURI();
				return new WebSocketListener() {
					@Override
					public void onWebSocketConnect(Session session) {
						this.ep = session.getRemote();
						l.onOpen(new Connection() {
							@Override
							public void send(CharSequence text) throws IOException {
								ep.sendString(text.toString());
							}

							@Override
							public URI getRequestUri() {
								return uri;
							}
						});
					}

					@Override
					public void onWebSocketClose(int status, String arg1) {
						l.onClose(status);
					}

					@Override
					public void onWebSocketError(Throwable arg0) {
					}

					@Override
					public void onWebSocketBinary(byte[] arg0, int arg1,
							int arg2) {
					}

					@Override
					public void onWebSocketText(String text) {
						l.onTextMessage(text);
					}

					private RemoteEndpoint ep;
					private ConnectionListener l = handler.createConnectionListener();
				};
			}
		});
	}

	/**
	 * WebSocketHandlerオブジェクト
	 */
	private WebSocketHandler handler;
}

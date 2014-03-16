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
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import jp.go.nict.ial.websocket.Connection;
import jp.go.nict.ial.websocket.ConnectionListener;
import jp.go.nict.ial.websocket.WebSocketHandler;
import jp.go.nict.langrid.commons.parameter.ParameterContext;
import jp.go.nict.langrid.commons.ws.param.ServletConfigParameterContext;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

/**
 * Tomcat向けWebSocketServletの実装クラス.<br>
 * TomcatのWebSocket実装に合わせた実装のServletクラス<BR>
 * org.apache.catalina.websocket.WebSocketServletを実装する.
 */
@SuppressWarnings("serial")
public class TomcatWebSocketServlet extends WebSocketServlet {
	/* (非 Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ParameterContext pc = new ServletConfigParameterContext(config);
		try {
			handler = (WebSocketHandler)Class.forName(pc.getString("handlerClass", "jp.go.nict.ial.websocket.NullWebSocketEntry")).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new ServletException(e);
		}
		handler.init(config);
	}

	/* (非 Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		if(handler != null) handler.destroy();
		super.destroy();
	}

	/* (非 Javadoc)
	 * @see org.apache.catalina.websocket.WebSocketServlet#createWebSocketInbound(java.lang.String, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request) {
		try{
			final URI uri = new URI(request.getRequestURI());
			return new MessageInbound() {
				@Override
				protected void onBinaryMessage(ByteBuffer arg0) throws IOException {
				}
	
				@Override
				protected void onTextMessage(CharBuffer message) throws IOException {
					l.onTextMessage(message);
				}
	
				@Override
				protected void onOpen(final WsOutbound outbound) {
					l.onOpen(new Connection() {
						@Override
						public URI getRequestUri() {
							return uri;
						}
						@Override
						public void send(CharSequence text) throws IOException {
							outbound.writeTextMessage(CharBuffer.wrap(text));
						}
					});
				}
	
				@Override
				protected void onClose(int status) {
					l.onClose(status);
				}
	
				private ConnectionListener l = handler.createConnectionListener();
			};
		} catch(URISyntaxException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * WebSocketHandlerオブジェクト
	 */
	private WebSocketHandler handler;
	/**
	 * ロガーオブジェクト
	 */
	private static Logger logger = Logger.getLogger(TomcatWebSocketServlet.class.getName());
}

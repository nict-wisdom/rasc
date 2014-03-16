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

package jp.go.nict.ial.websocket;

import javax.servlet.ServletConfig;

/**
 * 空のWebSocketHandler実装クラス.<br>
 * jp.go.nict.ial.websocket.WebSocketHandlerを実装する.<BR>
 * 
 *
 */
public class NullWebSocketHandler implements WebSocketHandler{
	/* (非 Javadoc)
	 * @see jp.go.nict.ial.websocket.WebSocketHandler#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) {
		
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
	
		return new ConnectionListener() {
			
			@Override
			public void onTextMessage(CharSequence message) {
				
			}
			
			@Override
			public void onOpen(Connection connection) {
				
			}
			
			@Override
			public void onClose(int status) {
				
			}
		};
	}
}

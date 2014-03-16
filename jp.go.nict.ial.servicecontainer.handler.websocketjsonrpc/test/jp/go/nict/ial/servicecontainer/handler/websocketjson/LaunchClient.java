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
import java.net.URI;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class LaunchClient {
	public static void main(String[] args) throws Exception{
		String destUri = "ws://localhost:8080/echo";
		WebSocketClient client = new WebSocketClient();
		try {
			client.start();
			URI echoUri = new URI(destUri);
			ClientUpgradeRequest request = new ClientUpgradeRequest();
			client.connect(new WebSocketListener() {
				@Override
				public void onWebSocketText(String arg0) {
				}
				
				@Override
				public void onWebSocketError(Throwable arg0) {
				}
				
				@Override
				public void onWebSocketConnect(Session arg0) {
					System.out.println("client socket connected");
					try{
						arg0.getRemote().sendString("{\"msg\":\"hello\"}");
					} catch(IOException e){
						e.printStackTrace();
					}
				}
				
				@Override
				public void onWebSocketClose(int arg0, String arg1) {
				}
				
				@Override
				public void onWebSocketBinary(byte[] arg0, int arg1, int arg2) {
				}
			}, echoUri, request);
			System.out.printf("Connecting to : %s%n", echoUri);
			Thread.sleep(3000);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			try {
				client.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

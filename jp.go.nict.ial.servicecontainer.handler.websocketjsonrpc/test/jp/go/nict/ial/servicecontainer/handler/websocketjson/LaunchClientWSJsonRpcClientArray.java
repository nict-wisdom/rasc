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

import java.net.URL;
import java.util.Arrays;

import jp.go.nict.ial.client.wsjsonrpc.WebSocketJsonRpcClientFactory;
import jp.go.nict.ial.servicecontainer.handler.websocketjson.test.HelloArrayIntf;
import jp.go.nict.langrid.commons.rpc.ArrayElementsNotifier;
import jp.go.nict.langrid.commons.rpc.ArrayElementsReceiver;

public class LaunchClientWSJsonRpcClientArray {
	public static void main(String[] args) throws Exception{
		WebSocketJsonRpcClientFactory f = new WebSocketJsonRpcClientFactory();
		try{
			HelloArrayIntf h = f.create(
					HelloArrayIntf.class,
					new URL("http://localhost:8080/wsjsServices/HelloArray")
					);
			((ArrayElementsNotifier)h).setReceiver(new ArrayElementsReceiver<String>() {
				@Override
				public void receive(String element) {
					System.out.println("ae: " + element);
				}
			});
			System.out.println(Arrays.toString(h.hello("hi")));
			System.out.println(Arrays.toString(h.hello("hi")));
		} finally{
			f.closeAll();
		}
	}
}

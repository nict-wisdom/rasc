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

package jp.go.nict.ial.servicecontainer.msgpackrpc.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jp.go.nict.ial.servicecontainer.handler.msgpackrpc.MsgPackRpcServer;
import jp.go.nict.langrid.client.msgpackrpc.MsgPackClientFactory;
import jp.go.nict.wisdom.wrapper.status.ServiceInitialize;

/**
 * MsgPackRpcServerInitializeStarterクラス.<br>
 * msgpackサービス用のBootstrap実装クラス.
 * @author mori
 *
 */
public class MsgPackRpcServerInitializeStarter {
	
	/**
	 * Bootstrapメイン
	 * @param args 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Please Set the options.");
			System.out.println("[ServiceName] [Port]");
			return;
		}

		final String serviceName = args[0];
		final int port = Integer.parseInt(args[1]);

		ExecutorService exec = Executors.newCachedThreadPool();

		final MsgPackRpcServer server = new MsgPackRpcServer();
		Future<?> ftStart = exec.submit(new Runnable() {
			@Override
			public void run() {
				try {
					server.start(serviceName, port);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		Thread.sleep(3000);

		MsgPackClientFactory factory = new MsgPackClientFactory();
		final ServiceInitialize client = factory.create(ServiceInitialize.class, new InetSocketAddress("127.0.0.1",
				port));

		Future<?> ftInit = exec.submit(new Runnable() {
			@Override
			public void run() {
				client.init();
			}
		});

		ftInit.get();
		System.out.println(String.format("### [%s : %d] Initialized.", serviceName, port));

		ftStart.get();
		factory.close();
		System.out.println(String.format("### [%s : %d] Down.", serviceName, port));
	}
}

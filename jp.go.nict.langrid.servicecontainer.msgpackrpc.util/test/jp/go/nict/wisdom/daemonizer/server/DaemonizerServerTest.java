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

package jp.go.nict.wisdom.daemonizer.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import jp.go.nict.ial.servicecontainer.handler.msgpackrpc.MsgPackRpcServer;
import jp.go.nict.langrid.client.msgpackrpc.MsgPackClientFactory;
import jp.go.nict.wisdom.daemonizer.command.TestConfUtil;
import jp.go.nict.wisdom.wrapper.api.TextAnalysisService;

import org.junit.After;
import org.junit.Before;

public abstract class DaemonizerServerTest {
	private static Logger logger = Logger.getLogger(DaemonizerServerTest.class.getName());

	protected static final String propDir = "prop";
	protected static final String testPropDir = "test-prop";
	protected static final String HOST = "localhost";
	protected static final int PORT = 19999;
	protected static final int SHUTDOWN_WAIT = 30000;
	protected static final int START_WAIT = 3000;

	protected static String target;
	protected static TestConfUtil conf;

	protected TextAnalysisService client;

	private MsgPackClientFactory factory;
	private Thread serverThread;

	@Before
	public void setUp() throws Exception {
		try {
			conf = new TestConfUtil(testPropDir + "/" + target + ".properties");
		} catch (IOException e) {
			logger.warning("No property file found.");
		}

		serverThread = new Thread(){
			@Override
			public void run() {
				try {
					MsgPackRpcServer server = new MsgPackRpcServer();
					server.start("JdeppService", PORT);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
//		serverThread.start();
		Thread.sleep(START_WAIT);

		factory = new MsgPackClientFactory();
		client = factory.create(TextAnalysisService.class, new InetSocketAddress(HOST, PORT));
	}

	@After
	public void tearDown() throws Exception {
		factory.close();

//		serverThread.interrupt();
//
//		// Wait the server to stop at most 30sec
//		try {
//			serverThread.join(SHUTDOWN_WAIT);
//		} catch (InterruptedException e) {
//		}
		Thread.sleep(START_WAIT);
	}
}

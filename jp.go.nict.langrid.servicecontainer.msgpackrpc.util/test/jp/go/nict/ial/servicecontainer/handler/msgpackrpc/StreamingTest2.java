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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jp.go.nict.ial.mock.HelloService;
import jp.go.nict.langrid.client.msgpackrpc.MsgPackClientFactory;
import jp.go.nict.langrid.commons.rpc.ArrayElementsNotifier;
import jp.go.nict.langrid.commons.rpc.ArrayElementsReceiver;

public class StreamingTest2 {
	public static void main(String[] args) throws Exception {
		final MsgPackClientFactory factory = new MsgPackClientFactory();
		final HelloService sv = factory.create(HelloService.class, new URL("http://localhost:19999"));
		try {
			final List<String> results = new ArrayList<String>();
//			final AtomicInteger count = new AtomicInteger();

			if (sv instanceof ArrayElementsNotifier) {
				((ArrayElementsNotifier)sv).setReceiver(new ArrayElementsReceiver() {
					@Override
					public void receive(Object element) {
//for helloJava()
//						MsgResult r = (MsgResult)element;

//for helloArray()
						String r = (String)element;

//for helloInt()
//						Integer r = ((ResultConversion)sv).convertValue(element, int.class); // int はボクシングでIntegerに変換

						/* String に変換して積み上げ */
						results.add(r.toString());
					}
				});
			}
			System.out.println("Start");
			long startTime = System.currentTimeMillis();

// for helloArray()
			String[] r = sv.helloArray("01234567890 0123456789 0123456789 0123456789 0123456789"); //length = 54 char.

// for helloInt()
//			int[] r = sv.helloInt("01234567890 0123456789 0123456789 0123456789 0123456789"); //length = 54 char.

// for helloJava()
//			MsgResult[] r = sv.helloJava("01234567890 0123456789 0123456789 0123456789 0123456789"); //length = 54 char.



			long endTime = System.currentTimeMillis();
			System.out.printf("Done. %d msec (result:%d) \n", endTime - startTime,results.size());
			System.out.println("Streaming Recived Result");
			for (String s : results) {
				System.out.println(s);
			}
		} finally {
			System.out.println("--- Finish!!! ---");
			factory.shutdown();//追加

		}
	}
}

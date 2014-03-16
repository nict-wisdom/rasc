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

import jp.go.nict.ial.mock.HelloService;
import jp.go.nict.langrid.client.msgpackrpc.MsgPackClientFactory;
import jp.go.nict.langrid.commons.rpc.ArrayElementsNotifier;
import jp.go.nict.langrid.commons.rpc.ArrayElementsReceiver;

import org.msgpack.type.Value;

public class StreamingTest {
	public static void main(String[] args) throws Exception{
		MsgPackClientFactory factory = new MsgPackClientFactory();
		HelloService sv = factory.create(HelloService.class, new URL("http://localhost:19999"));
		try{
			if (sv instanceof ArrayElementsNotifier) {
				ArrayElementsNotifier arn = (ArrayElementsNotifier) sv;
				arn.setReceiver(new ArrayElementsReceiver() {
					@Override
					public void receive(Object element) {
						Value v = (Value) element;
						System.out.println(v);
						//					System.out.printf("RECV:%d\n",count.incrementAndGet());
					}
				});
			}

			System.out.println("Start hello()");
			long startTime = System.currentTimeMillis();
			// warmup
			String[] r = sv.helloArray("01234567890 0123456789 0123456789 0123456789 0123456789"); //length = 54 char.
			long endTime = System.currentTimeMillis();
			System.out.printf("Done. %d msec\n", endTime - startTime);
//			System.out.printf("Result=%s\n", r);

		} finally{
			System.out.println("--- Finish!!! ---");
			factory.shutdown();//追加

		}
	}
}

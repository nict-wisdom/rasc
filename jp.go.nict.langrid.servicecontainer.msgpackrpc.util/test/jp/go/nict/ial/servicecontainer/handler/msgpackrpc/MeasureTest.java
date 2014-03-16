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

public class MeasureTest {
	public static void main(String[] args) throws Exception{
		MsgPackClientFactory factory = new MsgPackClientFactory();
		HelloService sv = factory.create(HelloService.class, new URL("http://172.21.20.77:19999"));
		try{
			// warmup
			sv.hello("01234567890 0123456789 0123456789 0123456789 0123456789"); //length = 54 char.
//			sv.hello("hello");
			Thread.sleep(60000);

			System.out.println("total. duration.");
			long sec = 60;
			long c = 0;
			long totalPer500 = 0;
			long total = 0;
			long e = sec * 1000 * 1000 * 1000;
			while(true){
				long s = System.nanoTime();
				sv.hello("01234567890 0123456789 0123456789 0123456789 0123456789"); //length = 54 char.
				long d = System.nanoTime() - s;
				c++;
				total += d;
				totalPer500 += d;
				if(c % 500 == 0){
					System.out.println(String.format(
							"%.2f times/sec.\t%.2f times/sec.\t%d total.",
							c * 1.0 / total * 1000 * 1000 * 1000,
							500.0 / totalPer500 * 1000 * 1000 * 1000,
							c
							));
					totalPer500 = 0;
				}
				if(total > e) break;
			}
			System.out.println("total requests: " + c);
			System.out.println("time(msec): " + total * 1000 * 1000);
			System.out.println(String.format(
					"%.2ftimes/msec.", c * 1.0 / sec));
		} finally{
			System.out.println("--- Finish!!! ---");
			factory.shutdown();//追加
		}
	}
}

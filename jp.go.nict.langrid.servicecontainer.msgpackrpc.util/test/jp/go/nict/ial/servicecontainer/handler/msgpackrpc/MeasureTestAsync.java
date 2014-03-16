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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import jp.go.nict.ial.mock.HelloService;
import jp.go.nict.langrid.client.msgpackrpc.MsgPackClientFactory;

public class MeasureTestAsync {
	public static void main(String[] args) throws Exception{

		MsgPackClientFactory factory = new MsgPackClientFactory();

		try{
			final HelloService sv = factory.create(HelloService.class, new URL("http://172.21.20.77:19999"));

			// warmup
			sv.hello("hello");
			sv.hello("hello");
			Thread.sleep(60000);
			final int threadNum = 20;
			System.out.println(String.format("thread.\t%d", threadNum));
			System.out.println("total.\tduration.");
			long sec = 60;
			long e = sec * 1000 * 1000 * 1000 + System.nanoTime();
			final AtomicLong total = new AtomicLong();
			final AtomicLong c = new AtomicLong();
			final Semaphore sem = new Semaphore(threadNum);
			ExecutorService service = Executors.newCachedThreadPool();
			while(true){
				sem.acquire();
				service.execute(new Runnable() {
					@Override
					public void run() {
						try{
							long s = System.nanoTime();
							//cli.callAsyncApply("hello", new Object[]{"hello"}).get();
							sv.hello("01234567890 0123456789 0123456789 0123456789 0123456789"); //length = 54 char.
							long sum = total.addAndGet(System.nanoTime() - s) / threadNum;
							long count = c.incrementAndGet();
							if(count % 500 == 0){
								System.out.println(String.format(
										"%.2f\ttimes/sec.\t%d\ttotal.",
										count * 1.0 / sum * 1000 * 1000 * 1000,
										count
										));
							}
						} finally{
							sem.release();
						}
					}
				});
				if(System.nanoTime() > e) break;
			}
			System.out.println("shutting down...");
			service.shutdown();
			service.awaitTermination(10, TimeUnit.SECONDS);
			System.out.println("total requests: " + c.get());
			System.out.println("time(msec): " + total.get() * 1000 * 1000);
			System.out.println(String.format(
					"%.2ftimes/sec.", c.get() * 1.0 / sec));
		} finally{
			System.out.println("--- Finish!!! ---");
			factory.shutdown();
			
		}
	}
}

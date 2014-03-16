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

package jp.go.nict.ial.mock;

import java.util.ArrayList;
import java.util.List;

import jp.go.nict.langrid.servicecontainer.executor.StreamingNotifier;
import jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver;

public class Hello implements HelloService, StreamingNotifier<Object> {

	public Hello() {
		System.out.println("new hello()");
	}

	@Override
	public String hello(String msg) {

		StreamingReceiver<Object> rcv = reciver.get();

		if (rcv != null) {
			for (int i = 0; i < 10000; i++) {
				String s = String.format("%d:%d:%s", i, System.currentTimeMillis(), Thread.currentThread().getName());
				rcv.receive(s);
			}
		}
		return msg;
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.iap.mock.HelloService#helloArray(java.lang.String)
	 */
	@Override
	public String[] helloArray(String msg) {

		List<String> results = new ArrayList<String>();

		StreamingReceiver<Object> rcv = reciver.get();

		for (int i = 0; i < 10000; i++) {
			String s = String.format("%d:%d:%s", i, System.currentTimeMillis(), Thread.currentThread().getName());
			if (rcv != null) {
				if (rcv.receive(s) == false) {
					results.add(s);
				}
			} else {
				results.add(s);
			}
		}
		return results.toArray(new String[] {});
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.iap.mock.HelloService#helloInt(java.lang.String)
	 */
	@Override
	public int[] helloInt(String msg) {
		List<Integer> results = new ArrayList<Integer>();

		StreamingReceiver<Object> rcv = reciver.get();

		for (int i = 0; i < 10000; i++) {
			if (rcv != null) {
				if (rcv.receive(i) == false) {
					results.add(i);
				}
			} else {
				results.add(i);
			}
		}

		Integer[] res = results.toArray(new Integer[] {});
		int[] result= new int[res.length];
		for(int i=0;i<res.length;i++){
			result[i] = res[i].intValue();
		}

		return result;
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.iap.mock.HelloService#helloJava(java.lang.String)
	 */
	@Override
	public MsgResult[] helloJava(String msg) {

		List<MsgResult> results = new ArrayList<MsgResult>();

		StreamingReceiver<Object> rcv = reciver.get();

		for (int i = 0; i < 10000; i++) {
			MsgResult mr = new MsgResult();
			mr.setMsg(msg);
			mr.setTime(System.currentTimeMillis());
			mr.setDataDummy(new DataDummy[]{
				new DataDummy(new double[]{0.001,0.22,0.33},Thread.currentThread().getName()),
				new DataDummy(new double[]{0.002,0.44,0.55},Thread.currentThread().getName()),
				new DataDummy(new double[]{0.005,0.66,0.77},Thread.currentThread().getName())
			});


			if (rcv != null) {
				if (rcv.receive(mr) == false) {
					results.add(mr);
				}
			} else {
				results.add(mr);
			}
		}
		return results.toArray(new MsgResult[] {});
	}

	@Override
	public String hello_error(String msg) throws Exception {
		System.out.println("hello_error(" + msg + ")");

		if (msg.contains("error")) {
			Exception e = new Exception("throw error");
			System.out.println(e.getMessage());
			throw e;
		}
		return msg;
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.langrid.servicecontainer.executor.StreamingNotifier#setReceiver(jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver)
	 */
	@Override
	public void setReceiver(StreamingReceiver<Object> receiver) {

		this.reciver.set(receiver);
		System.out.println(receiver);
	}

	private final ThreadLocal<StreamingReceiver<Object>> reciver = new ThreadLocal<StreamingReceiver<Object>>();
}

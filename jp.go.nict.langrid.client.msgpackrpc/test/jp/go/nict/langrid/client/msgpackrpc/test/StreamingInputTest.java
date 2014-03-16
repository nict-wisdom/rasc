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

package jp.go.nict.langrid.client.msgpackrpc.test;

import java.net.InetSocketAddress;
import java.util.Arrays;

import jp.go.nict.langrid.client.msgpackrpc.MsgPackClientFactory;

import org.junit.Test;

public class StreamingInputTest {
	@Test
	public void test() throws Exception{
		try(MsgPackClientFactory f = new MsgPackClientFactory()){
			StreamingInputHelloService service = f.create(
					StreamingInputHelloService.class, new InetSocketAddress("127.0.0.1", 19999));
			for(String s : service.hello(Arrays.asList("hello", "hello2"))){
				System.out.println(s);
			}
		}
	}
}

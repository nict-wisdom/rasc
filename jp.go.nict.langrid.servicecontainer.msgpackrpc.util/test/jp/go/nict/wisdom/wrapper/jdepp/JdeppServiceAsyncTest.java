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

package jp.go.nict.wisdom.wrapper.jdepp;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

import jp.go.nict.langrid.client.msgpackrpc.MsgPackClientFactory;
import jp.go.nict.wisdom.wrapper.api.TextAnalysisServiceAsync;

import org.junit.Test;

public class JdeppServiceAsyncTest {

	@Test
	public void test() throws Exception {
		MsgPackClientFactory factory = new MsgPackClientFactory();
		TextAnalysisServiceAsync sv = factory.create(TextAnalysisServiceAsync.class, new InetSocketAddress("localhost", 19999));

		Future<String> f = sv.analyzeAsync("解析のテスト");
		Future<String> f2 = sv.analyzeAsync("何度も実行");
		System.out.print(f.get());
		System.out.print(f2.get());

		factory.close();
	}
}

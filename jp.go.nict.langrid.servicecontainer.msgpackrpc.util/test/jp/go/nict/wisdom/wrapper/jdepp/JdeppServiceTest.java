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

import jp.go.nict.langrid.client.msgpackrpc.MsgPackClientFactory;
import jp.go.nict.wisdom.wrapper.api.TextAnalysisService;

import org.junit.Test;

public class JdeppServiceTest {

	@Test
	public void test() throws Exception {
		MsgPackClientFactory factory = new MsgPackClientFactory();
		TextAnalysisService sv = factory.create(TextAnalysisService.class, new InetSocketAddress("localhost", 19999));
		System.out.print(sv.analyze("解析のテスト"));
		System.out.print(sv.analyze("何度も実行"));
		factory.close();
	}
}

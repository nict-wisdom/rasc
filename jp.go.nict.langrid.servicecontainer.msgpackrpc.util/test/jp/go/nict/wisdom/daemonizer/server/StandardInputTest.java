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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import jp.go.nict.langrid.client.msgpackrpc.MsgPackClientFactory;
import jp.go.nict.wisdom.wrapper.api.TextAnalysisService;

public abstract class StandardInputTest {
	public static void main(String[] args) throws Exception{
		for(int i = 0; i < args.length; i++)
			System.out.println(args[i]);

		if(args.length != 2 && args.length != 3){
			System.out.println("Input args : [port] [filePath] (inputLog=On)");
			return;
		}

		MsgPackClientFactory factory = new MsgPackClientFactory();
		TextAnalysisService client = factory.create(TextAnalysisService.class, new InetSocketAddress("127.0.0.1", Integer.parseInt(args[0])));

		String filePath = args[1];
		BufferedReader br = null;
		try{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			StringBuffer sb = new StringBuffer();
			int c;
			while ((c = br.read()) != -1) {
				sb.append((char) c);
			}
			if(args.length == 3){
				System.out.println(sb.toString());
			}
			String ret = client.analyze(sb.toString());
			System.out.println(ret);
//			String str[] = {sb.toString()};
//			client.analyzeArray(str);
		} catch (Exception e) {
			System.out.println("---");
			System.out.println(e.getMessage());
			System.out.println("---");
		}finally{
			br.close();
		}
		factory.close();
	}
}

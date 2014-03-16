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

import java.net.MalformedURLException;
import java.net.URL;

import jp.go.nict.langrid.client.impl.protobuf.PbClientFactory;
import jp.go.nict.wisdom.wrapper.api.TextAnalysisService;


public class ClientProtcolBuffers {

	/**
	 * ProtocolBuffers クライアントサンプル.
	 * 
	 * @param args args[0] Mecabの解析にかける文字列
	 */
	public static void main(String[] args) {
		try {
			if(args.length <= 0){
				System.out.println("引数にMecabで解析する文字列を指定してください。");
				return;
			}
			
			
			TextAnalysisService s = new PbClientFactory().create(TextAnalysisService.class, new URL("http://localhost:8080/___WAR_NAME___/pbServices/___SERVICE_NAME___"));
			System.out.println(args[0]);
			System.out.println("--- result ---");
			System.out.println(s.analyze(args[0]));
			
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

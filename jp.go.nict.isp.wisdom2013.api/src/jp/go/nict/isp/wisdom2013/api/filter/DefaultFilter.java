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

/**
 *
 */
package jp.go.nict.isp.wisdom2013.api.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * DefaultFilterクラス.<br>
 * StringFilterの実装クラス、リストに含まれている物を除外する.
 * 
 * @author mori
 *
 */
public class DefaultFilter implements StringFilter{
	/*
	 *  stopwords.txtを読み込む
 	 * */
	/**
	 * コンストラクタ<br>
	 * stopwords.txtを読み込んで、リストを構築する
	 * 
	 */
	public DefaultFilter() {
		if(filteringWords == null){
			filteringWords = new ArrayList<String>();
			InputStream in;
			in = getClass().getResourceAsStream("/stopwords.txt");
			try {
				InputStreamReader isr = new InputStreamReader(in, "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					filteringWords.add(line);
				}
				br.close();
				isr.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * stopwords.txtに含まれる文字列が来た場合にTRUEを返却する
	 * */
	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.filter.StringFilter#filter(java.lang.String)
	 */
	public boolean filter(String str) {
		ListIterator<String> it = filteringWords.listIterator();
		while(it.hasNext()){
			if(str.contains(it.next()))
				return true;
		}

		return false;
	}

	private static List<String> filteringWords = null;
}

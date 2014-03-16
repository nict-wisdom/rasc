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

package jp.go.nict.isp.wisdom2013.api.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.trie4j.Trie;
import org.trie4j.doublearray.DoubleArray;
import org.trie4j.patricia.tail.TailPatriciaTrie;
import org.trie4j.tail.ConcatTailBuilder;

/**
 * TrieFilterクラス<br>
 * StringFilterの実装クラス、フィルタ用の文字列リストをTrie木構造で保持する実装
 * 
 * @author kishimoto
 * @see jp.go.nict.isp.wisdom2013.api.filter.StringFilter
 */
public class TrieFilter implements StringFilter{
	/**
	 * コンストラクタ.<br>
	 * 対象のリストをTrie構造に読み込む
	 */
	public TrieFilter() {
		InputStream is = getClass().getResourceAsStream("/stopwords.txt");
		try{
			Trie trie = new TailPatriciaTrie(new ConcatTailBuilder());
			try{
				BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String line = null;
				while((line = br.readLine()) != null){
					line = line.trim();
					if(line.length() == 0) continue;
					trie.insert(line);
				}
			} finally{
				is.close();
			}
			filteringWords = new DoubleArray(trie);
		} catch (IOException e) {
		}
	}

	/*
	 * stopwords.txtに含まれる文字列が来た場合にTRUEを返却する
	 * */
	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.filter.StringFilter#filter(java.lang.String)
	 */
	public boolean filter(String str) {
		return filteringWords.contains(str);
//		return filteringWords.findCommonPrefix(str, 0, str.length()) != -1;
	}

	private Trie filteringWords;
}

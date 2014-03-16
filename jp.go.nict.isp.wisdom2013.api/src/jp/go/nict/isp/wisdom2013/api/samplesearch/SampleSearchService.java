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

package jp.go.nict.isp.wisdom2013.api.samplesearch;

import jp.go.nict.langrid.commons.rpc.intf.Parameter;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

/**
 * サンプル検索サービスインターフェイスクラス.<br>
 * サンプル検索サービスを定義.サービスは本インターフェイスを実装する.
 * @author kishimoto
 *
 */
public interface SampleSearchService {
	/*
	 * 各Interfaceの引数には、@Parameter(name = "XXX") のアノテーションが必要
	 */

	/* 環境変数検索用 I/F getValues()*/
	/**
	 * サンプル検索サービス、検索メソッド1 <br>
	 * 検索キーに指定された、環境変数の値を取得して返す
	 * @param keys 検索キー
	 * @return 検索結果レコード
	 * @throws ProcessFailedException 
	 */
	public SampleSearchRecord[] getValues(@Parameter(name = "keys") final SampleSearchKey[] keys)
			throws ProcessFailedException;

	/* 環境変数検索用 特殊検索I/Fに類似の geValues2()*/
	/**
	 * サンプル検索サービス、検索メソッド2 <br>
	 * 検索キーに指定された、環境変数の値を取得して返す.検索結果の上限件数指定
	 * 
	 * @param keys 検索キー
	 * @param maxPerKey 
	 * @return 検索結果レコード
	 * @throws ProcessFailedException 
	 */
	public SampleSearchRecord[] getValues2(@Parameter(name = "keys") final SampleSearchKey[] keys,
			@Parameter(name = "maxPerKey") final int maxPerKey)
			throws ProcessFailedException;

	/* 環境変数検索用 カスタム ResultStrage使用の getValues3()*/
	/**
	 * サンプル検索サービス、検索メソッド3 <br>
	 * 検索キーに指定された、環境変数の値を取得して返す.<br>
	 * 検索結果レコードに関して、ソートを実施して返す.
	 * 
	 * @param keys 検索キー
	 * @return 検索結果レコード
	 * @throws ProcessFailedException 
	 */
	public SampleSearchRecord[] getValues3(@Parameter(name = "keys") final SampleSearchKey[] keys)
			throws ProcessFailedException;

}

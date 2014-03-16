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

package jp.go.nict.isp.wisdom2013.api.balancer;

import java.util.List;

/**
 * EndpointFactoryインターフェイスクラス.<br>
 * EndpointFactoryは本インターフェイスを実装する.EndPointリストを取得するファクトリーである.
 * 
 * @author kishimoto
 *
 */
public interface EndpointFactory {
	/**
	 * EndPointを生成(取得)する.
	 * @param defList デフォルトのEndPointリスト
	 * @return EndPointリスト
	 */
	public List<String> create(List<String> defList);

	/**
	 * EndPointを生成(取得)する.
	 * @param defList デフォルトのEndPointリスト
	 * @param sig 識別子
	 * @return EndPointリスト
	 */
	public List<String> create(List<String> defList, String sig);

	/**
	 * サービス配備先のパス情報を設定する.
	 * @param path パス情報
	 */
	public void setRealPath(String path);

	/**
	 * 識別子を設定する
	 * @param sig 識別子
	 */
	public void setSigName(String sig);

	/**
	 * 統計解析開始トリガー<br>
	 * バランサーを保持している場合には、バランサーのstartStatisticsを呼び出す。
	 * @param args 任意データ
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.EndpointBalancer
	 */
	public void startStatistics(Object... args);

	/**
	 * 統計解析終了トリガー<br>
	 * バランサーを保持している場合には、バランサーのendStatisticsを呼び出す。
	 * @param args 任意データ
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.EndpointBalancer
	 */
	public void endStatistics(Object... args);

}

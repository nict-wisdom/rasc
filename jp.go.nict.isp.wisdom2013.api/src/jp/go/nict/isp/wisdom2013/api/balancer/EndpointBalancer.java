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
 * EndpointBalancerインターフェイスクラス.<br>
 * バランサーは、インターフェイスを実装する.
 * 
 * @author kishimoto
 *
 */
public interface EndpointBalancer {

	/**
	 * バランサーアルゴリズムで選択された、EndPointリストを取得する.
	 * 
	 * @param list EndPointリストのリスト
	 * @param sig 識別子
	 * @return アルゴリズムで選択されたEndPointリスト
	 */
	public List<String> getList(List<List<String>> list, String sig);

	/**
	 * 統計解析開始メソッド(必要に応じて、EndpointFactoryから呼び出される)<br>
	 * バランサーで統計解析が必要な場合には、開始のトリガーとして利用する.
	 * 
	 * @param args 任意データ
	 */
	public void startStatistics(Object... args);

	/**
	 * 統計解析終了メソッド(必要に応じて、EndpointFactoryから呼び出される)<br>
	 * バランサーで統計解析が必要な場合には、終了のトリガーとして利用する.
	 * 
	 * @param args 任意データ
	 */
	public void endStatistics(Object... args);

}

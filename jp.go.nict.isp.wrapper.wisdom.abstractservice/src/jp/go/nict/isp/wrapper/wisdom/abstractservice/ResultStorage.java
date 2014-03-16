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

package jp.go.nict.isp.wrapper.wisdom.abstractservice;

import java.util.List;

/**
 * ResultStorageインターフェイスクラス.<br>
 * サービス連携フレームワーク、サーバ側検索結果用インターフェイスクラス.<br>
 * フレームワークではワーカーからの結果をResultStorage経由で操作する。
 * @author kishimoto
 *
 */
public interface ResultStorage<V> {
	/**
	 * 検索結果をストレージに格納する
	 * @param result 検索結果
	 * @return true:終了条件に到達(検索件数の上限到達など) false:処理を継続
	 */
	public boolean pushResult(V result);

	/**
	 * ストレージから検索結果を取り出す
	 * @return 検索結果
	 */
	public List<V> getResult();

	/**
	 * ストリーミングでの結果通知に失敗したデータをストレージに格納する
	 * @param result 検索結果
	 */
	public void pushError(V result);

	/**
	 * ストリーミングでの結果通知に失敗した検索結果を取得する
	 * @return 検索結果
	 */
	public List<V> getError();
}

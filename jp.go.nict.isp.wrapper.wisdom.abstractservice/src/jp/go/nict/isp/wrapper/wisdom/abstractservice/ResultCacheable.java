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

/**
 * ResultCacheableインターフェイスクラス.<br>
 * ResultStorageに独自の結果キャッシュ機構を用意する場合に、本インターフェイスを実装する。
 * 
 */
public interface ResultCacheable<V> {
	/**
	 * キャッシュヒットチェック
	 * @return true:キャッシュにヒット/false:ヒットしなかった
	 */
	public boolean findCache();
	/**
	 * キャッシュに結果を積み込む
	 * @param hashKey 検索用のハッシュキー
	 * @param values 結果レコード
	 */
	public void pushCache(final String hashKey, final V[] values);
	/**
	 * キャッシュ機構が有効かチェック
	 * @return true:有効/false:無効
	 */
	public boolean isEnable();

}

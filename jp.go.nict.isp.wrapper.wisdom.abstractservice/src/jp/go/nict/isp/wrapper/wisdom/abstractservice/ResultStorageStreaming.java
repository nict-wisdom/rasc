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
 * ResultStorageStreamingインターフェイスクラス.<br>
 * 結果ストレージ用のストリーミングインターフェイスクラス.<br>
 * ストレージがストリーミングによる結果通知に対応している場合には、本インスタンスを実装する.
 * 
 * @author kishimoto
 *
 */
public interface ResultStorageStreaming<V> {
	
	/**
	 * 結果と同じレコードを処理済みかチェックする
	 * @param result 結果レコード
	 * @return true:処理済み、false:未処理
	 */
	public boolean contains(V result);

	/**
	 * 結果レコードをフィルタチェックする
	 * @param result 結果レコード
	 * @return ture:フィルタ対象/false:フィルタ対象ではない
	 */
	public boolean checkFilter(V result);
}

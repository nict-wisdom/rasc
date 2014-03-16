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
 * 低レベルレイヤーラッパー用レシーバインタフェースクラス.<br>
 * サービス連携フレームワークのワーカーモジュールと低レベルレイヤー呼び出しラッパーは
 * 本インターフェイスを経由して結果通知を行う。
 * @author kishimoto
 *
 */
public interface ResourceApiWrapperReceiver<V> {

	/**
	 * 結果を通知する
	 * @param value 結果
	 */
	public void receiveNotify(V value);

}

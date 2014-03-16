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
 * 低レベルレイヤー呼び出しラッパーベースクラス.<br>
 * ワーカー側の低レベルレイヤー用の呼び出しラッパーは、本クラスから派生して実装する。
 * @author kishimoto
 *
 */
public class ResourceApiWrapperBase{

	/**
	 * ラッパークラス名を取得する
	 * @return ラッパークラス名
	 */
	public String getResourceApiWrapperName(){
		return this.getClass().getName();
	}

}

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

/**
 * 文字列フィルターインターフェイスクラス.<br>
 * 文字列フィルタークラスは、本インターフェイスを実装する.<br>
 * 文字列を対象としたフィルタリング
 * 
 * @author kishimoto
 *
 */
public interface StringFilter {
	/**
	 * フィルタリングを実施する
	 * @param str 文字列
	 * @return フィルタリング結果 true:フィルタリング対象/false:対象外
	 */
	boolean filter(String str);
}

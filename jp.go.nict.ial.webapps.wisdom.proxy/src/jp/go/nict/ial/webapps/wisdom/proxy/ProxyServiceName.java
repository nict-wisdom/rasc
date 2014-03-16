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

package jp.go.nict.ial.webapps.wisdom.proxy;

/**
 * Proxyサービス用、ServiceName操作用インターフェイスクラス.
 * 
 * @author kishimoto
 *
 */
public interface ProxyServiceName {

	/**
	 * Proxy用サービス名を設定する
	 * @param name 設定するサービス名
	 */
	public void setServiceName(String name);

	/**
	 * Proxy用サービス名を取得する
	 * @return サービス名
	 */
	public String getServiceName();

}

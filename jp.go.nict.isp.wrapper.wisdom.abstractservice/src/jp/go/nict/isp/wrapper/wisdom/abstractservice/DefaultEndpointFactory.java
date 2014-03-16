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

import jp.go.nict.isp.wisdom2013.api.balancer.AbstractEndpointFactory;

/**
 * デフォルトのエンドポイントファクトリ実装クラス.<br>
 * AbstractEndpointFactoryの派生クラス,デフォルトのエンドポイントをそのまま返す。
 * @author kishimoto
 *
 */
public class DefaultEndpointFactory extends AbstractEndpointFactory {

	/**
	 * コンストラクタ
	 */
	public DefaultEndpointFactory() {
		super();
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.AbstractEndpointFactory#create(java.util.List, java.lang.String)
	 */
	@Override
	public List<String> create(List<String> defList,String sig) {
		return defList;
	}

}

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
 * EndpointFactoryクラスを実装した抽象クラス.<br>
 * 最低限必要なEndpointFactoryを実装.
 * 
 * @author kishimoto
 * @see jp.go.nict.isp.wisdom2013.api.balancer.EndpointFactory
 */
public abstract class AbstractEndpointFactory implements EndpointFactory {

	private String realPath = "";

	private String sigName = "";

	protected String balancer = null;

	protected EndpointBalancer eb = null;

	/**
	 * コンストラクタ
	 */
	public AbstractEndpointFactory() {

	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.EndpointFactory#create(java.util.List)
	 */
	@Override
	public List<String> create(List<String> defList) {
		return create(defList, getSigName());
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.EndpointFactory#create(java.util.List, java.lang.String)
	 */
	@Override
	public abstract List<String> create(List<String> defList, String sig);

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.EndpointFactory#endStatistics(java.lang.Object[])
	 */
	@Override
	public void endStatistics(Object... args) {
		/* EndPoint切り替えに利用する統計処理開始 */
		/* 必要があれば、オーバーライドする  */
		if (eb != null) {
			eb.endStatistics(args);
		}

	}

	/**
	 * サービスを配備しているパス情報を取得する
	 * @return サービス配備位置のパス情報
	 */
	public String getRealPath() {
		return realPath;
	}

	/**
	 * EndPointリスト識別子を取得する。
	 * @return EndPointリスト識別子
	 */
	public String getSigName() {
		return sigName;
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.EndpointFactory#setRealPath(java.lang.String)
	 */
	@Override
	public void setRealPath(String path) {
		realPath = path;

	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.EndpointFactory#setSigName(java.lang.String)
	 */
	@Override
	public void setSigName(String sig) {
		sigName = sig;

	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.EndpointFactory#startStatistics(java.lang.Object[])
	 */
	@Override
	public void startStatistics(Object... args) {
		/* EndPoint切り替えに利用する統計処理開始 */
		/* 必要があれば、オーバーライドする  */
		if (eb != null) {
			eb.startStatistics(args);
		}
	}

	/**
	 * バランサーを取得する
	 * @return バランサーのインスタンス、nullの場合には、未設定
	 */
	public String getBalancer() {
		return balancer;
	}

	/**
	 * バランサーを設定する
	 * @param balancer バランサーのインスタンス
	 */
	public void setBalancer(String balancer) {
		this.balancer = balancer;
	}

}

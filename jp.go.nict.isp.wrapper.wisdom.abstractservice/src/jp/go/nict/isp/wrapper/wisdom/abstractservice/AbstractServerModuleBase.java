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

import jp.go.nict.isp.wisdom2013.api.balancer.EndpointFactory;
import jp.go.nict.isp.wisdom2013.api.endpoint.EndpointListDistribution;
import jp.go.nict.langrid.client.ClientFactory;
import jp.go.nict.langrid.servicecontainer.executor.StreamingNotifier;
import jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver;

/**
 * サービス連携、サーバサービス用ベースクラス.<br>
 * サービス連携サービスを実装するサーバクラスは、本クラスより派生する。
 * @author kishimoto
 *
 */
public class AbstractServerModuleBase implements StreamingNotifier<Object> {
	/* field */
	private List<String> endpointList = null; // endpoint list
	private int waitTimeOut = 300; // タイムアウト設定 デフォルト 300 秒
	private String comparator = null;
	private String filter = null;
	private String sort = null;
	private String distributor = null;
	private ClientFactory clientFactory = null;
	private EndpointListDistribution dist = null;
	private ThreadLocal<StreamingReceiver<Object>> stream_receiver = new ThreadLocal<StreamingReceiver<Object>>();
	private String balancer = null;
	private EndpointFactory endpointFactory=null;

	/**
	 * コンストラクタ
	 */
	public AbstractServerModuleBase() {
		stream_receiver.remove();
	}

	/**
	 * バランサークラス名を取得する
	 * @return バランサークラス名
	 */
	public String getBalancer() {
		return balancer;
	}

	/**
	 * クライアントファクトリークラス名を取得
	 * @return クライアントファクトリークラス名
	 */
	public ClientFactory getClientFactory() {
		return clientFactory;
	}

	/**
	 * コンパレータクラス名を取得
	 * @return コンパレータクラス名
	 */
	public String getComparator() {
		return comparator;
	}

	public String getDistributor() {
		return distributor;
	}

	/**
	 * エンドポイントファクトリークラスを取得
	 * @return エンドポイントファクトリークラス
 	 */
	public EndpointFactory getEndpointFactory() {
		return endpointFactory;
	}

	/**
	 * エンドポイントリストを取得する
	 * @return エンドポイントリスト
	 */
	public List<String> getEndpointList() {
		return endpointList;
	}

	public EndpointListDistribution getEndpointListDistribution() {
		if (dist == null && distributor != null) {
			try {
				Class<?> clazz = Class.forName(distributor);
				dist = (EndpointListDistribution) clazz.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return dist;
	}

	/**
	 * フィルタークラス名を取得
	 * @return フィルタークラス名
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * レシーバクラスを取得
	 * @return レシーバクラス
	 */
	public StreamingReceiver<Object> getReceiver() {
		return stream_receiver.get();
	}

	/**
	 * ソートクラス名を取得
	 * @return ソートクラス名
	 */
	public String getSort() {
		return sort;
	}

	/**
	 * タイムアウト値を取得
	 * @return タイムアウト値
	 */
	public int getWaitTimeOut() {
		return waitTimeOut;
	}

	/**
	 * ストリーミング可否チェック
	 * @return true:可 / false:否
	 */
	public boolean isStreamingReady() {
		return stream_receiver.get() != null;
	}

	/**
	 * バランサークラス名を設定
	 * @param balancer バランサークラス名
	 */
	public void setBalancer(String balancer) {
		this.balancer = balancer;
	}

	/**
	 * クライアントファクトリーを設定
	 * @param clientFactory クライアントファクトリー
	 */
	public void setClientFactory(ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	/**
	 * コンパレータクラス名を設定する
	 * @param comparator コンパレータクラス名
	 */
	public void setComparator(String comparator) {
		this.comparator = comparator;
	}

	public void setDistributor(String distributor) {
		this.distributor = distributor;
	}

	/**
	 * エンドポイントファクトリークラスを設定する
	 * @param endpointFactory エンドポイントファクトリークラス
	 */
	public void setEndpointFactory(EndpointFactory endpointFactory) {
		System.out.println(endpointFactory.getClass().getName());
		this.endpointFactory = endpointFactory;
	}

	/**
	 * エンドポイントリストを設定する
	 * @param endpointList エンドポイントリスト
	 */
	public void setEndpointList(List<String> endpointList) {
		System.out.printf("<%d> ==setEndpointList()== \n", System.currentTimeMillis());
		this.endpointList = endpointList;
	}

	/**
	 * フィルタークラス名を設定する
	 * @param filter フィルタークラス名
	 */
	public void setFilter(String filter) {
		this.filter = filter;
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.langrid.servicecontainer.executor.StreamingNotifier#setReceiver(jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver<T>)
	 */
	@Override
	public void setReceiver(StreamingReceiver<Object> receiver) {
		if (receiver != null) {
			stream_receiver.set(receiver);
		} else {
			stream_receiver.remove();
		}
	}

	/**
	 * ソートクラス名を設定する
	 * @param sort ソートクラス名
	 */
	public void setSort(String sort) {
		this.sort = sort;
	}

	/**
	 * タイムアウト値を設定する
	 * @param waitTimeOut タイムアウト値
	 */
	public void setWaitTimeOut(int waitTimeOut) {
		this.waitTimeOut = waitTimeOut;
	}
}

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

package jp.go.nict.isp.wisdom2013.plugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jp.go.nict.isp.wisdom2013.api.balancer.EndpointBalancer;

/**
 * RoundrobinBalancerImplクラス.<br>
 * EndpointBalancerの実装クラス、簡単なラウンドロビンでEndPointを選択する実装。
 * @author kishimoto
 *
 */
public class RoundrobinBalancerImpl implements EndpointBalancer {

	private final static RoundrobinBalancerImpl classInstance = new RoundrobinBalancerImpl();
	private final static Map<String, AtomicInteger> roundrobin = new ConcurrentHashMap<String, AtomicInteger>();

	/**
	 * コンストラクタ（隠蔽)
	 */
	private RoundrobinBalancerImpl() {

	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.EndpointBalancer#getList(java.util.List, java.lang.String)
	 */
	@Override
	public List<String> getList(List<List<String>> list, String sig) {

		int index = 0;
		if (!roundrobin.containsKey(sig)) {
			roundrobin.put(sig, new AtomicInteger(0));
		}
		int range = roundrobin.get(sig).getAndIncrement();

		if ((range < 0) || (range >= Integer.MAX_VALUE)) {
			roundrobin.get(sig).set(0);
		}

		index = range % list.size();

		return list.get(index);
	}

	/**
	 * インスタンスを返す
	 * @return RoundrobinBalancerImplのインスタンス
	 */
	public static final EndpointBalancer getInstance() {
		return classInstance;
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.EndpointBalancer#startStatistics(java.lang.Object[])
	 */
	@Override
	public void startStatistics(Object... args) {

	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.EndpointBalancer#endStatistics(java.lang.Object[])
	 */
	@Override
	public void endStatistics(Object... args) {

	}

}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.go.nict.isp.wisdom2013.api.filter.ResultRecordSort;

/**
 * ResultStorageMultiKeyクラス.<br>
 * ResultStorageBaseの派生クラス、キーが複数あるサービス用のストレージ<br>
 * キー毎の検索結果上限に対応した実装.
 * 
 * @author kishimoto
 *
 */
public abstract class ResultStorageMultiKey<V,K> extends ResultStorageBase<V>{

	private final Map<K, Set<V>> workerResult;
	private final Map<K, Boolean> hashEndCheck;
	private final K[] keys;
	private final int maxPerKey;

	/**
	 * コンストラクタ
	 * 
	 * @param serverBase サーバベースオブジェクト
	 * @param arrayResultType 結果型
	 * @param keys キー配列
	 * @param maxPerKey 検索結果の上限数
	 */
	public ResultStorageMultiKey(final AbstractServerModuleBase serverBase, final V[] arrayResultType,final K[] keys,final int maxPerKey) {
		super(serverBase,arrayResultType);
		this.workerResult = new HashMap<K, Set<V>>(keys.length);
		this.hashEndCheck = new HashMap<K, Boolean>(keys.length);
		this.keys = keys;
		this.maxPerKey = maxPerKey;

		for(K key : keys){
			workerResult.put(key, new HashSet<V>());
			hashEndCheck.put(key,new Boolean(false));
		}
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultStorageBase#pushResult(null)
	 */
	@Override
	public boolean pushResult(final V result) {
		/* 監視スレッドからのみアクセスなので、ロック処理は不要 */
		final K resultKey = getKey(result);
		final Set<V> resultset = workerResult.get(resultKey);
		boolean isAllEnd = false;

		if (maxPerKey <= -1) {
			/* 件数無制限 */
			if (filter != null) {
				if (filter.filter(result)) {
					return isAllEnd;
				}
			}
			resultset.add(result);
		} else {
			/* 数に制限あり */
			Boolean check = hashEndCheck.get(resultKey);
			if (check.booleanValue() == false) {
				if (resultset.size() < maxPerKey) {
					if (filter != null) {
						if (filter.filter(result)) {
							return isAllEnd;
						}
					}
					resultset.add(result);
				} else {
					hashEndCheck.put(resultKey, new Boolean(true));
				}
			} else {
				/* 全件終了チェック */

				boolean all_end = true;

				for (Iterator<Boolean> it = hashEndCheck.values().iterator(); it.hasNext();) {
					if (it.next() == false) {
						all_end = false;
					}
				}

				/* 全KEY終了条件 */
				if (all_end) {
					System.out.println("== All-Keys Up to maxPerKey == ! ");
					isAllEnd = true;
				}
			}
		}
		return isAllEnd;
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultStorageBase#getResult()
	 */
	@Override
	public List<V> getResult() {
		List<V> resultList = new ArrayList<V>();
		ResultRecordSort<V> sorter = loadSorter();

		/* 集約 */
		for(K curKey : keys ){
			Set<V> work = workerResult.get(curKey);
			resultList.addAll(work);
		}

		if (sorter != null) {
			V[] sorted = sorter.sort(resultList.toArray(arrayResultType));
			resultList.clear();
			resultList.addAll(Arrays.asList(sorted));
		}
		return resultList;
	}


	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultStorageBase#contains(null)
	 */
	@Override
	public boolean contains(V result) {

		Set<V> queue = workerResult.get(getKey(result));
		return queue.contains(result);
	}

	/**
	 * 結果レコードから検索キーを取得する.
	 * 
	 * @param result 結果レコード
	 * @return 検索キー
	 */
	protected abstract K getKey(V result);


}

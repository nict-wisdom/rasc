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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ResultCacheEngineクラス.<br>
 * Mapを使用したオンメモリのキャッシュクラス実装サンプルクラス.
 * @author kishimoto
 *
 */
public class ResultCacheEngine {
	private final Map<String, CacheRecord> mapCache;
	private static final ThreadLocal<ResultCacheEngine> gcSafe = new ThreadLocal<ResultCacheEngine>();
	private static final ResultCacheEngine myInstance = new ResultCacheEngine();

	static {
		gcSafe.set(myInstance);
	}

	/**
	 * キャッシュ格納用レコードクラス.
	 * @author kishimoto
	 *
	 */
	public class CacheRecord {

		private final long cacheTime;
		private final List<?> values;

		/**
		 * 結果リストを取得する
		 * @return 結果リスト
		 */
		public List<?> getValues() {
			return values;
		}

		/**
		 * コンストラクタ
		 * @param values 結果リスト
		 */
		public CacheRecord(List<?> values) {
			this.cacheTime = System.currentTimeMillis();
			this.values = values;
		}

		/**
		 * キャッシュ期限切れチェック
		 * @return true:期限切れ / false:期限内
		 */
		public final boolean isExpire() {
			return (System.currentTimeMillis() - cacheTime) > 43200000;
		}
	}

	/**
	 * コンストラクタ
	 */
	private ResultCacheEngine() {
		/* QUEに残すSession数 */
		
		final int MAX_QUEMAPSIZE = 3000;// ひとまず、3000

		/* LinkedHashMap の FIFO実装を使う */
		mapCache = new LinkedHashMap<String, CacheRecord>(MAX_QUEMAPSIZE) {

			@Override
			protected boolean removeEldestEntry(Entry<String, CacheRecord> eldest) {
				return size() > MAX_QUEMAPSIZE;
			}

			private static final long serialVersionUID = 1L;

		};

	}

	/**
	 * インスタンスを取得する
	 * @return インスタンス
	 */
	public static ResultCacheEngine getInstance() {
		return myInstance;
	}

	/**
	 * キャッシュから結果を取得する
	 * @param hashKey 検索用ハッシュキー
	 * @return 検索結果 nullの場合には、キャッシュヒットなし
	 */
	@SuppressWarnings("unchecked")
	public <V> List<V> getCache(String hashKey) {

		List<V> results = null;
		synchronized (mapCache) {

			if (mapCache.containsKey(hashKey)) {
				if (mapCache.get(hashKey).isExpire()) {
					//expire は削除する
					mapCache.remove(hashKey);
				} else {
					results = (List<V>) mapCache.get(hashKey).getValues();
				}
			}
		}
		return results;
	}

	/**
	 * キャッシュに検索結果を格納
	 * @param hashKey キャッシュ用ハッシュキー
	 * @param results 検索結果
	 */
	public <V> void pushCache(String hashKey, V[] results) {
		synchronized (mapCache) {
			if (!mapCache.containsKey(hashKey)) {
				List<V> list = new ArrayList<V>();
				list.addAll(Arrays.asList(results));
				mapCache.put(hashKey, new CacheRecord(list));
			}
		}
	}

}

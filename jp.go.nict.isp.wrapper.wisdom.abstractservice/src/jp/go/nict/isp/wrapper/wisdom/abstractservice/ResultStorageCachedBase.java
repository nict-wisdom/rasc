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

/**
 * ResultStorageCachedBaseクラス.<br>
 * ResultStorageBaseクラスを拡張し、ResultCacheableを実装したキャッシュ付きストレージのサンプル実装.
 * @author kishimoto
 *
 */
public abstract class ResultStorageCachedBase<V> extends ResultStorageBase<V> implements ResultCacheable<V> {

	/**
	 * コンストラクタ
	 * @param serverBase 
	 * @param arrayResultType 
	 */
	public ResultStorageCachedBase(AbstractServerModuleBase serverBase, V[] arrayResultType) {
		super(serverBase, arrayResultType);
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultCacheable#findCache()
	 */
	@Override
	public boolean findCache() {

		ResultCacheEngine rec = ResultCacheEngine.getInstance();
		List<V> results = rec.getCache(getHashKey());
		if (results == null) {
			resultSet.clear();
			return false;
		}
		resultSet.clear();
		resultSet.addAll(results);
		return true;
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultCacheable#pushCache(java.lang.String, null[])
	 */
	@Override
	public void pushCache(String hashKey, V[] values) {
		ResultCacheEngine rec = ResultCacheEngine.getInstance();
		rec.pushCache(hashKey, values);
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultStorageBase#getResult()
	 */
	@Override
	public List<V> getResult() {
		/* キャッシュに積み込み */
		if (isEnable()) {
			pushCache(getHashKey(), resultSet.toArray(arrayResultType));
		}
		return super.getResult();
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultCacheable#isEnable()
	 */
	@Override
	public boolean isEnable() {
		return ResultCacheEngine.getInstance() != null;
	}

	/* key及びmaxPerkeyなどからのhashKeyを生成する*/
	/**
	 * ハッシュキー取得
	 * @return ハッシュキー
	 */
	public abstract String getHashKey();

}

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jp.go.nict.isp.wisdom2013.api.filter.ResultRecordFilter;
import jp.go.nict.isp.wisdom2013.api.filter.ResultRecordSort;

/**
 * ResultStorageBaseクラス.<BR>
 * ResultStorage、ResultStorageStreamingの実装クラス.<BR>
 * ストレージクラスの実装クラスである、独自のストレージクラスを実装する場合には、本クラスから派生して実装する.<BR>
 * 本クラスでは、サービス連携での検索結果の保持や、フィルタリング、ソートなどの機構を実装する。
 * 
 * @author kishimoto
 *
 */
public class ResultStorageBase<V> implements ResultStorage<V>, ResultStorageStreaming<V> {

	protected final Set<V> resultSet;
	protected final AbstractServerModuleBase serverBase;
	protected final static ThreadLocal<Map<String, Comparator<?>>> safeComparator = new ThreadLocal<Map<String, Comparator<?>>>();
	protected final static ThreadLocal<Map<String, ResultRecordSort<?>>> safeSoter = new ThreadLocal<Map<String, ResultRecordSort<?>>>();
	protected final static ThreadLocal<Map<String, ResultRecordFilter<?>>> safeFilter = new ThreadLocal<Map<String, ResultRecordFilter<?>>>();
	protected final V[] arrayResultType;
	protected final ResultRecordFilter<V> filter;
	protected final List<V> errorResult;

	/**
	 * コンストラクタ
	 * @param serverBase サーバーベースオブジェクト
	 * @param arrayResultType 検索結果の結果型
	 */
	public ResultStorageBase(final AbstractServerModuleBase serverBase, final V[] arrayResultType) {

		this.serverBase = serverBase;
		this.arrayResultType = arrayResultType;
		this.resultSet = (serverBase.getComparator() != null) ? new TreeSet<V>(loadComparator())
				: new LinkedHashSet<V>();
		this.filter = loadFilter();
		this.errorResult = new ArrayList<V>();
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultStorage#pushResult(null)
	 */
	@Override
	public boolean pushResult(final V result) {
		/* 監視スレッドからのみアクセスなので、ロック処理は不要 */
		/**
		 * Reply()の場合には、件数無制限かつ,フィルターを実施する。
		 *
		 *
		 */
		if (filter != null) {
			if (filter.filter(result)) {
				return false;
			}
		}

		/* 件数無制限 */
		resultSet.add(result);
		return false;
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultStorage#getResult()
	 */
	@Override
	public List<V> getResult() {
		List<V> resultList = new ArrayList<V>();
		ResultRecordSort<V> sorter = loadSorter();

		if (sorter != null) {
			V[] sorted = sorter.sort(resultSet.toArray(arrayResultType));
			resultSet.clear();
			resultSet.addAll(Arrays.asList(sorted));
		}
		resultList.addAll(resultSet);
		return resultList;
	}

	/**
	 * コンパレータをロードする.
	 * @return コンパレータオブジェクト(nullの場合には、設定なし)
	 */
	@SuppressWarnings("unchecked")
	protected Comparator<V> loadComparator() {
		if (serverBase.getComparator() != null) {

			Map<String, Comparator<?>> mapComparator = null;
			Comparator<V> comparator = null;

			if (safeComparator.get() == null) {
				mapComparator = new HashMap<String, Comparator<?>>();

			} else {
				mapComparator = safeComparator.get();
			}

			if (mapComparator.containsKey(serverBase.getComparator()) == false) {

				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				Class<?> classz;
				System.out.printf("Comparator load(%s)\n", serverBase.getComparator());

				try {
					classz = loader.loadClass(serverBase.getComparator());
					comparator = (Comparator<V>) classz.newInstance();
					mapComparator.put(serverBase.getComparator(), comparator);
					safeComparator.set(mapComparator);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

			} else {
				System.out.printf("use cache comparator\n");
				comparator = (Comparator<V>) mapComparator.get(serverBase.getComparator());
			}

			return comparator;
		} else {
			return null;
		}
	}

	/**
	 * ソーターをロードする.
	 * @return ソーターオブジェクト(nullの場合には、設定なし）
	 * 
	 */
	@SuppressWarnings("unchecked")
	protected ResultRecordSort<V> loadSorter() {
		if (serverBase.getSort() != null) {

			Map<String, ResultRecordSort<?>> mapSort = null;
			ResultRecordSort<V> sorter = null;

			if (safeSoter.get() == null) {
				mapSort = new HashMap<String, ResultRecordSort<?>>();
			} else {
				mapSort = safeSoter.get();
			}

			if (mapSort.containsKey(serverBase.getSort()) == false) {
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				Class<?> classz;
				System.out.printf("Sorter load(%s)\n ", serverBase.getSort());

				try {
					classz = loader.loadClass(serverBase.getSort());
					sorter = (ResultRecordSort<V>) classz.newInstance();
					mapSort.put(serverBase.getSort(), sorter);
					safeSoter.set(mapSort);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

			} else {
				sorter = (ResultRecordSort<V>) mapSort.get(serverBase.getSort());
				System.out.printf("use cache sorter\n");
			}
			return sorter;

		} else {
			return null;
		}
	}

	/**
	 * フィルターをロードする.
	 * @return フィルターオブジェクト(nullの場合には、設定なし)
	 */
	@SuppressWarnings("unchecked")
	protected ResultRecordFilter<V> loadFilter() {
		if (serverBase.getFilter() != null) {

			Map<String, ResultRecordFilter<?>> mapFilter = null;
			ResultRecordFilter<V> filter = null;

			if (safeFilter.get() == null) {
				mapFilter = new HashMap<String, ResultRecordFilter<?>>();
			} else {
				mapFilter = safeFilter.get();
			}

			if (mapFilter.containsKey(serverBase.getFilter()) == false) {
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				Class<?> classz;
				System.out.printf("filter load(%s)\n ", serverBase.getFilter());

				try {
					classz = loader.loadClass(serverBase.getFilter());
					filter = (ResultRecordFilter<V>) classz.newInstance();
					mapFilter.put(serverBase.getFilter(), filter);
					safeFilter.set(mapFilter);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

			} else {
				filter = (ResultRecordFilter<V>) mapFilter.get(serverBase.getFilter());
				System.out.printf("use cache filter\n");
			}
			return filter;
		} else {
			return null;
		}

	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultStorageStreaming#contains(null)
	 */
	@Override
	public boolean contains(V result) {
		return resultSet.contains(result);
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultStorageStreaming#checkFilter(null)
	 */
	@Override
	public boolean checkFilter(V result) {
		if (filter != null) {
			return filter.filter(result);
		}
		return false;
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultStorage#pushError(null)
	 */
	@Override
	public void pushError(V result) {
		errorResult.add(result);

	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultStorage#getError()
	 */
	@Override
	public List<V> getError() {
		return errorResult;
	}
}

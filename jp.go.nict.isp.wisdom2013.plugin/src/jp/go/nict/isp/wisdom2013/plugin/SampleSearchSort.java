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

import java.util.Arrays;
import java.util.Comparator;

import jp.go.nict.isp.wisdom2013.api.filter.ResultRecordSort;
import jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchRecord;


/**
 * サンプル検索サービス用の検索結果ソートの実装クラス.<br>
 * ResultRecordSortの実装クラス.検索結果を降順にソートする実装.
 * 
 * @author kishimoto
 *
 */
public class SampleSearchSort implements ResultRecordSort<SampleSearchRecord> {

	/* Arraysのsort()を使ってソート */
	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.filter.ResultRecordSort#sort(null[])
	 */
	@Override
	public SampleSearchRecord[] sort(SampleSearchRecord[] records) {

		/* sort()実行 */
		Arrays.sort(records, new Comparator<SampleSearchRecord>() {

			/* Comparatorを匿名クラスで生成する */
			@Override
			public int compare(SampleSearchRecord o1, SampleSearchRecord o2) {

				/* nullは最小値として、文字列順で降順にソート */
				if ((o1.getValue() == null) && (o2.getValue() == null)) {
					return 0;
				} else if (o1.getValue() == null) {
					return 1;
				} else if (o2.getValue() == null) {
					return -1;
				} else if (o1.getValue().compareTo(o2.getValue()) > 0) {
					return -1;
				} else if (o1.getValue().compareTo(o2.getValue()) < 0) {
					return 1;
				}
				return 0;
			}
		});

		return records;
	}
}

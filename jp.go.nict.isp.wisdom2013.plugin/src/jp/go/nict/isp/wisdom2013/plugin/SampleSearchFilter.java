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

import jp.go.nict.isp.wisdom2013.api.filter.ResultRecordFilter;
import jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchRecord;


/**
 * サンプル検索サービス用結果レコードフィルタリング実装クラス.<br>
 * サンプル検索サービスのフィルタリングを実装する.
 * @author kishimoto
 *
 */
public class SampleSearchFilter implements ResultRecordFilter<SampleSearchRecord> {

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.filter.ResultRecordFilter#filter(null)
	 */
	@Override
	public boolean filter(SampleSearchRecord record) {
		/* filter()を実装
		 * ture:フィルタリング対象
		 * false:フィルタリング非対象
		 * */

		/* null は除外 */
		return (record.getValue() == null);
	}
}

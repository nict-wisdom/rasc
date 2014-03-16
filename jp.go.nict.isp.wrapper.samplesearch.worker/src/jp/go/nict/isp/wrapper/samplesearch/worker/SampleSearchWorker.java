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

/**
 *
 */
package jp.go.nict.isp.wrapper.samplesearch.worker;

import jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchKey;
import jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchRecord;
import jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchService;
import jp.go.nict.isp.wrapper.wisdom.abstractservice.AbstractWorkerModule;
import jp.go.nict.isp.wrapper.wisdom.abstractservice.AbstractWorkerModuleBase;
import jp.go.nict.isp.wrapper.wisdom.abstractservice.ResourceApiWrapperReceiver;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

/**
 * サンプル検索サービスのワーカー側の実装クラス.<br>
 * AbstractWorkerModuleBaseから派生して、実装する。<br>
 * SampleSearchServiceを実装する,サービス連携フレームワークを使用して実装する.
 *
 */
public class SampleSearchWorker extends AbstractWorkerModuleBase implements SampleSearchService {

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchService#getValues(jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchKey[])
	 */
	@Override
	public SampleSearchRecord[] getValues(final SampleSearchKey[] keys) throws ProcessFailedException {

		/* ワーカーモジュール生成 */
		AbstractWorkerModule<SampleSearchRecord, SampleSearchResourceApiWrapper> worker =
				new AbstractWorkerModule<SampleSearchRecord, SampleSearchResourceApiWrapper>(
						SampleSearchResourceApiWrapper.getInstance(), new SampleSearchRecord[] {}, "SampleSearch", this) {

					/* ResourceAPI呼び出しを実装する */
					@Override
					protected void callResourceApi(SampleSearchResourceApiWrapper wrapper,
							ResourceApiWrapperReceiver<SampleSearchRecord> receiver) throws ProcessFailedException {

						/* SampleSearchResourceApiWrapper の api呼び出しメソッドを呼び出す */
						wrapper.invokeApi(keys, receiver);
					}

				};

		/* ワーカーモジュールのgetResult()を呼び出し、結果として返す */
		return worker.getResult();
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchService#getValues2(jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchKey[], int)
	 */
	@Override
	public SampleSearchRecord[] getValues2(final SampleSearchKey[] keys, final int maxPerKey)
			throws ProcessFailedException {

		/* 環境変数取得なので、結果は必ず環境変数に対して一対一となるため
		 * 今回は、maxPerKeyを無視して、getValues()を共用する実装とする
		 * */

		return getValues(keys); //getValues()を呼び出し
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchService#getValues3(jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchKey[])
	 */
	@Override
	public SampleSearchRecord[] getValues3(SampleSearchKey[] keys) throws ProcessFailedException {
		/* getValues2 同様に getValues()を共用する*/
		return getValues(keys); //getValues()を呼び出し
	}
}

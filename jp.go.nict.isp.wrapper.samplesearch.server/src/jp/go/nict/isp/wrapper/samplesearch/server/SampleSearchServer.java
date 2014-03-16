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
package jp.go.nict.isp.wrapper.samplesearch.server;

import jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchKey;
import jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchRecord;
import jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchService;
import jp.go.nict.isp.wrapper.wisdom.abstractservice.AbstractServerModule;
import jp.go.nict.isp.wrapper.wisdom.abstractservice.AbstractServerModuleBase;
import jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultStorage;
import jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultStorageBase;
import jp.go.nict.isp.wrapper.wisdom.abstractservice.ResultStorageMultiKey;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

/**
 * サンプル検索サービスのサーバ側実装クラス
 * AbstractServerModuleBaseから派生して生成する。
 * SampleSearchServiceの実装を行う。
 * サービス連携基盤フレームワークを利用して実装を行う
 *
 */
public class SampleSearchServer extends AbstractServerModuleBase implements SampleSearchService {

	/* SampleSearchServiceのgetValuesを実装する。*/
	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchService#getValues(jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchKey[])
	 */
	@Override
	public SampleSearchRecord[] getValues(final SampleSearchKey[] keys) throws ProcessFailedException {

		/* 抽象型のServerを生成 */
		AbstractServerModule<SampleSearchRecord, SampleSearchService> server =
				new AbstractServerModule<SampleSearchRecord, SampleSearchService>
				(SampleSearchService.class,"SampleSearchServer", new SampleSearchRecord[] {}, this) {


			@Override
			public SampleSearchRecord[] sendRequest(SampleSearchService service) throws ProcessFailedException {
				return service.getValues(keys);
			}

		};
		/* Serverを呼び出して値を返す */
		return server.getResult();

	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchService#getValues2(jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchKey[], int)
	 */
	@Override
	public SampleSearchRecord[] getValues2(final SampleSearchKey[] keys, final int maxPerKey) throws ProcessFailedException {

		/*
		 * ここでは、デフォルトのResultStrageではなく、
		 * 特殊検索等で使用している、ResultStrageMultiKeyを用いた実装を行う
		 * */

		/* ResultStrageMultiKey を生成する */
		ResultStorage<SampleSearchRecord> storage =
			new ResultStorageMultiKey<SampleSearchRecord, SampleSearchKey>(this, new SampleSearchRecord[] {}, keys, maxPerKey) {
				/* getKeyをOverrideして、SampleSearchRecordからkeyを取り出して返す*/
				@Override
				protected SampleSearchKey getKey(SampleSearchRecord result) {
					return result.getKey();
				}
		};

		/* 抽象型のServerを生成 */
		AbstractServerModule<SampleSearchRecord, SampleSearchService> server =
				new AbstractServerModule<SampleSearchRecord, SampleSearchService>
				(SampleSearchService.class,"SampleSearchServer", new SampleSearchRecord[] {}, this,storage) {

			@Override
			public SampleSearchRecord[] sendRequest(SampleSearchService service) throws ProcessFailedException {
				return service.getValues2(keys,maxPerKey);
			}

		};
		/* Serverを呼び出して値を返す */
		return server.getResult();

	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchService#getValues3(jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchKey[])
	 */
	@Override
	public SampleSearchRecord[] getValues3(final SampleSearchKey[] keys) throws ProcessFailedException {

		/* カスタマイズしたResultStorageの例
		 * ResultStorageBaseから派生しカスタマイズする
		 * */
		ResultStorage<SampleSearchRecord> storage =
			new ResultStorageBase<SampleSearchRecord>(this, new SampleSearchRecord[] {}){

				@Override
				public boolean pushResult(SampleSearchRecord result) {
					/* 結果格納前に、valueを大文字へ変換するように拡張する*/
					if(result.getValue() != null){
						result.setValue(result.getValue().toUpperCase());
					}
					/* 親クラスのpushResult()へ渡す*/
					return super.pushResult(result);
				}
		};

		/* 抽象型のServerを生成 */
		AbstractServerModule<SampleSearchRecord, SampleSearchService> server =
				new AbstractServerModule<SampleSearchRecord, SampleSearchService>
				(SampleSearchService.class,"SampleSearchServer", new SampleSearchRecord[] {}, this,storage) {

			@Override
			public SampleSearchRecord[] sendRequest(SampleSearchService service) throws ProcessFailedException {
				return service.getValues3(keys);
			}
		};
		/* Serverを呼び出して値を返す */
		return server.getResult();
	}
}

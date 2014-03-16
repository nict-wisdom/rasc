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

package jp.go.nict.isp.wrapper.samplesearch.worker;

import jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchKey;
import jp.go.nict.isp.wisdom2013.api.samplesearch.SampleSearchRecord;
import jp.go.nict.isp.wrapper.wisdom.abstractservice.ResourceApiWrapperBase;
import jp.go.nict.isp.wrapper.wisdom.abstractservice.ResourceApiWrapperReceiver;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

/**
 * サンプル検索サービスのリソースAPI実装クラス.<br>
 * ResourceApiWrapperBaseから派生して実装する.<br>
 * ・SampleSearchではSingletonの形式で実装
 * ・実際のAPIは、Systemの環境変数取得メソッドを使用する
 *
 * */
public class SampleSearchResourceApiWrapper extends ResourceApiWrapperBase {

	/* singleton として自身生成*/
	private static SampleSearchResourceApiWrapper myInstance = new SampleSearchResourceApiWrapper();

	/* コンストラクタ*/
	/**
	 * コンストラクタ
	 */
	private SampleSearchResourceApiWrapper() {
		//特に初期化などなし
	}

	/* インスタンス取得メソッド*/
	/**
	 * インスタンス取得
	 * @return SampleSearchResourceApiWrapperのインスタンス
	 */
	public static SampleSearchResourceApiWrapper getInstance() {
		return myInstance; //インスタンスを返す
	}

	/* リソースAPIコールメソッド*/
	/**
	 * 低レベルレイヤーのサービスを呼び出す
	 * @param keys 		検索キー
	 * @param receiver  結果通知用レシーバ
	 * @throws ProcessFailedException 
	 */
	public void invokeApi(final SampleSearchKey[] keys,
			final ResourceApiWrapperReceiver<SampleSearchRecord> receiver) throws ProcessFailedException {
		/* サービスに応じたAPIの呼び出しを実装する*/

		/* 以下、SmapleSearvice用に環境変数取得を実装*/
		/* keyの配列分繰り返す */
		for (SampleSearchKey k : keys) {
			/* 環境変数を取得して、結果レコードを生成*/
			SampleSearchRecord result = new SampleSearchRecord(k, System.getenv(k.getEnvName()));

			/* レシーバを経由して、結果をワーカーモジュールへ通知 */
			receiver.receiveNotify(result);

		}
	}
}

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
package jp.go.nict.isp.wrapper.wisdom.abstractservice;

import java.util.Calendar;
import java.util.List;

import jp.go.nict.isp.wisdom2013.api.balancer.EndpointFactory;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver;
import jp.go.nict.langrid.servicecontainer.handler.RIProcessor;

/**
 * サービス連携フレームワークのサーバーコアモジュールクラス.<br>
 * サーバ側のサービス連携フレームワーク実装、サーバ側サービスは、本サービスを使用して、ワーカーと連携を行う.
 * @author kishimoto
 *
 */
public abstract class AbstractServerModule<V, S> {

	private final Class<S> clsService;
	private final String strLogName;
	private final V[] arrResultType;
	private final ResultStorage<V> resultStorage;
	private final AbstractServerModuleBase serverBase;
//	private final Object message;
//	private final Map<String, S> services;

	/* コンストラクタ */
	/**
	 * コンストラクタ
	 * @param clsService サービスクラス
	 * @param strLogName ログ表示名
	 * @param arrResultType サービスの結果型
	 * @param serverBase サーバサービスオブジェクト
	 */
	public AbstractServerModule(Class<S> clsService, String strLogName, V[] arrResultType, AbstractServerModuleBase serverBase) {
		this.clsService = clsService;
		this.strLogName = strLogName;
		this.arrResultType = arrResultType;
		this.resultStorage = new ResultStorageBase<V>(serverBase, arrResultType);
		this.serverBase = serverBase;

//		this.message = null;
//		services = new ConcurrentHashMap<String, S>();
		System.out.printf("<%d> ==%s ()== \n", System.currentTimeMillis(), getLogClassName());
	}

	/**
	 * コンストラクタ
	 * @param clsService サービスクラス
	 * @param strLogName ログ表示名
	 * @param arrResultType サービスの結果型
	 * @param serverBase サーバサービスオブジェクト
	 * @param resultStorage 任意のResultStorageオブジェクト
	 */
	public AbstractServerModule(Class<S> clsService, String strLogName, V[] arrResultType, AbstractServerModuleBase serverBase, ResultStorage<V> resultStorage) {
		this.clsService = clsService;
		this.strLogName = strLogName;
		this.arrResultType = arrResultType;
		this.serverBase = serverBase;
		this.resultStorage = resultStorage;

//		this.message = null;
//		services = new ConcurrentHashMap<String, S>();
		System.out.printf("<%d> ==%s ()== \n", System.currentTimeMillis(), getLogClassName());
	}

	/**
	 * コンストラクタ
	 * @param clsService サービスクラス
	 * @param strLogName ログ表示名
	 * @param arrResultType サービスの結果型
	 * @param serverBase サーバサービスオブジェクト
	 * @param services サービスマッピング用MAPクラス
	 * @param message Msgpack用オブジェクト
	 */
//	public AbstractServerModule(Class<S> clsService, String strLogName, V[] arrResultType, AbstractServerModuleBase serverBase, Map<String, S> services, Object message) {
//		this.clsService = clsService;
//		this.strLogName = strLogName;
//		this.arrResultType = arrResultType;
//		this.resultStorage = new ResultStorageBase<V>(serverBase, arrResultType);
//		this.serverBase = serverBase;
//
////		this.message = message;
////		this.services = services;
//		System.out.printf("<%d> ==%s ()== \n", System.currentTimeMillis(), getLogClassName());
//	}

	/**
	 * コンストラクタ
	 * @param clsService サービスクラス
	 * @param strLogName ログ表示名
	 * @param arrResultType サービスの結果型
	 * @param serverBase サーバサービスオブジェクト
	 * @param resultStorage 任意のResultStorageオブジェクト
	 * @param services サービスマッピング用MAPクラス
	 * @param message Msgpack用オブジェクト
	 */
//	public AbstractServerModule(Class<S> clsService, String strLogName, V[] arrResultType, AbstractServerModuleBase serverBase, ResultStorage<V> resultStorage, Map<String, S> services, Object message) {
//		this.clsService = clsService;
//		this.strLogName = strLogName;
//		this.arrResultType = arrResultType;
//		this.serverBase = serverBase;
//		this.resultStorage = resultStorage;
//
////		this.message = message;
////		this.services = services;
//		System.out.printf("<%d> ==%s ()== \n", System.currentTimeMillis(), getLogClassName());
//	}

	/**
	 * サービス連携呼び出し(ワーカーとサービス連携を開始する)
	 * @return 検索結果
	 * @throws ProcessFailedException 
	 */
	public V[] getResult() throws ProcessFailedException {
		final String threadTag = Thread.currentThread().getName();
		long startTime = System.currentTimeMillis();
		final boolean isStreamingReady = (serverBase.isStreamingReady() && (resultStorage instanceof ResultStorageStreaming<?>));
		final StreamingReceiver<Object> resultReceiver = serverBase.getReceiver();
		boolean isCached = false;

		System.out.printf("<%d> == %s::getResult()[waitTimeOut:%d msec] <%s> == \n", System.currentTimeMillis(), strLogName, serverBase.getWaitTimeOut(), threadTag);
		System.out.println("Start ## " + Calendar.getInstance().getTime());
		if (isStreamingReady) {
			System.out.printf(" Streaming Redy! %s \n", resultReceiver.getClass().getName());
		}

		if ((resultStorage instanceof ResultCacheable<?>)) {
			@SuppressWarnings("unchecked")
			ResultCacheable<V> cacheable = ((ResultCacheable<V>) resultStorage);
			if (cacheable.isEnable()) {
				if (cacheable.findCache()) {
					isCached = true;
				}
			}
		}

		if (!isCached) {
			/* executor 実行 */
			EndpointFactory ef = (serverBase.getEndpointFactory() != null) ? serverBase.getEndpointFactory() : new DefaultEndpointFactory();
			ef.setRealPath(RIProcessor.getCurrentServiceContext().getRealPath("/WEB-INF/"));
			//呼び出し
			new WorkerExecutor<V>().execute(isStreamingReady, resultReceiver, serverBase.getClientFactory(), clsService, serverBase.getWaitTimeOut(), this,resultStorage, serverBase.getEndpointList(),ef);

		} else {
			System.out.printf("use Result cached data \n");
		}

		List<V> resultList = resultStorage.getResult();

		Calendar et = Calendar.getInstance();
		System.out.println("End   ## " + et.getTime() + " [" + String.valueOf(System.currentTimeMillis() - startTime) + "ms] <" + threadTag + " >");

		System.out.printf("==> result records : [ %d records](%s):<%s> \n", resultList.size(), this.strLogName, threadTag);

		/* ストリーミングが有効な場合には、送信エラーの結果のみ返す */
		if ((isStreamingReady) && (!isCached)) {
			System.out.printf("==> Streaming send errors : [ %d records](%s):<%s> \n", resultStorage.getError().size(), strLogName, threadTag);
			return resultStorage.getError().toArray(arrResultType);
		}

		return resultList.toArray(arrResultType);
	}

	/**
	 * ログ表示名取得
	 * @return ログ表示名
	 */
	protected final String getLogClassName() {
		return strLogName;
	}

	/**
	 * サービス連携フレームワークより、ワーカーのサービスを呼び出す.<br>
	 * 抽象メソッドであり、AbstractServerModuleを使用するクラスでサービスの呼び出しを実装する.
	 * 
	 * @param service サービスのインスタンス
	 * @return ワーカーの検索結果
	 * @throws ProcessFailedException 
	 */
	public abstract V[] sendRequest(S service) throws ProcessFailedException;
}

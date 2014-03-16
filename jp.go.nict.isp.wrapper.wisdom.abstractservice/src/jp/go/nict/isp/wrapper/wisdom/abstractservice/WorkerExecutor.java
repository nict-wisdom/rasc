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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jp.go.nict.isp.wisdom2013.api.balancer.EndpointFactory;
import jp.go.nict.langrid.client.ClientFactory;
import jp.go.nict.langrid.client.msgpackrpc.MsgPackClientFactory;
import jp.go.nict.langrid.commons.rpc.ArrayElementsNotifier;
import jp.go.nict.langrid.commons.rpc.ArrayElementsReceiver;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver;

/**
 * WorkerExecutorクラス.<br>
 * サービス連携フレームワークで、ワーカーとの連携を実装するクラス.<br>
 * ワーカーへのリクエスト制御や結果の管理及び連携を制御する.
 * 
 * @author kishimoto
 *
 */
public class WorkerExecutor<V> {

//	private EndpointFactory endpointFactory;
//	private AbstractServerModule<V, ?> parent;

	/**
	 * コンストラクタ
	 */
	public WorkerExecutor() {

	}

//	public WorkerExecutor(EndpointFactory factory, AbstractServerModule<V, ?> parent) {
//		this();
//		this.endpointFactory = factory;
//		this.parent = parent;
//
//	}

	/**
	 * サービス連携実行処理
	 * 
	 * @param isStreamingReady ストリーミングフラグ
	 * @param resultReceiver 結果通知用レシーバ
	 * @param clfactory クライアントファクトリー
	 * @param clsService サービスクラス
	 * @param waitTimeOut タイムアウト値
	 * @param serverModule サーバーモジュールオブジェクト
	 * @param services msgpack用サービスマッピング
	 * @param resultStorage 結果格納ストレージ
	 * @param defEndpoints デフォルトのエンドポイントリスト
	 * @param ef エンドポイントファクトリー
	 * @throws ProcessFailedException 
	 */
	public final <S> void execute(final boolean isStreamingReady, final StreamingReceiver<Object> resultReceiver,
			final ClientFactory clfactory, final Class<S> clsService, final int waitTimeOut,
			final AbstractServerModule<V, S> serverModule, final Map<String, S> services, final ResultStorage<V> resultStorage, final List<String> defEndpoints,final EndpointFactory ef) throws ProcessFailedException {
		final ConcurrentLinkedQueue<V> queResult = new ConcurrentLinkedQueue<V>();
		final AtomicBoolean atomicJobEnd = new AtomicBoolean(false);
		final AtomicBoolean atomicDataEnd = new AtomicBoolean(false);
		final Thread mainThread = Thread.currentThread();

		/* executor で各Workerへの接続を実行する。 */
		final ExecutorService exec = Executors.newCachedThreadPool();
		final String threadTag = Thread.currentThread().getName();
//		final EndpointFactory ef = endpointFactory;
		ef.setSigName(clsService.getSimpleName());

		for (String endpoint : ef.create(defEndpoints)) {
			final String url = endpoint;

			/* executor に Jobを登録 */
			exec.execute(new Runnable() {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public void run() {
					try {
						boolean isMsgPack = false;
						if (clfactory.getClass().equals(MsgPackClientFactory.class)) {
							isMsgPack = true;
							if (!services.containsKey(url)) {
								services.put(url, new MsgPackClientFactory().create(clsService, new URL(url)));
							}
						}

						final S s = (isMsgPack) ? services.get(url) : clfactory.create(clsService, new URL(url));
						final AtomicInteger atCount = new AtomicInteger(0);

						if (s instanceof ArrayElementsNotifier) {
							((ArrayElementsNotifier) s).setReceiver(new ArrayElementsReceiver() {
								@Override
								public void receive(Object result) {
									/* count++ */
									atCount.getAndIncrement();

									/* concurrent キューに挿入して戻る */
									if (atomicDataEnd.get() == false) {
										queResult.add((V) result);
									}
								}
							});

							Calendar st = Calendar.getInstance();
							System.out.println("<" + threadTag + ">##" + url + " ## "
									+ String.format("%02d:%02d:%02d.%03d", st.get(Calendar.HOUR), st.get(Calendar.MINUTE), st.get(Calendar.SECOND), st.get(Calendar.MILLISECOND))
									+ " <" + Thread.currentThread().getName() + " >");

							serverModule.sendRequest(s);

							Calendar et = Calendar.getInstance();
							System.out.println("<" + threadTag + ">##" + url + " ## "
									+ String.format("%02d:%02d:%02d.%03d", et.get(Calendar.HOUR), et.get(Calendar.MINUTE), et.get(Calendar.SECOND), et.get(Calendar.MILLISECOND))
									+ " [" + String.valueOf(et.getTimeInMillis() - st.getTimeInMillis()) + "ms]"
									+ "( " + atCount.get() + " )" + " <" + Thread.currentThread().getName() + " >"
									);
						} else {
							V[] ret = (V[]) serverModule.sendRequest(s);
							for (V searchResult : ret) {
								queResult.add(searchResult);
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
						return;
					} catch (Exception e) {
						e.printStackTrace();
						return;
					} finally {
					}
				}
			});
		}

		/* キューを監視する者 */
		final Thread checkThread = new Thread(new Runnable() {

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				boolean allive = true;
				int countStreamingOK = 0;
				int countStreamingNG = 0;
				int inResult = 0;

				/* 各Workerからのキューをこのスレッドで監視する */
				while (allive) {

					/* キューを処理 */
					V searchResult = queResult.poll();// 取り出し
					while (searchResult != null) {
						inResult++;
						/* Streaming が有効な場合には、receiverで結果を返す */
						if (isStreamingReady) {
							/* 重複チェック */
							if (((ResultStorageStreaming<V>) resultStorage).contains(searchResult) == false) {
								/* フィルターチェック */
								if (((ResultStorageStreaming<V>) resultStorage).checkFilter(searchResult) == false) {

									/* receiverへ送信！*/
									if (!resultReceiver.receive(searchResult)) {
										/* 失敗した場合には、キューに入れておく*/
										resultStorage.pushError(searchResult);
										//										receiveErrors.add(searchResult);
										countStreamingNG++;
									} else {
										countStreamingOK++;
									}
								}
							}
						}

						if (resultStorage.pushResult(searchResult) == true) {
							allive = false;
							atomicDataEnd.set(true);
							mainThread.interrupt();// executor
													// にinterupt()を発生させて、即時終了
							break;
						}
						/* 取り出し */
						searchResult = queResult.poll();
					}

					/* 全てのWorkerが終了したかチェックする */
					if (atomicJobEnd.get() == true) {
						if (queResult.isEmpty()) {
							allive = false;
							break;
						}
					} else {
						Thread.yield();
					}
				}

				if (isStreamingReady) {
					System.out.printf("Streaming(%d:%d:%d:%d)(%s):<%s> \n", countStreamingOK, countStreamingNG, (countStreamingNG + countStreamingOK), inResult, clsService.getSimpleName(), threadTag);
				}
			}
		});

		/* 統計開始(暫定)*/
		ef.startStatistics(System.currentTimeMillis());

		/* executor 締切 */
		exec.shutdown();

		/* 監視開始 */
		checkThread.start();

		try {

			/* 終了待ちを行う */
			if (exec.awaitTermination(waitTimeOut, TimeUnit.MILLISECONDS) == false) {
				/* 待ち時間が満了した場合、executorを終端して終了 */
				System.out.println("== awaitTermination() timeout! == ");
				exec.shutdownNow();
			} else {
				System.out.println("== awaitTermination() terminated ! == ");
			}

		} catch (InterruptedException e) {
			System.out.println("== awaitTermination() interrupted! == ");
			exec.shutdownNow();
		} finally {
			atomicJobEnd.set(true);
		}

		/* 統計終了(暫定)*/
		ef.startStatistics(System.currentTimeMillis());

		try {
			/* キュー監視スレッド終了待ち */
			checkThread.join(1000);

		} catch (InterruptedException e) {
			System.out.println("== join () interrupted! == ");
		} finally {
			/* キューに積み込まないように設定 */
			atomicDataEnd.set(true);
		}

	}

}

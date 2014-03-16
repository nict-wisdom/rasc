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
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver;

/**
 * サービス連携フレームワーク、ワーカー用コアモジュールクラス.<BR>
 * サービス連携のワーカーサービスは、本クラスを使用してサーバとサービス連携を実装する.
 *  
 * 
 */
public abstract class AbstractWorkerModule<V, C extends ResourceApiWrapperBase> {

	private static final ThreadLocal<Set<ResourceApiWrapperBase>> safeSingleton = new ThreadLocal<Set<ResourceApiWrapperBase>>();// GC防止用
	private final AbstractWorkerModuleBase workerBase;
	private final String strLogName;
	private final V[] resultType;
	private final C resourceWrapper;
	private final String hash_id;

	/**
	 * コンストラクタ
	 * @param resourceWrapper ResourceWrapper
	 * @param resultType サービスの結果型
	 * @param strLogName ログ表示名
	 * @param workerBase ワーカーベースクラス
	 */
	public AbstractWorkerModule(C resourceWrapper, V[] resultType, String strLogName, AbstractWorkerModuleBase workerBase) {
		this.workerBase = workerBase;
		this.strLogName = strLogName;
		this.resultType = resultType;
		this.resourceWrapper = resourceWrapper;
		pushResourceWrapper(resourceWrapper);
		hash_id = String.format("%d", System.nanoTime());
	}

	/**
	 * サービス検索呼び出し<BR>
	 * ワーカーサービスは、本メソッドを呼び出して、検索結果を取得する。
	 * @return 検索結果
	 * @throws ProcessFailedException 
	 */
	public V[] getResult() throws ProcessFailedException {

		final List<V> results = new ArrayList<V>();
		final StreamingReceiver<Object> recv = workerBase.getReciver();
		final AtomicLong deltaTime = new AtomicLong(0);
		final AtomicLong onreciveTime = new AtomicLong(0);
		final AtomicLong record_cnt = new AtomicLong(0);
		long startTime = 0;

		System.out.printf("<%s>: %s::getResult() --start-- at %s \n", hash_id, strLogName, Calendar.getInstance().getTime());

		startTime = System.currentTimeMillis();

		try {
			/* callCoreApi の値取得を呼び出す */
			callResourceApi(resourceWrapper, new ResourceApiWrapperReceiver<V>() {
				@Override
				public void receiveNotify(V value) {
					long recvTime = System.currentTimeMillis();
					record_cnt.incrementAndGet();
					boolean received = false;
					if (recv != null) {
						long startTime = System.currentTimeMillis();
						received = recv.receive(value);
						deltaTime.addAndGet(System.currentTimeMillis() - startTime);
					}
					if (!received) {
						// 追加
						results.add(value);
					}
					onreciveTime.addAndGet(System.currentTimeMillis() - recvTime);
				}
			});

			long nowTimeMillis = System.currentTimeMillis();
			System.out.printf("<%s> : --done-- total[ %d ms] reciver[ %d ms] onrecive[ %d ms ] count[ %d ]\n", hash_id, nowTimeMillis - startTime, deltaTime.longValue(),
					onreciveTime.longValue(), record_cnt.intValue());
			System.out.printf("<%s> : %s::reply() --done-- [ %d ms ] [%s]\n", hash_id, strLogName, nowTimeMillis - startTime, workerBase.getReciver());

		} catch (Exception e) {
			throw new ProcessFailedException(e);
		}
		return results.toArray(resultType);
	}

	/* GC防止用にsingleton object を ThreadLocalへ格納して参照しておく */
	/**
	 * ResourceWrapperを保存
	 * @param ac ResourceWrapper
	 */
	protected void pushResourceWrapper(final C ac) {
		if (safeSingleton.get() != null) {
			Set<ResourceApiWrapperBase> lst = safeSingleton.get();
			if (lst.contains(ac) == false) {
				System.out.printf("--ResourceWrapper(%s:%s) -- \n", Thread.currentThread().getName(), ac.getClass().getName());
				lst.add(ac);
				safeSingleton.set(lst);
			}
		} else {
			Set<ResourceApiWrapperBase> lst = new HashSet<ResourceApiWrapperBase>();
			lst.add(ac);
			System.out.printf("--ResourceWrapper(%s:%s) -- \n", Thread.currentThread().getName(), ac.getClass().getName());
			safeSingleton.set(lst);
		}
	}

	/**
	 * 低レベルレイヤーのサービスを呼び出す<br>
	 * 抽象メソッドである、AbstractWorkerModuleを使用するクラスで実装を行う。
	 * @param wrapper ResourceWrapper
	 * @param receiver 結果通知用レシーバ
	 * @throws ProcessFailedException 
	 */
	protected abstract void callResourceApi(C wrapper, ResourceApiWrapperReceiver<V> receiver) throws ProcessFailedException;

	/**
	 * 任意ログ出力
	 * @param msg メッセージ
	 */
	public void printLog(final String msg) {

	}

}

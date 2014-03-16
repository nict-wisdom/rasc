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

import jp.go.nict.langrid.servicecontainer.executor.StreamingNotifier;
import jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver;

/**
 * サービス連携フレームワーク、ワーカーサービスベースクラス.<br>
 * サービス連携フレームワーク対応のワーカーサービスは、本クラスから派生して実装する。
 * @author kishimoto
 *
 */
public class AbstractWorkerModuleBase implements StreamingNotifier<Object> {
	private ThreadLocal<StreamingReceiver<Object>> stream_receiver = new ThreadLocal<StreamingReceiver<Object>>(); // レシーバーオブジェクト

	/**
	 * コンストラクタ
	 */
	public AbstractWorkerModuleBase() {

	}

	/* (非 Javadoc)
	 * @see jp.go.nict.langrid.servicecontainer.executor.StreamingNotifier#setReceiver(jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver<T>)
	 */
	@Override
	public void setReceiver(StreamingReceiver<Object> receiver) {
		if (receiver != null) {
			this.stream_receiver.set(receiver);
		} else {
			this.stream_receiver.remove();
		}
	}

	/**
	 * ストリーミング用レシーバを取得する。
	 * @return StreamingReceiver
	 */
	public StreamingReceiver<Object> getReciver() {
		return stream_receiver.get();
	}

}

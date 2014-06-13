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

package jp.go.nict.langrid.client.msgpackrpc;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.msgpack.MessagePack;
import org.msgpack.rpc.ClientEx;
import org.msgpack.rpc.loop.netty.NettyEventLoopEx;

/**
 * Msgpackクライアント用のコネクションプールクラス.
 * @author kishimoto
 *
 */
public class MsgPackClientPool {

	private static final MsgPackClientPool myInstance = new MsgPackClientPool();
	private Map<InetSocketAddress, ClientEx> poolClient = new HashMap<>();

	/**
	 * コンストラクタ(非公開).
	 */
	private MsgPackClientPool() {

	}

	/**
	 * インスタンスを取得する.
	 * @return MsgPackClientPool Instance.
	 */
	public static MsgPackClientPool getInstance() {
		return myInstance;
	}

	/**
	 * Msgpackクライアントをコネクションプールから取得する.<BR>
	 * 存在しない場合には、新規に作成する.
	 * @param addr  接続先アドレス
	 * @param timeOut タイムアウト
	 * @return ClientEx 
	 */
	public ClientEx getClient(InetSocketAddress addr, int timeOut) {
		ClientEx ce = null;

		synchronized (poolClient) {
			if (poolClient.containsKey(addr)) {
				//既に生成済み
				ce = poolClient.get(addr);
			} else {
				ce = new ClientEx(addr, new NettyEventLoopEx(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), Executors.newScheduledThreadPool(2), new MessagePack()));
				poolClient.put(addr, ce);
				ce.setRequestTimeout(timeOut);
			}
			ce.joinClient();
		}
		return ce;
	}

	/**
	 * 切断処理.指定されたClientExを切断する.
	 * @param ce ClientEx 
	 */
	public void close(ClientEx ce) {
		boolean execClose = false;
		synchronized (poolClient) {
			ce.leaveClient();
			if (ce.getRefCount() == 0) {
				InetSocketAddress key = null;
				for (Map.Entry<InetSocketAddress, ClientEx> ent : poolClient.entrySet()) {
					if (ent.getValue().equals(ce)) {
						key = ent.getKey();
						break;
					}
				}

				if (key != null) {
					poolClient.remove(key);
					execClose = true;
				}
			}
		}
		if (execClose) {
			ce.close();
			ce.getEventLoop().shutdown();
			try {
				ce.getEventLoop().join();
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}

	/**
	 * 全強制切断処理.
	 */
	public void shutdownAllClient() {
		synchronized (poolClient) {
			for (Map.Entry<InetSocketAddress, ClientEx> ent : poolClient.entrySet()) {
				ClientEx ce = ent.getValue();
				ce.close();
				ce.getEventLoop().shutdown();
				try {
					ce.getEventLoop().join();
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
			poolClient.clear();
		}
	}

}

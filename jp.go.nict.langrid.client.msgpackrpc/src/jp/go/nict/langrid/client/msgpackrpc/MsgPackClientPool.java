package jp.go.nict.langrid.client.msgpackrpc;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.msgpack.MessagePack;
import org.msgpack.rpc.ClientEx;
import org.msgpack.rpc.loop.netty.NettyEventLoopEx;

public class MsgPackClientPool {

	private static final MsgPackClientPool myInstance = new MsgPackClientPool();
	//	private boolean isEmpty = true;
	private Map<InetSocketAddress, ClientEx> poolClient = new HashMap<>();

	private MsgPackClientPool() {

	}

	public static MsgPackClientPool getInstance() {
		return myInstance;
	}

	public ClientEx getClient(InetSocketAddress addr,int timeOut) {
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
					execClose=true;
				}
			}
		}
		if(execClose){
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

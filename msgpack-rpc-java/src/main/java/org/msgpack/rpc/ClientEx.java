//
// MessagePack-RPC for Java
//
// Copyright (C) 2010 FURUHASHI Sadayuki
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
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
package org.msgpack.rpc;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.msgpack.rpc.address.Address;
import org.msgpack.rpc.config.ClientConfig;
import org.msgpack.rpc.loop.EventLoop;
import org.msgpack.rpc.message.RequestMessage;
import org.msgpack.rpc.reflect.Reflect;
import org.msgpack.type.Value;

/**
 * Client拡張クラス.
 * @author kishimoto
 *
 */
public class ClientEx extends Client {

	protected static ThreadLocal<Map<String, ResponseDataListener>> safeListener = new ThreadLocal<>();
	protected static final Map<Integer, ResponseDataListener> responseListener = new ConcurrentHashMap<>();
	protected static final AtomicInteger newSeqid = new AtomicInteger(0);
	protected AtomicInteger refAccess = new AtomicInteger(0);

	/**
	 * コンストラクタ.
	 * @param address 
	 * @param config 
	 * @param loop 
	 */
	public ClientEx(Address address, ClientConfig config, EventLoop loop) {
		super(address, config, loop);
	}

	/**
	 * コンストラクタ.
	 * @param address 
	 * @param config 
	 * @param loop 
	 * @param reflect 
	 */
	public ClientEx(Address address, ClientConfig config, EventLoop loop, Reflect reflect) {
		super(address, config, loop, reflect);
	}

	/**
	 * コンストラクタ.
	 * @param address 
	 */
	public ClientEx(InetSocketAddress address) {
		super(address);
	}

	/**
	 * コンストラクタ.
	 * @param address 
	 * @param config 
	 */
	public ClientEx(InetSocketAddress address, ClientConfig config) {
		super(address, config);
	}

	/**
	 * コンストラクタ.
	 * @param address 
	 * @param config 
	 * @param loop 
	 */
	public ClientEx(InetSocketAddress address, ClientConfig config, EventLoop loop) {
		super(address, config, loop);
	}

	/**
	 * コンストラクタ.
	 * @param address 
	 * @param config 
	 * @param loop 
	 * @param reflect 
	 */
	public ClientEx(InetSocketAddress address, ClientConfig config, EventLoop loop, Reflect reflect) {
		super(address, config, loop, reflect);
	}

	/**
	 * コンストラクタ.
	 * @param address 
	 * @param loop 
	 */
	public ClientEx(InetSocketAddress address, EventLoop loop) {
		super(address, loop);
	}

	/**
	 * コンストラクタ.
	 * @param host 
	 * @param port 
	 */
	public ClientEx(String host, int port) throws UnknownHostException {
		super(host, port);
	}

	/**
	 * コンストラクタ.
	 * @param host 
	 * @param port 
	 * @param config 
	 */
	public ClientEx(String host, int port, ClientConfig config)
			throws UnknownHostException {
		super(host, port, config);
	}

	/**
	 * コンストラクタ.
	 * @param host 
	 * @param port 
	 * @param config 
	 * @param loop 
	 */
	public ClientEx(String host, int port, ClientConfig config, EventLoop loop)
			throws UnknownHostException {
		super(host, port, config, loop);
	}

	/**
	 * コンストラクタ.
	 * @param host 
	 * @param port 
	 * @param loop 
	 */
	public ClientEx(String host, int port, EventLoop loop)
			throws UnknownHostException {
		super(host, port, loop);
	}

	/**
	 * コンストラクタ.
	 * @param host 
	 * @param port 
	 * @param loop 
	 * @param reflect 
	 */
	public ClientEx(String host, int port, EventLoop loop, Reflect reflect)
			throws UnknownHostException {
		super(host, port, loop, reflect);
	}

	/**
	 * ストリーミング用のイベントリスナーを追加する
	 * @param method method名
	 * @param listen リスナ
	 */
	public void addListener(String method, ResponseDataListener listen) {
		Map<String, ResponseDataListener> listeners = safeListener.get();
		if (listeners == null) {
			listeners = new HashMap<>();
			safeListener.set(listeners);
		}
		listeners.put(method, listen);
	}

	/**
	 * Client参照を登録する.
	 */
	public void joinClient() {
		refAccess.incrementAndGet();
	}

	/**
	 * Client参照を解除する.
	 */
	public void leaveClient() {
		refAccess.decrementAndGet();
	}

	/**
	 * Client参照数を取得する.
	 * @return
	 */
	public int getRefCount() {
		return refAccess.get();
	}

	/* (非 Javadoc)
	 * @see org.msgpack.rpc.Session#onResponse(int, org.msgpack.type.Value, org.msgpack.type.Value)
	 */
	@Override
	public void onResponse(int msgid, Value result, Value error) {
		FutureImpl f;
		synchronized (reqtable) {
			f = reqtable.remove(msgid);
			if (responseListener != null) {
				responseListener.remove(msgid);
			}
		}
		if (f == null) {
			// FIXME log
			return;
		}
		f.setResult(result, error);
	}

	/**
	 * ストリーミングハンドラ
	 * @param msgid 
	 * @param result 
	 * @param error 
	 */
	public void onResponseData(int msgid, Value result, Value error) {
		if (responseListener != null) {
			ResponseDataListener rdl = responseListener.get(msgid);
			if (rdl != null) {
				rdl.onResponseData(msgid, result, error);
			}
		}
	}

	/* (非 Javadoc)
	 * @see org.msgpack.rpc.Session#sendRequest(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Future<Value> sendRequest(String method, Object[] args) {

		int msgid = ClientEx.newSeqid.incrementAndGet();
		if ((msgid == Integer.MAX_VALUE) || (msgid < 0)) {
			ClientEx.newSeqid.set(0);
		}

		RequestMessage msg = new RequestMessage(msgid, method, args);
		FutureImpl f = new FutureImpl(this);

		synchronized (reqtable) {
			reqtable.put(msgid, f);
			Map<String, ResponseDataListener> listeners = safeListener.get();
			if (listeners != null) {
				ResponseDataListener rdl = listeners.remove(method);
				if (rdl != null) {
					responseListener.put(msgid, rdl);
				}
			}
		}
		transport.sendMessage(msg);

		return new Future<Value>(loop.getMessagePack(), f);
	}

}

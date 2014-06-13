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
package jp.go.nict.ial.servicecontainer.handler.msgpackrpc;

import java.io.IOException;
import java.lang.reflect.Method;

import jp.go.nict.langrid.servicecontainer.executor.StreamingNotifier;
import jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver;

import org.msgpack.rpc.Request;
import org.msgpack.rpc.RequestEx;
import org.msgpack.rpc.dispatcher.MethodDispatcher;
import org.msgpack.rpc.reflect.Invoker;
import org.msgpack.rpc.reflect.Reflect;
import org.msgpack.rpc.reflect.ReflectionInvokerBuilder.ReflectionInvoker;

/**
 * MethodDispatcherExクラス<br>
 * org.msgpack.rpc.dispatcher.MethodDispatcher の派生クラス.<br>
 * Msgpackのストリーミング実装.
 * @author kishimoto
 *
 */
public class MethodDispatcherEx extends MethodDispatcher {

	private static final String PROP_KEY_MSGPACKRPC_ENABLE_STREAMING = "msgpack.rpc.enable.streaming";
	private static final String PROP_VALUE_MSGPACKRPC_ENABLE_STREAMING = "yes";
	private boolean isEnableStreaming = true;

	/**
	 * コンストラクタ
	 * @param reflect 
	 * @param target 
	 * @param iface 
	 */
	public MethodDispatcherEx(Reflect reflect, Object target, Class<?> iface) {
		super(reflect, target, iface);
		String value = System.getProperty(PROP_KEY_MSGPACKRPC_ENABLE_STREAMING, PROP_VALUE_MSGPACKRPC_ENABLE_STREAMING);
		if (!value.equals(PROP_VALUE_MSGPACKRPC_ENABLE_STREAMING)) {
			isEnableStreaming = false;
		}
	}

	/**
	 * コンストラクタ
	 * @param reflect 
	 * @param target 
	 * @param methods 
	 */
	public MethodDispatcherEx(Reflect reflect, Object target, Method[] methods) {
		super(reflect, target, methods);
		String value = System.getProperty(PROP_KEY_MSGPACKRPC_ENABLE_STREAMING, PROP_VALUE_MSGPACKRPC_ENABLE_STREAMING);
		if (!value.equals(PROP_VALUE_MSGPACKRPC_ENABLE_STREAMING)) {
			isEnableStreaming = false;
		}
	}

	/**
	 * コンストラクタ
	 * @param reflect 
	 * @param target 
	 */
	public MethodDispatcherEx(Reflect reflect, Object target) {
		super(reflect, target);
		String value = System.getProperty(PROP_KEY_MSGPACKRPC_ENABLE_STREAMING, PROP_VALUE_MSGPACKRPC_ENABLE_STREAMING);
		if (!value.equals(PROP_VALUE_MSGPACKRPC_ENABLE_STREAMING)) {
			isEnableStreaming = false;
		}
	}

	/* (非 Javadoc)
	 * @see org.msgpack.rpc.dispatcher.MethodDispatcher#dispatch(org.msgpack.rpc.Request)
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public void dispatch(final Request request) throws Exception {
		final RequestEx requestEx = (RequestEx) request;
		Invoker ivk = methodMap.get(request.getMethodName());
		if (ivk == null) {
			// FIXME
			throw new IOException(".CallError.NoMethodError");
		}

		if (ivk instanceof ReflectionInvoker) {
			ReflectionInvoker ri = (ReflectionInvoker) ivk;
			if ((ri.getMethod().getReturnType().isArray()) && (isEnableStreaming) && (target instanceof StreamingNotifier)) {
				StreamingNotifier<Object> notify = (StreamingNotifier<Object>) target;
				notify.setReceiver(new StreamingReceiver<Object>() {

					@Override
					public boolean receive(Object result) {
						requestEx.sendResponseData(result, null);
						return true;
					}
				});
			}
		}
		ivk.invoke(target, request);
	}

}

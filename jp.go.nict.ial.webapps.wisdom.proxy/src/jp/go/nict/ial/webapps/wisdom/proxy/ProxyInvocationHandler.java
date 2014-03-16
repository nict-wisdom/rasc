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

package jp.go.nict.ial.webapps.wisdom.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Proxy用のInvocationHandler実装クラス.<br>
 * サービスのメソッドを呼び出す
 * @author kishimoto
 */
public class ProxyInvocationHandler implements InvocationHandler {

	/**
	 * serviceインスタンス
	 */
	private Object service;

	/**
	 * コンストラクタ
	 */
	public ProxyInvocationHandler() {

	}

	/**
	 * コンストラクタ
	 * @param service サービスのインスタンス
	 */
	public ProxyInvocationHandler(Object service) {
		this();
		this.service = service;

	}

	/* (非 Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object instance, Method method, Object[] args) throws Throwable {

		if (service instanceof InvocationHandler) {
			return ((InvocationHandler) service).invoke(service, method, args);
		}
		return null;
	}
}

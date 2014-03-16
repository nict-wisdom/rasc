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

import java.lang.reflect.Proxy;
import java.util.Set;

import jp.go.nict.langrid.commons.ws.ServiceContext;
import jp.go.nict.langrid.servicecontainer.executor.StreamingNotifier;
import jp.go.nict.langrid.servicecontainer.handler.TargetServiceFactory;
import jp.go.nict.langrid.servicecontainer.service.AbstractService;

/**
 * Proxyサービス用のTargetServiceFactory実装クラス<br>
 * jp.go.nict.langrid.servicecontainer.handler.TargetServiceFactoryから派生し、Proxyサービス用に拡張.<br>
 * jp.go.nict.ial.webapps.wisdom.proxy.ProxyTargetServiceFactory.createService(ClassLoader, ServiceContext, Class<T>)をオーバーライド.
 * 
 * @author kishimoto
 * @see jp.go.nict.langrid.servicecontainer.handler.TargetServiceFactory
 */
public class ProxyTargetServiceFactory extends TargetServiceFactory {

	/* (非 Javadoc)
	 * @see jp.go.nict.langrid.servicecontainer.handler.AbstractServiceFactory#createService(java.lang.ClassLoader, jp.go.nict.langrid.commons.ws.ServiceContext, java.lang.Class<T>)
	 */
	@Override
	public <T> T createService(ClassLoader classLoader, ServiceContext context, Class<T> interfaceClass) {
		Object service = getService();

		Set<Class<?>> interfaces = getInterfaces();
		if (service instanceof StreamingNotifier) {
			interfaces.add(StreamingNotifier.class);
		}
		if (service instanceof AbstractService) {
			((AbstractService) service).setServiceName(super.getServiceName());
		}

		if (service instanceof ProxyServiceName) {
			((ProxyServiceName) service).setServiceName(super.getServiceName());
		}
		return interfaceClass.cast(Proxy.newProxyInstance(
				classLoader, interfaces.toArray(new Class<?>[] {})
				, new ProxyInvocationHandler(service)
				));
	}

}

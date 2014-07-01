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

package jp.go.nict.rasc.service.initializer.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import jp.go.nict.langrid.commons.ws.LocalServiceContext;
import jp.go.nict.langrid.commons.ws.ServiceContext;
import jp.go.nict.langrid.servicecontainer.handler.ServiceFactory;
import jp.go.nict.langrid.servicecontainer.handler.ServiceLoader;

/**
 * サービス初期化用 Servlet クラス.<BR>
 * web.xml の load-on-startup にて呼び出されるServlet.
 * @author kishimoto
 *
 */
public class InitializerServlet extends HttpServlet {

	private static final String INITIALIZER_CLASS_NAME = "jp.go.nict.rasc.service.api.ServiceInitializer";
	private static final String ABSTRACT_SERVICES_CLASS_NAME = "jp.go.nict.langrid.servicecontainer.service.AbstractService";

	/* (非 Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();

		/* Service Context */
		final ServiceContext sc = new LocalServiceContext() {

			@Override
			public String getInitParameter(String param) {
				return getServletContext().getInitParameter(param);
			}

			@Override
			public String getRealPath(String path) {
				return getServletContext().getRealPath(path);
			}

		};

		/* Service Loader*/
		ServiceLoader loader = new ServiceLoader(sc);
		/* Class Loader */
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		try {
			/* サービスを検索 */
			for (String s : loader.listServiceNames()) {
				ServiceFactory f = loader.loadServiceFactory(cl, s);
				Object service = f.getService();

				/* setServiceName があれば、service名をセットする*/
				for (Method m : service.getClass().getMethods()) {
					if (m.getName().equals("setServiceName") && m.getDeclaringClass().getName().equals(ABSTRACT_SERVICES_CLASS_NAME)) {
						m.invoke(service, s);
						break;
					}
				}

				for (Class<?> clazz : f.getInterfaces()) {

					/* 初期化用のInterfaceを持つものは、サービスのinitを呼び出し */
					if (clazz.getName().equals(INITIALIZER_CLASS_NAME)) {
						for (Method m : clazz.getMethods()) {
							if (m.getName().equals("init")) {
								m.invoke(service);
								break;
							}
						}
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}

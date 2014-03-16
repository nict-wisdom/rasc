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

package jp.go.nict.isp.wisdom2013.plugin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import jp.go.nict.isp.wisdom2013.api.balancer.AbstractEndpointFactory;
import jp.go.nict.isp.wisdom2013.api.balancer.EndpointBalancer;
import net.arnx.jsonic.JSON;

/**
 * EndpointFactoryImplクラス.<br>
 * EndpointFactoryの実装クラス、JSON形式のファイルからEndPointリストを読み込む。
 * @author kishimoto
 *
 */
public class EndpointFactoryImpl extends AbstractEndpointFactory {

	/**
	 * JSONファイル読み込み用クラス.
	 * @author kishimoto
	 *
	 */
	protected class EndpointInfo {
		private String base;

		private List<List<String>> endpoints;

		/**
		 * コンストラクタ
		 */
		public EndpointInfo() {

		}

		/**
		 * baseを取得する
		 * @return base
		 */
		public String getBase() {
			return base;
		}

		/**
		 * endpointsを取得する
		 * @return endpoints
		 */
		public List<List<String>> getEndpoints() {
			return endpoints;
		}

		/**
		 * baseを設定する
		 * @param base 
		 */
		public void setBase(String base) {
			this.base = base;
		}

		/**
		 * endpointsを設定する。
		 * @param endpoints 
		 */
		public void setEndpoints(List<List<String>> endpoints) {
			this.endpoints = endpoints;
		}
	}

	private EndpointInfo endpoint = null;

	private String jsonfile = null;

	/**
	 * コンストラクタ
	 */
	public EndpointFactoryImpl() {
		super();
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.AbstractEndpointFactory#create(java.util.List, java.lang.String)
	 */
	@Override
	public List<String> create(List<String> defList, String sig) {
		if (endpoint == null) {

			try {
				FileInputStream fis = new FileInputStream(getRealPath() + "/" + jsonfile);
				endpoint = JSON.decode(fis, EndpointInfo.class);
				fis.close();

				if (eb == null) {
					Class<?> clsz = Class.forName(balancer);
					Method mthod = clsz.getMethod("getInstance");
					eb = (EndpointBalancer) mthod.invoke(null);
				}

				if (endpoint.endpoints.size() <= 0) {
					return defList;
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return defList;
			} catch (IOException e) {
				e.printStackTrace();
				return defList;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return defList;
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				return defList;
			} catch (SecurityException e) {
				e.printStackTrace();
				return defList;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return defList;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return defList;
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				return defList;
			}
		}

		List<String> l = eb.getList(endpoint.getEndpoints(), sig);

		return (l.size() > 0) ? l : defList;
	}

	/**
	 * JSONファイルパスを取得する。
	 * @return JSONファイルへのパス
	 */
	public String getJsonfile() {
		return jsonfile;
	}

	/**
	 * JSONファイルパスを設定する
	 * @param jsonfile JSONファイルへのパス
	 */
	public void setJsonfile(String jsonfile) {
		this.jsonfile = jsonfile;
	}

}

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jp.go.nict.isp.wisdom2013.api.balancer.AbstractEndpointFactory;
import jp.go.nict.isp.wisdom2013.api.balancer.EndpointBalancer;

/**
 * EndpointFactoryDatabaseクラス.<br>
 * EndpointFactoryの実装クラス,Endpointの情報をデータベースより取得する実装.
 * 
 * @author mori
 *
 */
public class EndpointFactoryDatabase extends AbstractEndpointFactory {
	/**
	 * データベースへのPATHを取得する
	 * @return データベースへのPATH
	 */
	public String getDb_path() {
		return db_path;
	}

	/**
	 * データベースへのPATHを設定する
	 * @param db_path データベースのパス
	 */
	public void setDb_path(String db_path) {
		this.db_path = db_path;
	}

	/**
	 * データベースのUserIDを取得する
	 * @return UserID
	 */
	public String getDb_id() {
		return db_id;
	}

	/**
	 * データベースのUserIDを設定する
	 * @param db_id UserID
	 */
	public void setDb_id(String db_id) {
		this.db_id = db_id;
	}

	/**
	 * データベースのパスワードを取得する
	 * @return パスワード
	 */
	public String getDb_pass() {
		return db_pass;
	}

	/**
	 * データベースのパスワードを設定する
	 * @param db_pass パスワード
	 */
	public void setDb_pass(String db_pass) {
		this.db_pass = db_pass;
	}

	/**
	 * コンストラクタ
	 */
	public EndpointFactoryDatabase() {
		super();
		counter = new AtomicInteger();
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.isp.wisdom2013.api.balancer.AbstractEndpointFactory#create(java.util.List, java.lang.String)
	 */
	@Override
	public List<String> create(List<String> defList, String serviceName) {
		int limit = 500;
		if(list == null){
			//初回は必ずリスト生成
			makeList(serviceName);
		}else if(counter.getAndIncrement() > limit){
			//規定回数以上実行されると、リストを再生成する
			counter.set(0);
			makeList(serviceName);
		}

		if (eb == null) {
			Class<?> clsz;
			try {
				clsz = Class.forName(balancer);
				Method mthod = clsz.getMethod("getInstance");
				eb = (EndpointBalancer) mthod.invoke(null);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return list.get(0);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				return list.get(0);
			} catch (SecurityException e) {
				e.printStackTrace();
				return list.get(0);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return list.get(0);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return list.get(0);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				return list.get(0);
			}
		}

		return eb.getList(list, serviceName);
	}

	/**
	 * サービスのEndPointリストをDB情報から作成する.
	 * @param serviceName サービス名
	 */
	public void makeList(String serviceName) {
		list = new ArrayList<List<String>>();
		//DB接続
		Connection con;
		try {
			con = DriverManager.getConnection("jdbc:mysql://" + db_path, db_id, db_pass);

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM " + serviceName);

			while(rs.next()){
				//1リスト1件で作成
				List <String> l = new ArrayList<String>();
				l.add(rs.getString("url"));
				list.add(l);
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String db_path = "hostname/endpoints";	/** DBのURLを設定する */
	private String db_id = "userName";				/** DBアクセスのUserID */
	private String db_pass = "passWord";			/** DBアクセスのPassword */
	private List<List <String>> list;
	private AtomicInteger counter;

}

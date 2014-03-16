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

package jp.go.nict.isp.wisdom2013.api.samplesearch;

import org.msgpack.annotation.Message;

import jp.go.nict.langrid.commons.rpc.intf.Field;

/**
 * サンプル検索サービス用、検索キークラス.
 * 
 * @author kishimoto
 * 
 */
/* Msgpack用に@Messageが必要 */
@Message
public class SampleSearchKey {

	/*
	 * 各フィールドには、@Fieldが必要
	 */
	/**
	 * 検索対象の環境変数名
	 */
	@Field(order = 1)
	private String envName; // 検索する環境変数名

	/* デフォルトコンストラクタ(必須） */
	/**
	 * コンストラクタ.
	 */
	public SampleSearchKey() {

	}

	/* コンストラクタ(任意) */
	/**
	 * コンストラクタ
	 * @param envName 環境変数名
	 */
	public SampleSearchKey(String envName) {
		this.envName = envName;
	}

	/* 各フィールドへのアクセッサ(必須) */
	/**
	 * 環境変数名を取得する
	 * @return 環境変数名
	 */
	public final String getEnvName() {
		return envName;
	}

	/**
	 * 環境変数名を設定する
	 * @param envName 環境変数名
	 */
	public final void setEnvName(String envName) {
		this.envName = envName;
	}

	/* hashCode()及びequals() override */
	/* (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((envName == null) ? 0 : envName.hashCode());
		return result;
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SampleSearchKey other = (SampleSearchKey) obj;
		if (envName == null) {
			if (other.envName != null)
				return false;
		} else if (!envName.equals(other.envName))
			return false;
		return true;
	}

}

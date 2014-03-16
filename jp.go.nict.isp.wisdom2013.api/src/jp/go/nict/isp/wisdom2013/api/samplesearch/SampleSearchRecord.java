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
 * サンプル検索サービス用、検索結果レコードクラス.<br>
 * サンプル検索サービスの検索結果が格納されるレコード.
 * 
 * @author kishimoto
 *
 */
/**/
@Message
public class SampleSearchRecord {

	/*
	 * 各フィールドには、@Fieldが必要
	 */
	/**
	 * 検索キー
	 */
	@Field(order = 1)
	private SampleSearchKey key; // 検索キー (複数検索するので、どのキーに対する回答かを示す)

	/**
	 * 検索結果(環境変数の内容)
	 */
	@Field(order = 2)
	private String value; // 検索結果(環境変数の内容)

	/* デフォルトコンストラクタ(必須） */
	/**
	 * コンストラクタ
	 */
	public SampleSearchRecord() {

	}

	/* コンストラクタ(任意) */
	
	/**
	 * コンストラクタ
	 * @param key 検索キークラス
	 * @param value 検索結果
	 */
	public SampleSearchRecord(SampleSearchKey key, String value) {
		this.key = key;
		this.value = value;
	}

	/* 各フィールドへのアクセッサ(必須) */
	/**
	 * 検索キーを取得する
	 * @return 検索キー
	 */
	public final SampleSearchKey getKey() {
		return key;
	}

	/**
	 * 検索キーを設定する
	 * @param key 検索キー
	 */
	public final void setKey(SampleSearchKey key) {
		this.key = key;
	}

	/**
	 * 検索結果取得
	 * @return 検索結果
	 */
	public final String getValue() {
		return value;
	}

	/**
	 * 検索結果を設定する
	 * @param value 検索結果
	 */
	public final void setValue(String value) {
		this.value = value;
	}

	/* hashCode()及びequals() override */
	/* (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		SampleSearchRecord other = (SampleSearchRecord) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}

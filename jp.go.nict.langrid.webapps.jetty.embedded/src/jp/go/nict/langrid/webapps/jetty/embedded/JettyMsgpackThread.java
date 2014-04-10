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

package jp.go.nict.langrid.webapps.jetty.embedded;


/**
 * Msgpackサービス用Threadクラス.<br>
 * Msgpackサービス用にmsgpackPort情報を保持する。
 * 
 * @author kishimoto
 *
 */
public class JettyMsgpackThread extends Thread {

	/**
	 * コンストラクタ
	 * @param group ThreadGroup
	 * @param target Runnable
	 */
	public JettyMsgpackThread(ThreadGroup group, Runnable target) {
		super(group, target);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	/**
	 * msgpack起動ポート番号.
	 */
	private int msgpackPort = 0;

	/**
	 * コンストラクタ.
	 * @param r 起動用Runnable
	 */
	public JettyMsgpackThread(Runnable r) {
		super(r);
	}

	/**
	 * msgpackポート番号取得
	 * @return
	 */
	public int getMsgpackPort() {
		return msgpackPort;
	}

	/**
	 * msgpackポート番号設定
	 * @param msgpackPort ポート番号
	 */
	public void setMsgpackPort(int msgpackPort) {
		this.msgpackPort = msgpackPort;
	}

}

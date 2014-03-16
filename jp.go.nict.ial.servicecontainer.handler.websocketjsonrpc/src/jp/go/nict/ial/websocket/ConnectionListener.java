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

package jp.go.nict.ial.websocket;


/**
 * WebSocket接続用イベントリスナーインターフェイスクラス.
 */
public interface ConnectionListener {
	/**
	 * 接続完了時イベント
	 * @param connection 接続情報
	 */
	void onOpen(Connection connection);
	/**
	 * 接続切断時イベント
	 * @param status 切断理由
	 */
	void onClose(int status);
	/**
	 * メッセージ受信時イベント
	 * @param message 受信した電文
	 */
	void onTextMessage(CharSequence message);
}

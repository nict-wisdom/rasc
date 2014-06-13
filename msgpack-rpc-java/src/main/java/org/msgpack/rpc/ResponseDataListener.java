package org.msgpack.rpc;

import org.msgpack.type.Value;

/**
 * ストリーミングデータ受信用リスナのインターフェイスクラス.
 * @author kishimoto
 *
 */
public interface ResponseDataListener {
	
	/**
	 * ストリーミング受信ハンドラ.
	 * @param msgid 
	 * @param result 
	 * @param error 
	 */
	public void onResponseData(int msgid, Value result, Value error);

}

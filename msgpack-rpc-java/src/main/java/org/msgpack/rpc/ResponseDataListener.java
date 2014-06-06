package org.msgpack.rpc;

import org.msgpack.type.Value;

public interface ResponseDataListener {
	public void onResponseData(int msgid, Value result, Value error);

}

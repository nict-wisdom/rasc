//
// MessagePack-RPC for Java
//
// Copyright (C) 2010 FURUHASHI Sadayuki
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package org.msgpack.rpc.transport;

import org.msgpack.type.Value;
import org.msgpack.rpc.message.MessagesEx;
import org.msgpack.rpc.ClientEx;
import org.msgpack.rpc.Session;
import org.msgpack.rpc.Server;

public class RpcMessageHandlerEx extends RpcMessageHandler {

    public RpcMessageHandlerEx(Session session) {
        this(session, null);
    }

    public RpcMessageHandlerEx(Server server) {
        this(null, server);
    }

    public RpcMessageHandlerEx(Session session, Server server) {
        super(session, server);
    }

    @Override
    public void handleMessage(MessageSendable channel, Value msg) {
        if (useThread) {
            loop.getWorkerExecutor().submit(
                    new HandleMessageTask(this, channel, msg));
        } else {
            handleMessageImpl(channel, msg);
        }
    }

    private void handleMessageImpl(MessageSendable channel, Value msg) {
        Value[] array = msg.asArrayValue().getElementArray();

        // TODO check array.length
        int type = array[0].asIntegerValue().getInt();
        if (type == MessagesEx.REQUEST) {
            // REQUEST
            int msgid = array[1].asIntegerValue().getInt();
            String method = array[2].asRawValue().getString();
            Value args = array[3];
            handleRequest(channel, msgid, method, args);

        } else if (type == MessagesEx.RESPONSE) {
            // RESPONSE
            int msgid = array[1].asIntegerValue().getInt();
            Value error = array[2];
            Value result = array[3];
            handleResponse(channel, msgid, result, error);

        } else if (type == MessagesEx.NOTIFY) {
            // NOTIFY
            String method = array[1].asRawValue().getString();
            Value args = array[2];
            handleNotify(channel, method, args);

        } else if (type == MessagesEx.RECVDATA) {
            // RESPONSE
            int msgid = array[1].asIntegerValue().getInt();
            Value error = array[2];
            Value result = array[3];
            handleResponseData(channel, msgid, result, error);

        } else {
            // FIXME error result
            throw new RuntimeException("unknown message type: " + type);
        }
    }

    private void handleRequest(MessageSendable channel, int msgid,
            String method, Value args) {
        if (server == null) {
            return; // FIXME error result
        }
        server.onRequest(channel, msgid, method, args);
    }

    private void handleNotify(MessageSendable channel, String method, Value args) {
        if (server == null) {
            return; // FIXME error result?
        }
        server.onNotify(method, args);
    }

    private void handleResponse(MessageSendable channel, int msgid,
            Value result, Value error) {
        if (session == null) {
            return; // FIXME error?
        }
        session.onResponse(msgid, result, error);
    }

    private void handleResponseData(MessageSendable channel, int msgid,
            Value result, Value error) {
        if (session == null) {
            return; // FIXME error?
        }
        ((ClientEx) session).onResponseData(msgid, result, error);
    }
}

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

package org.msgpack.rpc;

import org.msgpack.rpc.config.ClientConfig;
import org.msgpack.rpc.error.RPCError;
import org.msgpack.rpc.loop.EventLoop;
import org.msgpack.rpc.transport.MessageSendable;
import org.msgpack.type.Value;

public class ServerEx extends Server {

    public ServerEx() {
        super();
    }

    public ServerEx(ClientConfig config) {
        super(config);
    }

    public ServerEx(EventLoop loop) {
        super(loop);
    }

    public ServerEx(ClientConfig config, EventLoop loop) {
        super(config, loop);
    }

    @Override
    public void onRequest(MessageSendable channel, int msgid, String method, Value args) {
        RequestEx request = new RequestEx(channel, msgid, method, args);
        try {
            dp.dispatch(request);
        } catch (RPCError e) {
            // FIXME
            request.sendError(e.getCode(), e);
        } catch (Exception e) {
            // FIXME request.sendError("RemoteError", e.getMessage());
            if(e.getMessage() == null)
            {
                request.sendError("");
            }else{
                request.sendError(e.getMessage());
            }
        }
    }

    @Override
    public void onNotify(String method, Value args) {
        RequestEx request = new RequestEx(method, args);
        try {
            dp.dispatch(request);
        } catch (Exception e) {
            // FIXME ignore?
        }
    }
}

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

import org.msgpack.rpc.message.ResponseDataMessage;
import org.msgpack.rpc.transport.MessageSendable;
import org.msgpack.type.Value;

/**
 * Request拡張クラス.
 * @author kishimoto
 *
 */
public class RequestEx extends Request {


    /**
     * コンストラクタ.
     * @param channel 
     * @param msgid 
     * @param method 
     * @param args 
     */
    public RequestEx(MessageSendable channel, int msgid, String method,Value args) {
        super(channel, msgid, method, args);
    }

    /**
     * コンストラクタ.
     * @param method 
     * @param args 
     */
    public RequestEx(String method, Value args) {
        super(method, args);
    }

    /**
     * ストリーミングデータ送信.
     * @param result 
     * @param error 
     */
    public synchronized void sendResponseData(Object result, Object error) {
        if (channel == null) {
            return;
        }
        ResponseDataMessage msg = new ResponseDataMessage(msgid, error, result);
        channel.sendMessage(msg);
        
    }

    
}

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

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.msgpack.rpc.address.Address;
import org.msgpack.rpc.config.ClientConfig;
import org.msgpack.rpc.loop.EventLoop;
import org.msgpack.rpc.reflect.Reflect;
import org.msgpack.type.Value;

public abstract class ClientEx extends Client {

    public ClientEx(Address address, ClientConfig config, EventLoop loop,
            Reflect reflect) {
        super(address, config, loop, reflect);
    }

    public ClientEx(Address address, ClientConfig config, EventLoop loop) {
        super(address, config, loop);
    }

    public ClientEx(InetSocketAddress address, ClientConfig config,
            EventLoop loop, Reflect reflect) {
        super(address, config, loop, reflect);
    }

    public ClientEx(InetSocketAddress address, ClientConfig config,
            EventLoop loop) {
        super(address, config, loop);
    }

    public ClientEx(InetSocketAddress address, ClientConfig config) {
        super(address, config);
    }

    public ClientEx(InetSocketAddress address, EventLoop loop) {
        super(address, loop);
    }

    public ClientEx(InetSocketAddress address) {
        super(address);
    }

    public ClientEx(String host, int port, ClientConfig config, EventLoop loop)
            throws UnknownHostException {
        super(host, port, config, loop);
    }

    public ClientEx(String host, int port, ClientConfig config)
            throws UnknownHostException {
        super(host, port, config);
    }

    public ClientEx(String host, int port, EventLoop loop, Reflect reflect)
            throws UnknownHostException {
        super(host, port, loop, reflect);
    }

    public ClientEx(String host, int port, EventLoop loop)
            throws UnknownHostException {
        super(host, port, loop);
    }

    public ClientEx(String host, int port) throws UnknownHostException {
        super(host, port);
    }
    
    public abstract void onResponseData(int msgid,Value result, Value error);
    
    
}

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
package org.msgpack.rpc.loop.netty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.msgpack.MessagePack;
import org.msgpack.rpc.Session;
import org.msgpack.rpc.Server;
import org.msgpack.rpc.transport.ServerTransport;
import org.msgpack.rpc.transport.ClientTransport;
import org.msgpack.rpc.config.TcpServerConfig;
import org.msgpack.rpc.config.TcpClientConfig;

public class NettyEventLoopEx extends NettyEventLoop {

    /**
     * @param workerExecutor
     * @param ioExecutor
     * @param scheduledExecutor
     * @param messagePack
     */
    public NettyEventLoopEx(ExecutorService workerExecutor,
            ExecutorService ioExecutor,
            ScheduledExecutorService scheduledExecutor, MessagePack messagePack) {
        super(workerExecutor, ioExecutor, scheduledExecutor, messagePack);
    }

    @Override
    protected ClientTransport openTcpTransport(TcpClientConfig config,
            Session session) {
        return new NettyTcpClientTransportEx(config, session, this);
    }

    @Override
    protected ServerTransport listenTcpTransport(TcpServerConfig config,
            Server server) {
        return new NettyTcpServerTransportEx(config, server, this);
    }
    
    @Override
    public synchronized ClientSocketChannelFactory getClientFactory() {
        if (clientFactory == null) {
            clientFactory = new NioClientSocketChannelFactory(getIoExecutor(),getWorkerExecutor(),1);
        }
        return clientFactory;
    }
}

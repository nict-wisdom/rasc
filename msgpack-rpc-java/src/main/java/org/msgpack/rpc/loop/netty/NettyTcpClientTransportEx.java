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
package org.msgpack.rpc.loop.netty;

import java.util.Map;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.Channels;
import org.msgpack.rpc.Session;
import org.msgpack.rpc.config.TcpClientConfig;
import org.msgpack.rpc.transport.PooledStreamClientTransport;
import org.msgpack.rpc.transport.RpcMessageHandlerEx;

class NettyTcpClientTransportEx extends PooledStreamClientTransport<Channel, ChannelBufferOutputStream> {
    private static final String TCP_NO_DELAY = "tcpNoDelay";

    private final ClientBootstrap bootstrap;

    NettyTcpClientTransportEx(TcpClientConfig config, Session session,
            NettyEventLoop loop) {
        // TODO check session.getAddress() instanceof IPAddress
        super(config, session);

        RpcMessageHandlerEx handler = new RpcMessageHandlerEx(session);

        bootstrap = new ClientBootstrap(loop.getClientFactory());
        bootstrap.setPipelineFactory(new StreamPipelineFactory(loop.getMessagePack(), handler));
        Map<String, Object> options = config.getOptions();
        setIfNotPresent(options, TCP_NO_DELAY, Boolean.TRUE, bootstrap);
        bootstrap.setOptions(options);
       
    }

    private final ChannelFutureListener connectListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (!future.isSuccess()) {
                onConnectFailed(future.getChannel(), future.getCause());
                return;
            }
            Channel c = future.getChannel();
            c.getCloseFuture().addListener(closeListener);
            onConnected(c);
        }
    };

    private final ChannelFutureListener closeListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            Channel c = future.getChannel();
            onClosed(c);
        }
    };

    @Override
    protected void startConnection() {
        ChannelFuture f = bootstrap.connect(session.getAddress().getSocketAddress());
        f.addListener(connectListener);
    }

    @Override
    protected ChannelBufferOutputStream newPendingBuffer() {
        return new ChannelBufferOutputStream(
                ChannelBuffers.dynamicBuffer(HeapChannelBufferFactory.getInstance()));
    }

    @Override
    protected void resetPendingBuffer(ChannelBufferOutputStream b) {
        b.buffer().clear();
    }

    @Override
    protected void flushPendingBuffer(ChannelBufferOutputStream b, Channel c) {
        Channels.write(c, b.buffer());
        b.buffer().clear();
    }

    @Override
    protected void closePendingBuffer(ChannelBufferOutputStream b) {
        b.buffer().clear();
    }

    @Override
    protected void sendMessageChannel(Channel c, Object msg) {
        Channels.write(c, msg);
    }

    @Override
    protected void closeChannel(Channel c) {
        c.close();
    }

    private static void setIfNotPresent(Map<String, Object> options,
            String key, Object value, ClientBootstrap bootstrap) {
        if (!options.containsKey(key)) {
            bootstrap.setOption(key, value);
        }
    }

    @Override
    public void close() {
        super.close();
        bootstrap.shutdown();
    }
}

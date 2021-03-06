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

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.msgpack.rpc.Server;
import org.msgpack.rpc.address.Address;
import org.msgpack.rpc.config.TcpServerConfig;
import org.msgpack.rpc.transport.RpcMessageHandler;
import org.msgpack.rpc.transport.ServerTransport;

/**
 * NettyTcpServerTransport拡張クラス.
 * @author kishimoto
 *
 */
class NettyTcpServerTransportEx implements ServerTransport {
    private Channel listenChannel;
    private final static String CHILD_TCP_NODELAY = "child.tcpNoDelay";
    private final static String REUSE_ADDRESS = "reuseAddress";
    private final ServerBootstrap bootstrap;

    /**
     * @param config 
     * @param server 
     * @param loop 
     */
    public NettyTcpServerTransportEx(TcpServerConfig config, Server server, NettyEventLoop loop) {
        if (server == null) {
            throw new IllegalArgumentException("Server must not be null");
        }

        Address address = config.getListenAddress();
        RpcMessageHandler handler = new RpcMessageHandler(server);
        handler.useThread(true);

        bootstrap = new ServerBootstrap(loop.getServerFactory());
        bootstrap.setPipelineFactory(new StreamPipelineFactory(loop.getMessagePack(), handler));
        final Map<String, Object> options = config.getOptions();
        setIfNotPresent(options, CHILD_TCP_NODELAY, Boolean.TRUE, bootstrap);
        setIfNotPresent(options, REUSE_ADDRESS, Boolean.TRUE, bootstrap);
        bootstrap.setOptions(options);
        this.listenChannel = bootstrap.bind(address.getSocketAddress());
    }

    /* (非 Javadoc)
     * @see org.msgpack.rpc.transport.ServerTransport#close()
     */
    @Override
    public void close() {
        listenChannel.close();
        bootstrap.shutdown();
    }

    /**
     * @param options 
     * @param key 
     * @param value 
     * @param bootstrap 
     */
    private static void setIfNotPresent(Map<String, Object> options,
            String key, Object value, ServerBootstrap bootstrap) {
        if (!options.containsKey(key)) {
            bootstrap.setOption(key, value);
        }
    }
}

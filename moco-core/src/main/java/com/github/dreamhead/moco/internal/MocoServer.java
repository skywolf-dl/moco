package com.github.dreamhead.moco.internal;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.Callable;

import static com.github.dreamhead.moco.internal.Awaiter.awaitUntil;

public class MocoServer {
    private static final int DEFAULT_TIMEOUT = 3;
    private EventLoopGroup group;
    private ChannelFuture future;
    private InetSocketAddress address;

    public MocoServer() {
        group = new NioEventLoopGroup();
    }

    public int start(final int port, final ChannelInitializer<? extends Channel> pipelineFactory) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(pipelineFactory);

        try {
            future = bootstrap.bind(port).sync();
            SocketAddress socketAddress = future.channel().localAddress();
            address = (InetSocketAddress) socketAddress;
            return address.getPort();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        doStop();
        if (address != null) {
            awaitUntil(serverIsClosed(address), DEFAULT_TIMEOUT);
            address = null;
        }
    }

    private void doStop() {
        if (future != null) {
            future.channel().close().syncUninterruptibly();
            future = null;
        }

        if (group != null) {
            group.shutdownGracefully();
            group = null;
        }
    }

    private Callable<Boolean> serverIsClosed(final InetSocketAddress address) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    Socket socket = new Socket();
                    socket.connect(address);
                    return false;
                } catch (SocketException e) {
                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}

// SPDX-FileCopyrightText: 2026 Axle Duggan (axlecoffee) <contact@axle.coffee>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package coffee.axle.proxy;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
//? if <1.21.11 {
import net.minecraft.Util;
//?}

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class TestPing {
    public String state = "";
    public long latency = -1;
    public boolean failed = false;

    private long pingSentAt;
    private Connection pingDestination = null;
    private Proxy proxy;
    private static final ThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(5,
            (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());

    public void run(String ip, int port, Proxy proxy) {
        this.proxy = proxy;
        this.latency = -1;
        this.failed = false;
        TestPing.EXECUTOR.submit(() -> ping(ip, port));
    }

    private void ping(String ip, int port) {
        state = Component.translatable("ui.coffeeproxy.ping.pinging", ip).getString();
        Connection connection;
        try {
            connection = createTestConnection(Proxy.resolveAddress(ip), port);
        } catch (UnknownHostException e) {
            failed = true;
            state = ChatFormatting.RED + Component.translatable("ui.coffeeproxy.err.cantConnect").getString();
            return;
        } catch (Exception e) {
            failed = true;
            state = ChatFormatting.RED + Component.translatable("ui.coffeeproxy.err.cantPing", ip).getString();
            return;
        }
        pingDestination = connection;

        connection.initiateServerboundStatusConnection(ip, port, new ClientStatusPacketListener() {
            private boolean successful;

            @Override
            public void handlePongResponse(ClientboundPongResponsePacket packet) {
                successful = true;
                pingDestination = null;
                long pingToServer = Util.getMillis() - pingSentAt;
                latency = pingToServer;
                state = Component.translatable("ui.coffeeproxy.ping.showPing", pingToServer).getString();
                connection.disconnect(Component.translatable("multiplayer.status.finished"));
            }

            @Override
            public void handleStatusResponse(ClientboundStatusResponsePacket packet) {
                pingSentAt = Util.getMillis();
                connection.send(new ServerboundPingRequestPacket(pingSentAt));
            }

            @Override
            public void onDisconnect(DisconnectionDetails details) {
                pingDestination = null;
                if (!this.successful) {
                    failed = true;
                    state = ChatFormatting.RED
                            + Component.translatable("ui.coffeeproxy.err.cantPingReason", ip, details.reason().getString())
                                    .getString();
                }
            }

            @Override
            public boolean isAcceptingMessages() {
                return true;
            }

            @Override
            public ConnectionProtocol protocol() {
                return ConnectionProtocol.STATUS;
            }
        });

        // vanilla sends this right after initiate (it only sets up protocols + handshake)
        connection.send(ServerboundStatusRequestPacket.INSTANCE);
    }

    private Connection createTestConnection(InetAddress address, int port) {
        final Connection connection = new Connection(PacketFlow.CLIENTBOUND);

        //? if <1.21.11 {
        new Bootstrap()
                .group(Connection.NETWORK_WORKER_GROUP.get())
        //?} else {
        /*var eventLoopHolder = net.minecraft.server.network.EventLoopGroupHolder.remote(false);
        new Bootstrap()
                .group(eventLoopHolder.eventLoopGroup())
        *///?}
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        try {
                            channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                        } catch (ChannelException ignored) {
                        }

                        ChannelPipeline pipeline = channel.pipeline().addLast("timeout",
                                new ReadTimeoutHandler(30));

                        // added before configureSerialization so the mixin sees it and skips this channel
                        pipeline.addFirst(proxy.getHandler());

                        Connection.configureSerialization(pipeline, PacketFlow.CLIENTBOUND, false, null);
                        connection.configurePacketHandler(pipeline);
                    }
                })
                //? if <1.21.11 {
                .channel(NioSocketChannel.class)
                //?} else {
                /*.channel(eventLoopHolder.channelCls())
                *///?}
                .connect(address, port)
                .syncUninterruptibly();
        return connection;
    }

    public void pingPendingNetworks() {
        if (pingDestination != null) {
            if (pingDestination.isConnected()) {
                pingDestination.tick();
            } else {
                pingDestination.handleDisconnection();
            }
        }
    }
}

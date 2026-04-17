package coffee.axle.proxy;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
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
import net.minecraft.network.protocol.status.StatusProtocols;
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

    private long pingSentAt;
    private Connection pingDestination = null;
    private Proxy proxy;
    private static final ThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(5,
            (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());

    public void run(String ip, int port, Proxy proxy) {
        this.proxy = proxy;
        TestPing.EXECUTOR.submit(() -> ping(ip, port));
    }

    private void ping(String ip, int port) {
        state = Component.translatable("ui.coffeeproxy.ping.pinging", ip).getString();
        Connection connection;
        try {
            connection = createTestConnection(Proxy.resolveAddress(ip), port);
        } catch (UnknownHostException e) {
            state = ChatFormatting.RED + Component.translatable("ui.coffeeproxy.err.cantConnect").getString();
            return;
        } catch (Exception e) {
            state = ChatFormatting.RED + Component.translatable("ui.coffeeproxy.err.cantPing", ip).getString();
            return;
        }
        pingDestination = connection;

        connection.setupInboundProtocol(StatusProtocols.CLIENTBOUND, new ClientStatusPacketListener() {
            private boolean successful;

            @Override
            public void handlePongResponse(ClientboundPongResponsePacket packet) {
                successful = true;
                pingDestination = null;
                //? if <1.21.11 {
                long pingToServer = Util.getMillis() - pingSentAt;
                //?} else {
                /*long pingToServer = System.currentTimeMillis() - pingSentAt;
                *///?}
                state = Component.translatable("ui.coffeeproxy.ping.showPing", pingToServer).getString();
                connection.disconnect(Component.translatable("multiplayer.status.finished"));
            }

            @Override
            public void handleStatusResponse(ClientboundStatusResponsePacket packet) {
                //? if <1.21.11 {
                pingSentAt = Util.getMillis();
                //?} else {
                /*pingSentAt = System.currentTimeMillis();
                *///?}
                connection.send(new ServerboundPingRequestPacket(pingSentAt));
            }

            @Override
            public void onDisconnect(DisconnectionDetails details) {
                pingDestination = null;
                if (!this.successful) {
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

        try {
            connection.send(ServerboundStatusRequestPacket.INSTANCE);
        } catch (Throwable throwable) {
            state = ChatFormatting.RED + Component.translatable("ui.coffeeproxy.err.cantPing", ip).getString();
        }
    }

    private Connection createTestConnection(InetAddress address, int port) {
        final Connection connection = new Connection(PacketFlow.CLIENTBOUND);

        Coffeeproxy.suppressProxyMixin = true;
        try {
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
                            Connection.configureSerialization(pipeline, PacketFlow.CLIENTBOUND, false, null);
                            connection.configurePacketHandler(pipeline);

                            switch (proxy.type) {
                                case SOCKS5 -> channel.pipeline().addFirst(new Socks5ProxyHandler(
                                        proxy.resolveProxyAddress(),
                                        proxy.username.isEmpty() ? null : proxy.username,
                                        proxy.password.isEmpty() ? null : proxy.password));
                                case SOCKS4 -> channel.pipeline().addFirst(new Socks4ProxyHandler(
                                        proxy.resolveProxyAddress(),
                                        proxy.username.isEmpty() ? null : proxy.username));
                                case HTTP -> channel.pipeline().addFirst(new io.netty.handler.proxy.HttpProxyHandler(
                                        proxy.resolveProxyAddress(),
                                        proxy.username.isEmpty() ? null : proxy.username,
                                        proxy.password.isEmpty() ? "" : proxy.password));
                            }
                        }
                    })
                    //? if <1.21.11 {
                    .channel(NioSocketChannel.class)
                    //?} else {
                    /*.channel(eventLoopHolder.channelCls())
                    *///?}
                    .connect(address, port)
                    .syncUninterruptibly();
        } finally {
            Coffeeproxy.suppressProxyMixin = false;
        }
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

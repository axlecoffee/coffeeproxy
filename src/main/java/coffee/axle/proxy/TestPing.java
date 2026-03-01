package coffee.axle.proxy;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.network.NetworkPhase;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class TestPing {
    public String state = "";

    private long pingSentAt;
    private ClientConnection pingDestination = null;
    private Proxy proxy;
    private static final ThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(5,
            (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());

    public void run(String ip, int port, Proxy proxy) {
        this.proxy = proxy;
        TestPing.EXECUTOR.submit(() -> ping(ip, port));
    }

    private void ping(String ip, int port) {
        state = Text.translatable("ui.coffeeproxy.ping.pinging", ip).getString();
        ClientConnection clientConnection;
        try {
            clientConnection = createTestClientConnection(Proxy.resolveAddress(ip), port);
        } catch (UnknownHostException e) {
            state = Formatting.RED + Text.translatable("ui.coffeeproxy.err.cantConnect").getString();
            return;
        } catch (Exception e) {
            state = Formatting.RED + Text.translatable("ui.coffeeproxy.err.cantPing", ip).getString();
            return;
        }
        pingDestination = clientConnection;

        clientConnection.connect(ip, port, new ClientQueryPacketListener() {
            private boolean successful;

            @Override
            public void onPingResult(PingResultS2CPacket packet) {
                successful = true;
                pingDestination = null;
                long pingToServer = Util.getMeasuringTimeMs() - pingSentAt;
                state = Text.translatable("ui.coffeeproxy.ping.showPing", pingToServer).getString();
                clientConnection.disconnect(Text.translatable("multiplayer.status.finished"));
            }

            @Override
            public void onResponse(QueryResponseS2CPacket packet) {
                pingSentAt = Util.getMeasuringTimeMs();
                clientConnection.send(new QueryPingC2SPacket(pingSentAt));
            }

            @Override
            public void onDisconnected(DisconnectionInfo info) {
                pingDestination = null;
                if (!this.successful) {
                    state = Formatting.RED
                            + Text.translatable("ui.coffeeproxy.err.cantPingReason", ip, info.reason().getString())
                                    .getString();
                }
            }

            @Override
            public boolean isConnectionOpen() {
                return true;
            }

            @Override
            public NetworkPhase getPhase() {
                return NetworkPhase.STATUS;
            }
        });

        try {
            clientConnection.send(QueryRequestC2SPacket.INSTANCE);
        } catch (Throwable throwable) {
            state = Formatting.RED + Text.translatable("ui.coffeeproxy.err.cantPing", ip).getString();
        }
    }

    private ClientConnection createTestClientConnection(InetAddress address, int port) {
        final ClientConnection clientConnection = new ClientConnection(NetworkSide.CLIENTBOUND);

        Coffeeproxy.suppressProxyMixin = true;
        try {
            new Bootstrap()
                    .group(ClientConnection.CLIENT_IO_GROUP.get())
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) {
                            try {
                                channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                            } catch (ChannelException ignored) {
                            }

                            ChannelPipeline pipeline = channel.pipeline().addLast("timeout",
                                    new ReadTimeoutHandler(30));
                            ClientConnection.addHandlers(pipeline, NetworkSide.CLIENTBOUND, false, null);
                            clientConnection.addFlowControlHandler(pipeline);

                            if (proxy.type == Proxy.ProxyType.SOCKS5) {
                                channel.pipeline().addFirst(new Socks5ProxyHandler(
                                        proxy.resolveProxyAddress(),
                                        proxy.username.isEmpty() ? null : proxy.username,
                                        proxy.password.isEmpty() ? null : proxy.password));
                            } else {
                                channel.pipeline().addFirst(new Socks4ProxyHandler(
                                        proxy.resolveProxyAddress(),
                                        proxy.username.isEmpty() ? null : proxy.username));
                            }
                        }
                    })
                    .channel(NioSocketChannel.class)
                    .connect(address, port)
                    .syncUninterruptibly();
        } finally {
            Coffeeproxy.suppressProxyMixin = false;
        }
        return clientConnection;
    }

    public void pingPendingNetworks() {
        if (pingDestination != null) {
            if (pingDestination.isOpen()) {
                pingDestination.tick();
            } else {
                pingDestination.handleDisconnection();
            }
        }
    }
}

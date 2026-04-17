package coffee.axle.proxy.mixin;

import coffee.axle.proxy.Coffeeproxy;
import coffee.axle.proxy.Proxy;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ClientConnectionInit {
    @Inject(method = "configureSerialization", at = @At("HEAD"))
    private static void onConfigureSerialization(ChannelPipeline pipeline, PacketFlow flow, boolean local,
            BandwidthDebugMonitor bandwidthDebugMonitor, CallbackInfo ci) {
        if (local || Coffeeproxy.suppressProxyMixin)
            return;

        Proxy proxy = Coffeeproxy.proxy;

        if (Coffeeproxy.proxyEnabled) {
            Coffeeproxy.lastUsedProxy = proxy;

            switch (proxy.type) {
                case SOCKS5 -> pipeline.addFirst(new Socks5ProxyHandler(
                        proxy.resolveProxyAddress(),
                        proxy.username.isEmpty() ? null : proxy.username,
                        proxy.password.isEmpty() ? null : proxy.password));
                case SOCKS4 -> pipeline.addFirst(new Socks4ProxyHandler(
                        proxy.resolveProxyAddress(),
                        proxy.username.isEmpty() ? null : proxy.username));
                case HTTP -> pipeline.addFirst(new io.netty.handler.proxy.HttpProxyHandler(
                        proxy.resolveProxyAddress(),
                        proxy.username.isEmpty() ? null : proxy.username,
                        proxy.password.isEmpty() ? "" : proxy.password));
            }
        } else {
            Coffeeproxy.lastUsedProxy = new Proxy();
        }

        if (Coffeeproxy.proxyMenuButton != null) {
            Coffeeproxy.proxyMenuButton.setMessage(Component.literal("Proxy: " + Coffeeproxy.getLastUsedProxyIp()));
        }
    }
}

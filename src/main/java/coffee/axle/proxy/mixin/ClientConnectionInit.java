package coffee.axle.proxy.mixin;

import coffee.axle.proxy.Coffeeproxy;
import coffee.axle.proxy.Proxy;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.handler.PacketSizeLogger;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionInit {
    @Inject(method = "addHandlers", at = @At("HEAD"))
    private static void onAddHandlers(ChannelPipeline pipeline, NetworkSide side, boolean local,
            PacketSizeLogger packetSizeLogger, CallbackInfo ci) {
        if (local || Coffeeproxy.suppressProxyMixin)
            return;

        Proxy proxy = Coffeeproxy.proxy;

        if (Coffeeproxy.proxyEnabled) {
            Coffeeproxy.lastUsedProxy = proxy;

            if (proxy.type == Proxy.ProxyType.SOCKS5) {
                pipeline.addFirst(new Socks5ProxyHandler(
                        proxy.resolveProxyAddress(),
                        proxy.username.isEmpty() ? null : proxy.username,
                        proxy.password.isEmpty() ? null : proxy.password));
            } else {
                pipeline.addFirst(new Socks4ProxyHandler(
                        proxy.resolveProxyAddress(),
                        proxy.username.isEmpty() ? null : proxy.username));
            }
        } else {
            Coffeeproxy.lastUsedProxy = new Proxy();
        }

        if (Coffeeproxy.proxyMenuButton != null) {
            Coffeeproxy.proxyMenuButton.setMessage(Text.literal("Proxy: " + Coffeeproxy.getLastUsedProxyIp()));
        }
    }
}

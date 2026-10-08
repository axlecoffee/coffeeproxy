// SPDX-FileCopyrightText: 2026 Axle Duggan (axlecoffee) <contact@axle.coffee>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package coffee.axle.proxy.mixin;

import coffee.axle.proxy.Coffeeproxy;
import coffee.axle.proxy.Proxy;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.proxy.ProxyHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.BandwidthDebugMonitor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ClientConnectionInit {
    @Inject(method = "configureSerialization", at = @At("HEAD"))
    private static void onConfigureSerialization(ChannelPipeline pipe, PacketFlow flow, boolean local, BandwidthDebugMonitor bandwidthDebugMonitor, CallbackInfo ci) {
        if (local || pipe.get(ProxyHandler.class) != null)
            return;

        Proxy proxy = Coffeeproxy.proxy;

        if (Coffeeproxy.proxyEnabled) {
            pipe.addFirst(proxy.getHandler());
        }
    }
}

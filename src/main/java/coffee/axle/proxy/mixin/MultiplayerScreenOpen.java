// SPDX-FileCopyrightText: 2026 Axle Duggan (axlecoffee) <contact@axle.coffee>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package coffee.axle.proxy.mixin;

import coffee.axle.proxy.Coffeeproxy;
import coffee.axle.proxy.Config;
import coffee.axle.proxy.GuiProxyList;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class MultiplayerScreenOpen extends Screen {

    @Unique
    private Button coffeeProxy$proxyMenuButton;

    protected MultiplayerScreenOpen(Component title) {
        super(title);
    }

    // init not repositionElements on 1.21.4/5/8: JoinMultiplayerScreen only declares the latter
    // on 1.21.10+, loom can't remap inherited targets so the jar would keep mojmap names.
    // 1.21.10+ overrides repositionElements WITHOUT super.rebuildWidgets, so init() never
    // re-fires on resize there -> the button would drift. Both injects needed.
    //? if <1.21.10 {
    /*@Inject(method = "init()V", at = @At("TAIL"))
    private void repositionProxyButton(CallbackInfo ci) {
        handleProxyButton();
    }
    *///?}
    //? if >=1.21.10 {
    @Inject(method = "repositionElements()V", at = @At("TAIL"))
    private void repositionProxyButton(CallbackInfo ci) {
        handleProxyButton();
    }
    //?}

    @Unique
    private void handleProxyButton() {
        if (coffeeProxy$proxyMenuButton == null) {
            JoinMultiplayerScreen ms = (JoinMultiplayerScreen) (Object) this;
            coffeeProxy$proxyMenuButton = Button
                    .builder(Component.literal("Proxy: "), (buttonWidget) -> {
                        Coffeeproxy.openScreen(new GuiProxyList(ms));
                    }).size(120, 20).build();
        }
        // suggestion (also commission?) from a random guy to "hide" it - like i guess man
        if (!FabricLoader.getInstance().isModLoaded("modmenu") || Config.showMultiplayerButton) {
            if (!this.children().contains(coffeeProxy$proxyMenuButton)) {
                this.addRenderableWidget(coffeeProxy$proxyMenuButton);
            }
        }

        coffeeProxy$proxyMenuButton.setMessage(Component.literal(
                "Proxy: " + (Config.activeName.isEmpty() ? "none" : Config.activeName)));
        coffeeProxy$proxyMenuButton.setPosition(this.width - 125, 5);
    }
}
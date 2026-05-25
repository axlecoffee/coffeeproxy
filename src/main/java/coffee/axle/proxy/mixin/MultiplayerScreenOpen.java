package coffee.axle.proxy.mixin;

import coffee.axle.proxy.Config;
import coffee.axle.proxy.Coffeeproxy;
import coffee.axle.proxy.GuiProxy;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
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

    @Inject(method = "repositionElements()V", at = @At("TAIL"))
    private void repositionProxyButton(CallbackInfo ci) {
        if (coffeeProxy$proxyMenuButton == null) {
            String playerName = Minecraft.getInstance().getUser().getName();
            if (!playerName.equals(Config.lastPlayerName)) {
                Config.lastPlayerName = playerName;
                if (Config.accounts.containsKey(playerName)) {
                    Coffeeproxy.proxy = Config.accounts.get(playerName);
                } else {
                    if (Config.accounts.containsKey("")) {
                        Coffeeproxy.proxy = Config.accounts.get("");
                    }
                }
            }

            JoinMultiplayerScreen ms = (JoinMultiplayerScreen) (Object) this;
            coffeeProxy$proxyMenuButton = Button
                    .builder(Component.literal("Proxy: " + Coffeeproxy.getLastUsedProxyIp()), (buttonWidget) -> {
                        //? if <26.2 {
                        Minecraft.getInstance().setScreen(new GuiProxy(ms));
                        //?} else {
                        /*Minecraft.getInstance().setScreenAndShow(new GuiProxy(ms));
                        *///?}
                    }).size(120, 20).build();
        }

        if (!FabricLoader.getInstance().isModLoaded("modmenu") || Config.showMultiplayerButton) {
            if (!this.children().contains(coffeeProxy$proxyMenuButton)) {
                this.addRenderableWidget(coffeeProxy$proxyMenuButton);
            }
        }

        Coffeeproxy.proxyMenuButton = coffeeProxy$proxyMenuButton;
        coffeeProxy$proxyMenuButton.setPosition(this.width - 125, 5);
    }
}

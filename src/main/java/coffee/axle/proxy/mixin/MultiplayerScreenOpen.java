package coffee.axle.proxy.mixin;

import coffee.axle.proxy.Config;
import coffee.axle.proxy.Coffeeproxy;
import coffee.axle.proxy.GuiProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class MultiplayerScreenOpen extends Screen {

    protected MultiplayerScreenOpen(Component title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void multiplayerGuiOpen(CallbackInfo ci) {
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
        Coffeeproxy.proxyMenuButton = Button
                .builder(Component.literal("Proxy: " + Coffeeproxy.getLastUsedProxyIp()), (buttonWidget) -> {
                    //? if <26.2 {
                    Minecraft.getInstance().setScreen(new GuiProxy(ms));
                    //?} else {
                    /*Minecraft.getInstance().setScreenAndShow(new GuiProxy(ms));
                    *///?}
                }).bounds(this.width - 125, 5, 120, 20).build();

        this.addRenderableWidget(Coffeeproxy.proxyMenuButton);
    }
}

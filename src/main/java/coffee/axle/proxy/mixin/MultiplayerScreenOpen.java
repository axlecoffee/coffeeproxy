package coffee.axle.proxy.mixin;

import coffee.axle.proxy.Config;
import coffee.axle.proxy.Coffeeproxy;
import coffee.axle.proxy.GuiProxy;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenOpen {
    @Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;updateButtonActivationStates()V"))
    public void multiplayerGuiOpen(CallbackInfo ci) {
        String playerName = MinecraftClient.getInstance().getSession().getUsername();
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

        MultiplayerScreen ms = (MultiplayerScreen) (Object) this;
        Coffeeproxy.proxyMenuButton = ButtonWidget
                .builder(Text.literal("Proxy: " + Coffeeproxy.getLastUsedProxyIp()), (buttonWidget) -> {
                    MinecraftClient.getInstance().setScreen(new GuiProxy(ms));
                }).dimensions(ms.width - 125, 5, 120, 20).build();

        ScreenAccessor si = (ScreenAccessor) ms;
        si.getDrawables().add(Coffeeproxy.proxyMenuButton);
        si.getSelectables().add(Coffeeproxy.proxyMenuButton);
        si.getChildren().add(Coffeeproxy.proxyMenuButton);
    }
}

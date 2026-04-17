package coffee.axle.proxy;

import net.minecraft.client.Minecraft;
//? if <26 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
//? if >=1.21.10 {
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.apache.commons.lang3.StringUtils;

public class GuiProxy extends Screen {
    private Proxy.ProxyType currentType = Proxy.ProxyType.SOCKS5;

    private EditBox ipPort;
    private EditBox username;
    private EditBox password;
    private Checkbox enabledCheck;

    private final Screen parentScreen;

    private String msg = "";

    private int[] positionY;
    private int positionX;

    private TestPing testPing = new TestPing();

    private static final String TEXT_PROXY = Component.translatable("ui.coffeeproxy.options.proxy").getString();

    public GuiProxy(Screen parentScreen) {
        super(Component.literal(TEXT_PROXY));
        this.parentScreen = parentScreen;
    }

    private static boolean isValidIpPort(String ipP) {
        String[] split = ipP.split(":");
        if (split.length > 1) {
            if (!StringUtils.isNumeric(split[1]))
                return false;
            int port = Integer.parseInt(split[1]);
            if (port < 0 || port > 0xFFFF)
                return false;
            return true;
        } else {
            return false;
        }
    }

    private boolean checkProxy() {
        if (!isValidIpPort(ipPort.getValue())) {
            msg = ChatFormatting.RED + Component.translatable("ui.coffeeproxy.options.invalidIpPort").getString();
            this.ipPort.setFocused(true);
            return false;
        }
        return true;
    }

    private void centerButtons(int amount, int buttonLength, int gap) {
        positionX = (this.width / 2) - (buttonLength / 2);
        positionY = new int[amount];
        int center = (this.height + amount * gap) / 2;
        int buttonStarts = center - (amount * gap);
        for (int i = 0; i != amount; i++) {
            positionY[i] = buttonStarts + (gap * i);
        }
    }

    @Override
    //? if >=1.21.10 {
    public boolean keyPressed(KeyEvent keyEvent) {
        super.keyPressed(keyEvent);
    //?} else {
    /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
    *///?}
        msg = "";
        testPing.state = "";
        return true;
    }

    @Override
    //? if <26 {
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    //?} else {
    /*public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    *///?}

        if (enabledCheck.selected() && !isValidIpPort(ipPort.getValue())) {
            //? if >=1.21.10 {
            enabledCheck.onPress((InputWithModifiers) new KeyEvent(0, 0, 0));
            //?} else {
            /*enabledCheck.onPress();
            *///?}
        }

        //? if <26 {
        guiGraphics.drawString(this.font, Component.translatable("ui.coffeeproxy.options.proxyType").getString(),
                positionX, positionY[1] - 10, 0xFFA0A0A0);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("ui.coffeeproxy.options.auth").getString(), this.width / 2, positionY[3] + 8,
                0xFFFFFFFF);
        guiGraphics.drawString(this.font, Component.translatable("ui.coffeeproxy.options.ipPort").getString(),
                positionX, positionY[2] - 10, 0xFFA0A0A0);

        this.ipPort.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (currentType == Proxy.ProxyType.SOCKS4) {
            guiGraphics.drawString(this.font, Component.translatable("ui.coffeeproxy.auth.id").getString(),
                    positionX, positionY[4] - 10, 0xFFA0A0A0);
            this.username.render(guiGraphics, mouseX, mouseY, partialTicks);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("ui.coffeeproxy.auth.password").getString(),
                    positionX, positionY[5] - 10, 0xFFA0A0A0);
            guiGraphics.drawString(this.font, Component.translatable("ui.coffeeproxy.auth.username").getString(),
                    positionX, positionY[4] - 10, 0xFFA0A0A0);
            this.username.render(guiGraphics, mouseX, mouseY, partialTicks);
            this.password.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        guiGraphics.drawCenteredString(this.font, !msg.isEmpty() ? msg : testPing.state, this.width / 2,
                positionY[6] + 5, 0xFFA0A0A0);
        //?} else {
        /*guiGraphics.text(this.font, Component.translatable("ui.coffeeproxy.options.proxyType").getString(),
                positionX, positionY[1] - 10, 0xFFA0A0A0);
        guiGraphics.centeredText(this.font,
                Component.translatable("ui.coffeeproxy.options.auth").getString(), this.width / 2, positionY[3] + 8,
                0xFFFFFFFF);
        guiGraphics.text(this.font, Component.translatable("ui.coffeeproxy.options.ipPort").getString(),
                positionX, positionY[2] - 10, 0xFFA0A0A0);

        this.ipPort.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        if (currentType == Proxy.ProxyType.SOCKS4) {
            guiGraphics.text(this.font, Component.translatable("ui.coffeeproxy.auth.id").getString(),
                    positionX, positionY[4] - 10, 0xFFA0A0A0);
            this.username.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        } else {
            guiGraphics.text(this.font, Component.translatable("ui.coffeeproxy.auth.password").getString(),
                    positionX, positionY[5] - 10, 0xFFA0A0A0);
            guiGraphics.text(this.font, Component.translatable("ui.coffeeproxy.auth.username").getString(),
                    positionX, positionY[4] - 10, 0xFFA0A0A0);
            this.username.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTicks);
            this.password.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }

        guiGraphics.centeredText(this.font, !msg.isEmpty() ? msg : testPing.state, this.width / 2,
                positionY[6] + 5, 0xFFA0A0A0);
        *///?}
    }

    @Override
    public void tick() {
        testPing.pingPendingNetworks();
    }

    @Override
    public void init() {
        int buttonLength = 160;
        centerButtons(10, buttonLength, 32);

        String savedIpPort = this.ipPort != null ? this.ipPort.getValue() : Coffeeproxy.proxy.ipPort;
        String savedUsername = this.username != null ? this.username.getValue() : Coffeeproxy.proxy.username;
        String savedPassword = this.password != null ? this.password.getValue() : Coffeeproxy.proxy.password;
        if (this.ipPort == null) {
            currentType = Coffeeproxy.proxy.type;
        }

        Button proxyType = Button.builder(Component.literal(currentType.name()), button -> {
            Proxy.ProxyType[] values = Proxy.ProxyType.values();
            currentType = values[(currentType.ordinal() + 1) % values.length];
            button.setMessage(Component.literal(currentType.name()));
        }).bounds(positionX, positionY[1], buttonLength, 20).build();
        this.addRenderableWidget(proxyType);

        this.ipPort = new EditBox(this.font, positionX, positionY[2], buttonLength, 20,
                Component.literal(""));
        this.ipPort.setValue(savedIpPort);
        this.ipPort.setMaxLength(1024);
        this.ipPort.setFocused(true);
        this.addWidget(this.ipPort);

        this.username = new EditBox(this.font, positionX, positionY[4], buttonLength, 20,
                Component.literal(""));
        this.username.setMaxLength(255);
        this.username.setValue(savedUsername);
        this.addWidget(this.username);

        this.password = new EditBox(this.font, positionX, positionY[5], buttonLength, 20,
                Component.literal(""));
        this.password.setMaxLength(255);
        this.password.setValue(savedPassword);
        this.addWidget(this.password);

        int posXButtons = (this.width / 2) - (((buttonLength / 2) * 3) / 2);

        Button apply = Button.builder(Component.translatable("ui.coffeeproxy.options.apply"), button -> {
            if (checkProxy()) {
                Coffeeproxy.proxy = new Proxy(currentType, ipPort.getValue(), username.getValue(), password.getValue());
                Coffeeproxy.proxyEnabled = enabledCheck.selected();
                Config.setDefaultProxy(Coffeeproxy.proxy);
                Config.saveConfig();
                //? if <26.2 {
                Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(new TitleScreen()));
                //?} else {
                /*Minecraft.getInstance().setScreenAndShow(new JoinMultiplayerScreen(new TitleScreen()));
                *///?}
            }
        }).bounds(posXButtons, positionY[8], buttonLength / 2 - 3, 20).build();
        this.addRenderableWidget(apply);

        Button test = Button.builder(Component.translatable("ui.coffeeproxy.options.test"), (button) -> {
            if (ipPort.getValue().isEmpty() || ipPort.getValue().equalsIgnoreCase("none")) {
                msg = ChatFormatting.RED + Component.translatable("ui.coffeeproxy.err.specProxy").getString();
                return;
            }
            if (checkProxy()) {
                testPing = new TestPing();
                testPing.run("mc.hypixel.net", 25565,
                        new Proxy(currentType, ipPort.getValue(), username.getValue(), password.getValue()));
            }
        }).bounds(posXButtons + buttonLength / 2 + 3, positionY[8], buttonLength / 2 - 3, 20).build();
        this.addRenderableWidget(test);

        Checkbox.Builder checkboxBuilder = Checkbox
                .builder(Component.translatable("ui.coffeeproxy.options.proxyEnabled"), this.font);
        checkboxBuilder.pos(
                (this.width / 2)
                        - (15 + font.width(Component.translatable("ui.coffeeproxy.options.proxyEnabled"))) / 2,
                positionY[7]);
        boolean shouldBeChecked = this.enabledCheck != null ? this.enabledCheck.selected() : Coffeeproxy.proxyEnabled;
        if (shouldBeChecked) {
            checkboxBuilder.selected(true);
        }
        this.enabledCheck = checkboxBuilder.build();
        this.addRenderableWidget(this.enabledCheck);

        Button cancel = Button.builder(Component.translatable("ui.coffeeproxy.options.cancel"), (button) -> {
            //? if <26.2 {
            Minecraft.getInstance().setScreen(parentScreen);
            //?} else {
            /*Minecraft.getInstance().setScreenAndShow(parentScreen);
            *///?}
        }).bounds(posXButtons + (buttonLength / 2 + 3) * 2, positionY[8], buttonLength / 2 - 3, 20).build();
        this.addRenderableWidget(cancel);
    }

    @Override
    public void onClose() {
        msg = "";
    }
}

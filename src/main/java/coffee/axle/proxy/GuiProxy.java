// SPDX-FileCopyrightText: 2026 Axle Duggan (axlecoffee) <contact@axle.coffee>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package coffee.axle.proxy;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
//? if >=1.21.10 {
import net.minecraft.client.input.KeyEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.apache.commons.lang3.StringUtils;

public class GuiProxy extends Screen {
    private Proxy.ProxyType currentType = Proxy.ProxyType.SOCKS5;

    private EditBox name;
    private EditBox ipPort;
    private EditBox username;
    private EditBox password;

    private final Screen parentScreen;
    private final String editName;

    private String msg = "";

    private int[] positionY;
    private int positionX;

    private TestPing testPing = new TestPing();

    private static final String TEXT_PROXY = Component.translatable("ui.coffeeproxy.options.proxy").getString();

    public GuiProxy(Screen parentScreen, String editName) {
        super(Component.literal(TEXT_PROXY));
        this.parentScreen = parentScreen;
        this.editName = editName;
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
    /*public void extractRenderState(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    *///?}

        Draw.text(guiGraphics, this.font, Component.translatable("ui.coffeeproxy.options.name").getString(),
                positionX, positionY[1] - 10, 0xFFA0A0A0);
        Draw.text(guiGraphics, this.font, Component.translatable("ui.coffeeproxy.options.proxyType").getString(),
                positionX, positionY[2] - 10, 0xFFA0A0A0);
        Draw.centeredText(guiGraphics, this.font,
                Component.translatable("ui.coffeeproxy.options.auth").getString(), this.width / 2, positionY[4] + 8,
                0xFFFFFFFF);
        Draw.text(guiGraphics, this.font, Component.translatable("ui.coffeeproxy.options.ipPort").getString(),
                positionX, positionY[3] - 10, 0xFFA0A0A0);

        Draw.widget(guiGraphics, this.name, mouseX, mouseY, partialTicks);
        Draw.widget(guiGraphics, this.ipPort, mouseX, mouseY, partialTicks);
        if (currentType == Proxy.ProxyType.SOCKS4) {
            Draw.text(guiGraphics, this.font, Component.translatable("ui.coffeeproxy.auth.id").getString(),
                    positionX, positionY[5] - 10, 0xFFA0A0A0);
            Draw.widget(guiGraphics, this.username, mouseX, mouseY, partialTicks);
        } else {
            Draw.text(guiGraphics, this.font, Component.translatable("ui.coffeeproxy.auth.password").getString(),
                    positionX, positionY[6] - 10, 0xFFA0A0A0);
            Draw.text(guiGraphics, this.font, Component.translatable("ui.coffeeproxy.auth.username").getString(),
                    positionX, positionY[5] - 10, 0xFFA0A0A0);
            Draw.widget(guiGraphics, this.username, mouseX, mouseY, partialTicks);
            Draw.widget(guiGraphics, this.password, mouseX, mouseY, partialTicks);
        }

        Draw.centeredText(guiGraphics, this.font, !msg.isEmpty() ? msg : testPing.state, this.width / 2,
                positionY[7] + 5, 0xFFA0A0A0);
    }

    @Override
    public void tick() {
        testPing.pingPendingNetworks();
    }

    @Override
    public void init() {
        int buttonLength = 160;
        centerButtons(11, buttonLength, 32);

        String entry = editName == null ? "" : Config.proxies.getOrDefault(editName, "");
        Proxy saved = entry.isEmpty() ? new Proxy() : Proxy.parse(entry);

        String savedName = this.name != null ? this.name.getValue() : (editName == null ? "" : editName);
        String savedIpPort = this.ipPort != null ? this.ipPort.getValue() : saved.ipPort;
        String savedUsername = this.username != null ? this.username.getValue() : saved.username;
        String savedPassword = this.password != null ? this.password.getValue() : saved.password;
        if (this.ipPort == null) {
            currentType = saved.type;
        }

        this.name = new EditBox(this.font, positionX, positionY[1], buttonLength, 20,
                Component.literal(""));
        this.name.setValue(savedName);
        this.name.setMaxLength(64);
        this.name.setFocused(true);
        this.addWidget(this.name);

        Button proxyType = Button.builder(Component.literal(currentType.name()), button -> {
            Proxy.ProxyType[] values = Proxy.ProxyType.values();
            currentType = values[(currentType.ordinal() + 1) % values.length];
            button.setMessage(Component.literal(currentType.name()));
        }).bounds(positionX, positionY[2], buttonLength, 20).build();
        this.addRenderableWidget(proxyType);

        this.ipPort = new EditBox(this.font, positionX, positionY[3], buttonLength, 20,
                Component.literal(""));
        this.ipPort.setValue(savedIpPort);
        this.ipPort.setMaxLength(1024);
        this.addWidget(this.ipPort);

        this.username = new EditBox(this.font, positionX, positionY[5], buttonLength, 20,
                Component.literal(""));
        this.username.setMaxLength(255);
        this.username.setValue(savedUsername);
        this.addWidget(this.username);

        this.password = new EditBox(this.font, positionX, positionY[6], buttonLength, 20,
                Component.literal(""));
        this.password.setMaxLength(255);
        this.password.setValue(savedPassword);
        this.addWidget(this.password);

        int posXButtons = (this.width / 2) - (((buttonLength / 2) * 3) / 2);

        Button apply = Button.builder(Component.translatable("ui.coffeeproxy.options.save"), button -> {
            String entryName = name.getValue().trim();
            if (entryName.isEmpty()) {
                msg = ChatFormatting.RED + Component.translatable("ui.coffeeproxy.err.specName").getString();
                this.name.setFocused(true);
                return;
            }
            if (!checkProxy()) {
                return;
            }
            Proxy edited = new Proxy(currentType, ipPort.getValue(), username.getValue(), password.getValue());
            if (editName != null && !editName.equals(entryName)) {
                Config.proxies.remove(editName);
                if (Config.activeName.equals(editName)) {
                    Config.activeName = entryName;
                }
            }
            Config.proxies.put(entryName, edited.format());
            if (entryName.equals(Config.activeName)) {
                Config.setActive(entryName);
            }
            Config.saveConfig();
            Coffeeproxy.openScreen(parentScreen);
        }).bounds(posXButtons, positionY[9], buttonLength / 2 - 3, 20).build();
        this.addRenderableWidget(apply);

        Button test = Button.builder(Component.translatable("ui.coffeeproxy.options.test"), (button) -> {
            if (ipPort.getValue().isEmpty() || ipPort.getValue().equalsIgnoreCase("none")) {
                msg = ChatFormatting.RED + Component.translatable("ui.coffeeproxy.err.specProxy").getString();
                return;
            }
            if (checkProxy()) {
                testPing = new TestPing();
                testPing.run("anticheat-test.com", 25565,
                        new Proxy(currentType, ipPort.getValue(), username.getValue(), password.getValue()));
            }
        }).bounds(posXButtons + buttonLength / 2 + 3, positionY[9], buttonLength / 2 - 3, 20).build();
        this.addRenderableWidget(test);

        Button cancel = Button.builder(Component.translatable("ui.coffeeproxy.options.cancel"), (button) -> {
            Coffeeproxy.openScreen(parentScreen);
        }).bounds(posXButtons + (buttonLength / 2 + 3) * 2, positionY[9], buttonLength / 2 - 3, 20).build();
        this.addRenderableWidget(cancel);
    }

    @Override
    public void onClose() {
        msg = "";
    }
}

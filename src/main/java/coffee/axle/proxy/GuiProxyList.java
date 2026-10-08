// SPDX-FileCopyrightText: 2026 Axle Duggan (axlecoffee) <contact@axle.coffee>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package coffee.axle.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ObjectSelectionList;
//? if >=1.21.10 {
import net.minecraft.client.input.MouseButtonEvent;
//?} else {
/*import net.minecraft.Util;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderPipelines;
import net.fabricmc.loader.api.FabricLoader;

public class GuiProxyList extends Screen {
    private final Screen parentScreen;

    private ProxyList proxyList;
    private Button connectButton;
    private Button deleteButton;
    private Button editButton;
    private Checkbox showMultiplayerCheck;

    private static final String TEXT_TITLE = Component.translatable("ui.coffeeproxy.list.title").getString();
    private static final String TEXT_FAILED = Component.translatable("ui.coffeeproxy.list.failed").getString();
    private static final Component TEXT_CONNECT = Component.translatable("ui.coffeeproxy.list.connect");
    private static final Component TEXT_DISCONNECT = Component.translatable("ui.coffeeproxy.list.disconnect");

    public GuiProxyList(Screen parentScreen) {
        super(Component.literal(TEXT_TITLE));
        this.parentScreen = parentScreen;
    }

    @Override
    public void init() {
        this.proxyList = new ProxyList(this.minecraft);
        this.addRenderableWidget(this.proxyList);
        for (var entry : Config.proxies.entrySet()) {
            this.proxyList.addProxy(entry.getKey(), entry.getValue());
        }

        this.addRenderableWidget(Button
                .builder(Component.translatable("ui.coffeeproxy.list.importExport"),
                        button -> Coffeeproxy.openScreen(new GuiImportExport(this)))
                .bounds(8, 8, 100, 20).build());

        int x1 = this.width / 2 - 154;
        int x2 = x1 + 104;
        int x3 = x1 + 208;

        this.connectButton = this.addRenderableWidget(Button
                .builder(TEXT_CONNECT, button -> connectSelected())
                .bounds(x1, this.height - 52, 100, 20).build());
        this.addRenderableWidget(Button
                .builder(Component.translatable("ui.coffeeproxy.list.add"), button -> openEditor(null))
                .bounds(x2, this.height - 52, 100, 20).build());
        this.editButton = this.addRenderableWidget(Button
                .builder(Component.translatable("ui.coffeeproxy.list.edit"), button -> {
                    ProxyEntry selected = this.proxyList.getSelected();
                    if (selected != null) {
                        openEditor(selected.name);
                    }
                })
                .bounds(x3, this.height - 52, 100, 20).build());

        this.deleteButton = this.addRenderableWidget(Button
                .builder(Component.translatable("ui.coffeeproxy.list.delete"), button -> deleteSelected())
                .bounds(x1, this.height - 28, 100, 20).build());
        this.addRenderableWidget(Button
                .builder(Component.translatable("ui.coffeeproxy.list.refresh"), button -> this.proxyList.refreshPings())
                .bounds(x2, this.height - 28, 100, 20).build());
        this.addRenderableWidget(Button
                .builder(Component.translatable("ui.coffeeproxy.list.done"), button -> onClose())
                .bounds(x3, this.height - 28, 100, 20).build());

        Checkbox.Builder showMultiplayerBuilder = Checkbox
                .builder(Component.translatable("ui.coffeeproxy.options.showMultiplayerButton"), this.font);
        showMultiplayerBuilder.pos(0, this.height - 78);
        showMultiplayerBuilder.selected(Config.showMultiplayerButton);
        this.showMultiplayerCheck = showMultiplayerBuilder.build();
        this.showMultiplayerCheck.setX(this.width / 2 - this.showMultiplayerCheck.getWidth() / 2);
        this.showMultiplayerCheck.active = FabricLoader.getInstance().isModLoaded("modmenu");
        this.addRenderableWidget(this.showMultiplayerCheck);
    }

    private void connectSelected() {
        ProxyEntry selected = this.proxyList.getSelected();
        if (selected != null) {
            connectTo(selected.name);
        }
    }

    void connectTo(String name) {
        Config.setActive(Config.activeName.equals(name) ? "" : name);
        Config.saveConfig();
        ProxyEntry selected = this.proxyList.getSelected();
        if (selected != null) {
            selected.startPing();
        }
    }

    void openEditor(String editName) {
        Coffeeproxy.openScreen(new GuiProxy(this, editName));
    }

    private void deleteSelected() {
        ProxyEntry selected = this.proxyList.getSelected();
        if (selected == null) {
            return;
        }
        Config.proxies.remove(selected.name);
        if (Config.activeName.equals(selected.name)) {
            Config.setActive("");
        }
        Config.saveConfig();
        this.proxyList.removeSelected(selected);
        this.proxyList.setSelected(null);
    }

    @Override
    public void tick() {
        this.proxyList.tickEntries();
    }

    @Override
    //? if <26 {
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    //?} else {
    /*public void extractRenderState(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    *///?}
        ProxyEntry selected = this.proxyList.getSelected();
        this.connectButton.active = selected != null;
        this.connectButton.setMessage(selected != null && selected.name.equals(Config.activeName)
                ? TEXT_DISCONNECT : TEXT_CONNECT);
        this.deleteButton.active = selected != null;
        this.editButton.active = selected != null;

        Draw.centeredText(guiGraphics, this.font, TEXT_TITLE, this.width / 2, 14, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        Config.showMultiplayerButton = this.showMultiplayerCheck.selected();
        Config.saveConfig();
        Coffeeproxy.openScreen(this.parentScreen);
    }

    private class ProxyList extends ObjectSelectionList<ProxyEntry> {
        ProxyList(Minecraft minecraft) {
            super(minecraft, GuiProxyList.this.width, GuiProxyList.this.height - 124, 36, 36);
        }

        @Override
        public int getRowWidth() {
            return 305;
        }

        void addProxy(String name, String entry) {
            this.addEntry(new ProxyEntry(name, entry));
        }

        void removeSelected(ProxyEntry row) {
            this.removeEntry(row);
        }

        void refreshPings() {
            for (ProxyEntry row : this.children()) {
                row.startPing();
            }
        }

        void tickEntries() {
            for (ProxyEntry row : this.children()) {
                row.tickPing();
            }
        }
    }

    private class ProxyEntry extends ObjectSelectionList.Entry<ProxyEntry> {
        final String name;
        final Proxy proxy;
        private TestPing testPing = new TestPing();
        private long lastClickTime;
        private int rowLeft;
        private int rowTop;
        private int rowWidth;

        ProxyEntry(String name, String endpoint) {
            this.name = name;
            this.proxy = Proxy.parse(endpoint);
            startPing();
        }

        void startPing() {
            this.testPing = new TestPing();
            this.testPing.run("anticheat-test.com", 25565, this.proxy);
        }

        void tickPing() {
            this.testPing.pingPendingNetworks();
        }

        @Override
        public Component getNarration() {
            return Component.literal(this.name);
        }

        private int rowColor() {
            if (this.testPing.failed) {
                return 0xFFFF5555;
            }
            if (this.name.equals(Config.activeName)) {
                return 0xFF55FF55;
            }
            return 0xFFA0A0A0;
        }

        private String pingText() {
            if (this.testPing.failed) {
                return TEXT_FAILED;
            }
            if (this.testPing.latency >= 0) {
                return Component.translatable("multiplayer.status.ping", this.testPing.latency).getString();
            }
            return "";
        }

        private String pingSprite() {
            long l = this.testPing.latency;
            if (l < 0) {
                return "unreachable";
            }
            if (l <= 150) return "ping_5";
            if (l <= 300) return "ping_4";
            if (l <= 600) return "ping_3";
            if (l <= 1000) return "ping_2";
            return "ping_1";
        }

        private boolean clickRow(boolean doubleClick) {
            proxyList.setSelected(this);
            if (doubleClick) {
                connectTo(this.name);
            }
            return true;
        }

        //? if <1.21.10 {
        /*@Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX,
                int mouseY, boolean hovering, float partialTicks) {
            this.rowLeft = left;
            this.rowTop = top;
            this.rowWidth = width;
            drawRow(guiGraphics);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean doubleClick = Util.getMillis() - this.lastClickTime < 250;
            this.lastClickTime = Util.getMillis();
            return clickRow(doubleClick);
        }
        *///?}
        //? if >=1.21.10 <26 {
        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTicks) {
            this.rowLeft = this.getX();
            this.rowTop = this.getY();
            this.rowWidth = this.getWidth();
            drawRow(guiGraphics);
        }
        //?}
        //? if >=26 {
        /*@Override
        public void extractContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering,
                float partialTicks) {
            this.rowLeft = this.getX();
            this.rowTop = this.getY();
            this.rowWidth = this.getWidth();
            drawRow(guiGraphics);
        }
        *///?}
        //? if >=1.21.10 {
        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            return clickRow(doubleClick);
        }
        //?}

        private void drawRow(GuiGraphics guiGraphics) {
            int color = rowColor();
            String ping = pingText();
            Draw.text(guiGraphics, GuiProxyList.this.font, this.name, this.rowLeft + 2, this.rowTop + 2, color);
            Draw.text(guiGraphics, GuiProxyList.this.font, this.proxy.ipPort, this.rowLeft + 2, this.rowTop + 13, 0xFF808080);
            int iconX = this.rowLeft + this.rowWidth - 14;
            ResourceLocation sprite = ResourceLocation.withDefaultNamespace("server_list/" + pingSprite());
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite,
                    iconX, this.rowTop + 2, 10, 8);
            Draw.text(guiGraphics, GuiProxyList.this.font, ping, iconX - 2 - GuiProxyList.this.font.width(ping), this.rowTop + 2, color);
        }
    }
}

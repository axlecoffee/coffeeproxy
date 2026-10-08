// SPDX-FileCopyrightText: 2026 Axle Duggan (axlecoffee) <contact@axle.coffee>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package coffee.axle.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;

public class GuiImportExport extends Screen {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    private static final String TEXT_TITLE = Component.translatable("ui.coffeeproxy.io.title").getString();
    private static final String TEXT_UNDEFINED = ChatFormatting.RED
            + Component.translatable("ui.coffeeproxy.io.undefined").getString();
    private static final String TEXT_FETCHING = Component.translatable("ui.coffeeproxy.io.fetching").getString();
    private static final String TEXT_FETCH_ERROR = ChatFormatting.RED
            + Component.translatable("ui.coffeeproxy.io.fetchError").getString();
    private static final String TEXT_COPIED = ChatFormatting.GREEN
            + Component.translatable("ui.coffeeproxy.io.copied").getString();

    private final Screen parentScreen;

    private EditBox urlBox;
    private Button urlButton;
    private Button saveOverwrite;
    private Button saveAppend;

    private String msg = "";
    private LinkedHashMap<String, String> pending;
    // written by the fetch thread, consumed on the tick (main) thread
    private volatile String fetched;
    private volatile boolean fetchFailed;
    private boolean fetching;

    public GuiImportExport(Screen parentScreen) {
        super(Component.literal(TEXT_TITLE));
        this.parentScreen = parentScreen;
    }

    @Override
    public void init() {
        int w = 160;
        int x = (this.width - w) / 2;
        int y0 = (this.height - 180) / 2;
        String savedUrl = this.urlBox == null ? "" : this.urlBox.getValue();

        this.urlBox = new EditBox(this.font, x, y0 + 24, w, 20, Component.literal(""));
        this.urlBox.setValue(savedUrl);
        this.urlBox.setMaxLength(1024);
        this.addWidget(this.urlBox);

        this.addRenderableWidget(Button.builder(Component.translatable("ui.coffeeproxy.io.clipboard"),
                button -> applyPayload(this.minecraft.keyboardHandler.getClipboard()))
                .bounds(x, y0, w, 20).build());

        this.urlButton = this.addRenderableWidget(Button.builder(Component.translatable("ui.coffeeproxy.io.url"),
                button -> fetchUrl())
                .bounds(x, y0 + 48, w, 20).build());
        this.urlButton.active = !this.fetching;

        this.saveOverwrite = this.addRenderableWidget(Button
                .builder(Component.translatable("ui.coffeeproxy.io.overwrite"), button -> save(true))
                .bounds(x, y0 + 88, w, 20).build());
        this.saveAppend = this.addRenderableWidget(Button
                .builder(Component.translatable("ui.coffeeproxy.io.append"), button -> save(false))
                .bounds(x, y0 + 112, w, 20).build());
        refreshSaves();

        this.addRenderableWidget(Button.builder(Component.translatable("ui.coffeeproxy.io.export"),
                button -> exportClipboard())
                .bounds(x, y0 + 136, w, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("ui.coffeeproxy.list.done"),
                button -> onClose())
                .bounds(x, y0 + 160, w, 20).build());
    }

    // clipboard is either raw json ({ at pos1) or base64 of it, anything else is Undefined!
    private static LinkedHashMap<String, String> parsePayload(String text) {
        try {
            String json = text == null ? "" : text.trim();
            if (!json.startsWith("{")) {
                json = new String(Base64.getDecoder().decode(json), StandardCharsets.UTF_8);
            }
            JsonElement proxies = JsonParser.parseString(json).getAsJsonObject().get("proxies");
            if (proxies == null || !proxies.isJsonObject()) {
                return null;
            }
            LinkedHashMap<String, String> out = new LinkedHashMap<>();
            for (var entry : proxies.getAsJsonObject().entrySet()) {
                out.put(entry.getKey(), entry.getValue().getAsString());
            }
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    private void applyPayload(String text) {
        LinkedHashMap<String, String> parsed = parsePayload(text);
        if (parsed == null) {
            this.pending = null;
            this.msg = TEXT_UNDEFINED;
        } else {
            this.pending = parsed;
            this.msg = ChatFormatting.GREEN
                    + Component.translatable("ui.coffeeproxy.io.loaded", parsed.size()).getString();
        }
        refreshSaves();
    }

    private void fetchUrl() {
        String url = this.urlBox.getValue().trim();
        this.fetching = true;
        this.urlButton.active = false;
        this.msg = TEXT_FETCHING;
        new Thread(() -> {
            String body = "";
            boolean failed = false;
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET().build();
                body = HTTP.send(request, BodyHandlers.ofString()).body();
            } catch (Exception e) {
                // bad url / timeout / refused
                failed = true;
            }
            this.fetchFailed = failed;
            this.fetched = body;
        }, "CoffeeProxy importer").start();
    }

    private void refreshSaves() {
        this.saveOverwrite.active = this.pending != null;
        this.saveAppend.active = this.pending != null;
    }

    private void save(boolean overwrite) {
        if (this.pending == null) {
            return;
        }
        if (overwrite) {
            Config.proxies.clear();
            Config.proxies.putAll(this.pending);
            Config.setActive(Config.proxies.containsKey(Config.activeName) ? Config.activeName : "");
        } else {
            // colliding names get a number so re-importing the same list duplicates instead of replaces
            for (var entry : this.pending.entrySet()) {
                String name = entry.getKey();
                for (int n = 2; Config.proxies.containsKey(name); n++) {
                    name = entry.getKey() + " " + n;
                }
                Config.proxies.put(name, entry.getValue());
            }
        }
        Config.saveConfig();
        Coffeeproxy.openScreen(this.parentScreen);
    }

    private void exportClipboard() {
        // the exported list doubles as the selected payload, so export -> save round trips without another click
        this.pending = new LinkedHashMap<>(Config.proxies);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject root = new JsonObject();
        root.add("proxies", gson.toJsonTree(this.pending));
        String b64 = Base64.getEncoder().encodeToString(gson.toJson(root).getBytes(StandardCharsets.UTF_8));
        this.minecraft.keyboardHandler.setClipboard(b64);
        this.msg = TEXT_COPIED;
        refreshSaves();
    }

    @Override
    public void tick() {
        if (this.fetched == null) {
            return;
        }
        String text = this.fetched;
        this.fetched = null;
        this.fetching = false;
        this.urlButton.active = true;
        if (this.fetchFailed) {
            this.pending = null;
            this.msg = TEXT_FETCH_ERROR;
            refreshSaves();
        } else {
            applyPayload(text);
        }
    }

    @Override
    //? if <26 {
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    //?} else {
    /*public void extractRenderState(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    *///?}
        int y0 = (this.height - 180) / 2;
        Draw.centeredText(guiGraphics, this.font, this.msg, this.width / 2, y0 + 74, 0xFFA0A0A0);
        Draw.widget(guiGraphics, this.urlBox, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        Coffeeproxy.openScreen(this.parentScreen);
    }
}

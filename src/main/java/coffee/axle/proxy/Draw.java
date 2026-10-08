// SPDX-FileCopyrightText: 2026 Axle Duggan (axlecoffee) <contact@axle.coffee>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package coffee.axle.proxy;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;

// abstracted or smth because of stonecutter if else coming to HARM ME 
final class Draw {
    static void text(GuiGraphics g, Font font, String s, int x, int y, int color) {
        //? if <26 {
        g.drawString(font, s, x, y, color);
        //?} else {
        /*g.text(font, s, x, y, color);
        *///?}
    }

    static void centeredText(GuiGraphics g, Font font, String s, int x, int y, int color) {
        //? if <26 {
        g.drawCenteredString(font, s, x, y, color);
        //?} else {
        /*g.centeredText(font, s, x, y, color);
        *///?}
    }

    static void widget(GuiGraphics g, EditBox box, int mouseX, int mouseY, float partialTicks) {
        //? if <26 {
        box.render(g, mouseX, mouseY, partialTicks);
        //?} else {
        /*box.extractWidgetRenderState(g, mouseX, mouseY, partialTicks);
        *///?}
    }
}

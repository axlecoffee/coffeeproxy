// SPDX-FileCopyrightText: 2026 Axle Duggan (axlecoffee) <contact@axle.coffee>
//
// SPDX-License-Identifier: CC0-1.0

plugins {
    id("dev.kikugie.stonecutter")
    id("coffee.axle.blahaj")
}

stonecutter active "1.21.10-fabric" /* [SC] DO NOT EDIT */

stonecutter parameters {
    replacements {
        string(current.parsed >= "1.21.8") {
            replace("RenderType::guiTextured", "RenderPipelines.GUI_TEXTURED")
            replace("net.minecraft.client.renderer.RenderType", "net.minecraft.client.renderer.RenderPipelines")
        }
        // keys are anchored to code context: bare "Identifier" would eat "SPDX-License-Identifier"
        // and bare "System.currentTimeMillis()" collides with the version-independent calls in Proxy
        string(current.parsed >= "1.21.11") {
            replace("net.minecraft.resources.ResourceLocation", "net.minecraft.resources.Identifier")
            replace("ResourceLocation sprite = ResourceLocation.withDefaultNamespace", "Identifier sprite = Identifier.withDefaultNamespace")
            replace("Util.getMillis() - pingSentAt", "System.currentTimeMillis() - pingSentAt")
            replace("pingSentAt = Util.getMillis()", "pingSentAt = System.currentTimeMillis()")
        }
        string(current.parsed >= "26") {
            replace("GuiGraphics", "GuiGraphicsExtractor")
        }
        string(current.parsed >= "26.2") {
            replace("Minecraft.getInstance().setScreen", "Minecraft.getInstance().gui.setScreen")
        }
    }
}

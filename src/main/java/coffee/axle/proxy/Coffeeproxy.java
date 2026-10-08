// SPDX-FileCopyrightText: 2026 Axle Duggan (axlecoffee) <contact@axle.coffee>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package coffee.axle.proxy;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class Coffeeproxy implements ModInitializer {
	public static boolean proxyEnabled = false;
	public static Proxy proxy = new Proxy();

	@Override
	public void onInitialize() {
		Config.loadConfig();
		Config.setActive(Config.activeName);
	}

	public static void openScreen(Screen screen) {
		Minecraft.getInstance().setScreen(screen);
	}
}
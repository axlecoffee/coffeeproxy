package coffee.axle.proxy;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.widget.ButtonWidget;

public class Coffeeproxy implements ModInitializer {
	public static boolean proxyEnabled = false;
	public static Proxy proxy = new Proxy();
	public static Proxy lastUsedProxy = new Proxy();
	public static ButtonWidget proxyMenuButton;
	public static boolean suppressProxyMixin = false;

	public static String getLastUsedProxyIp() {
		return lastUsedProxy.ipPort.isEmpty() ? "none" : lastUsedProxy.getIp();
	}

	@Override
	public void onInitialize() {
		Config.loadConfig();
	}
}
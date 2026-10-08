// SPDX-FileCopyrightText: 2026 Axle Duggan (axlecoffee) <contact@axle.coffee>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package coffee.axle.proxy;

import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

public class Proxy {
    private static final ConcurrentHashMap<String, CachedResolve> DNS_CACHE = new ConcurrentHashMap<>();
    private static final long DNS_CACHE_TTL_MS = 5 * 60 * 1000L;

    public String ipPort = "";
    public ProxyType type = ProxyType.SOCKS5;
    public String username = "";
    public String password = "";

    private transient InetSocketAddress cachedAddress;
    private transient long cachedAddressTime;

    public Proxy() {
    }

    public Proxy(ProxyType type, String ipPort, String username, String password) {
        this.type = type;
        this.ipPort = ipPort;
        this.username = username;
        this.password = password;
    }

    public static Proxy parse(String entry) {
        Proxy proxy = new Proxy();
        String rest = entry.trim();
        int schemeEnd = rest.indexOf("://");
        if (schemeEnd >= 0) {
            String scheme = rest.substring(0, schemeEnd);
            if (scheme.equalsIgnoreCase("socks4")) {
                proxy.type = ProxyType.SOCKS4;
            } else if (scheme.equalsIgnoreCase("http")) {
                proxy.type = ProxyType.HTTP;
            }
            rest = rest.substring(schemeEnd + 3).trim();
        }
        int at = rest.indexOf('@');
        if (at >= 0) {
            String auth = rest.substring(at + 1);
            rest = rest.substring(0, at);
            int colon = auth.indexOf(':');
            if (colon >= 0) {
                proxy.username = auth.substring(0, colon);
                proxy.password = auth.substring(colon + 1);
            } else {
                proxy.username = auth;
            }
        }
        proxy.ipPort = rest;
        return proxy;
    }

    public String format() {
        String scheme = "";
        if (type == ProxyType.SOCKS4) {
            scheme = "socks4://";
        } else if (type == ProxyType.HTTP) {
            scheme = "http://";
        }
        String auth = "";
        if (!username.isEmpty()) {
            auth = "@" + username;
            if (!password.isEmpty()) {
                auth += ":" + password;
            }
        }
        return scheme + ipPort + auth;
    }

    public int getPort() {
        return Integer.parseInt(ipPort.split(":")[1]);
    }

    public String getIp() {
        return ipPort.split(":")[0];
    }

    public InetSocketAddress resolveProxyAddress() {
        long now = System.currentTimeMillis();
        if (cachedAddress != null && now - cachedAddressTime < DNS_CACHE_TTL_MS) {
            return cachedAddress;
        }
        cachedAddress = new InetSocketAddress(getIp(), getPort());
        cachedAddressTime = now;
        return cachedAddress;
    }

    private String meowEmpty(String value, String fallback) { return value.isEmpty() ? fallback : value; }

    public ProxyHandler getHandler() {
        return switch (type) {
            case SOCKS5 -> new Socks5ProxyHandler(resolveProxyAddress(), meowEmpty(username, null), meowEmpty(password, null));
            case SOCKS4 -> new Socks4ProxyHandler(resolveProxyAddress(), meowEmpty(username, null));
            case HTTP -> new HttpProxyHandler(resolveProxyAddress(), meowEmpty(username, null), meowEmpty(password, ""));
        };
    }

    public static InetAddress resolveAddress(String host) throws UnknownHostException {
        CachedResolve cached = DNS_CACHE.get(host);
        long now = System.currentTimeMillis();
        if (cached != null && now - cached.timestamp < DNS_CACHE_TTL_MS) {
            return cached.address;
        }
        InetAddress resolved = InetAddress.getByName(host);
        DNS_CACHE.put(host, new CachedResolve(resolved, now));
        return resolved;
    }

    public enum ProxyType {
        SOCKS4,
        SOCKS5,
        HTTP
    }

    private record CachedResolve(InetAddress address, long timestamp) {
    }
}

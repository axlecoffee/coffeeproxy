package coffee.axle.proxy;

import com.google.gson.annotations.SerializedName;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

public class Proxy {
    private static final ConcurrentHashMap<String, CachedResolve> DNS_CACHE = new ConcurrentHashMap<>();
    private static final long DNS_CACHE_TTL_MS = 5 * 60 * 1000L;

    @SerializedName("IP:PORT")
    public String ipPort = "";
    public ProxyType type = ProxyType.SOCKS5;
    public String username = "";
    public String password = "";

    private transient InetSocketAddress cachedAddress;
    private transient long cachedAddressTime;

    public Proxy() {
    }

    public Proxy(boolean isSocks4, String ipPort, String username, String password) {
        this.type = isSocks4 ? ProxyType.SOCKS4 : ProxyType.SOCKS5;
        this.ipPort = ipPort;
        this.username = username;
        this.password = password;
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
        SOCKS5
    }

    private record CachedResolve(InetAddress address, long timestamp) {
    }
}

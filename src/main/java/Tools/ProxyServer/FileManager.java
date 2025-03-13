package Tools.ProxyServer;

import java.io.Serializable;
import java.util.TreeMap;

class FileManager implements Serializable {
    //代理服务器信息（key:服务器名称;value:服务器地址）
    public TreeMap<String, String> treemap;
    //当前选中的服务器名称
    public String currentSelectedProxyServerName;

    public FileManager() {
        treemap = new TreeMap<>();
        currentSelectedProxyServerName = "";
    }

}

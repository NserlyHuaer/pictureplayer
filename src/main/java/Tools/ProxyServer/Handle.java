package Tools.ProxyServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class Handle {
    public final File savePath;
    private FileManager fileManager;
    private static final Logger log = LoggerFactory.getLogger(Handle.class);

    //推荐路径：data\\ProxyServerMenu.pxs
    public Handle(String savePath) {
        this.savePath = new File(savePath);
        fileManager = new FileManager();
        if (!this.savePath.exists()) {
            try {
                this.savePath.createNewFile();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void refresh() {
        try {
            FileInputStream fileIn = new FileInputStream(savePath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            fileManager = (FileManager) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage());
        }

    }

    /**
     * 获取当前选中的代理服务器名称
     *
     * @return 代理服务器名称
     */
    public String getCurrentSelectionProxyServerName() {
        return fileManager.currentSelectedProxyServerName;
    }

    /**
     * 设置当前选中的代理服务器名称
     *
     * @param ProxyServerName 代理服务器名称
     */
    public void setCurrentSelectionProxyServerName(String ProxyServerName) {
        if (containsProxyServerName(ProxyServerName))
            fileManager.currentSelectedProxyServerName = ProxyServerName;
    }

    public void delete(String ProxyServerName) {
        fileManager.treemap.remove(ProxyServerName);
    }

    public void add(String ProxyServerName, String ProxyServerAddress) {
        if (fileManager.treemap.containsKey(ProxyServerName))
            fileManager.treemap.replace(ProxyServerName, ProxyServerAddress);
        else
            fileManager.treemap.put(ProxyServerName, ProxyServerAddress);
    }

    public boolean containsProxyServerAddress(String ProxyServerAddress) {
        return fileManager.treemap.containsValue(ProxyServerAddress);
    }

    public boolean containsProxyServerName(String ProxyServerName) {
        return fileManager.treemap.containsKey(ProxyServerName);
    }

    public TreeMap<String, String> getAllProxyServers() {
        return fileManager.treemap;
    }

    public Set<String> getAllProxyServerNames() {
        return fileManager.treemap.keySet();
    }

    public Collection<String> getAllProxyServerAddress() {
        return fileManager.treemap.values();
    }

    public String getProxyServerAddress(String ProxyServerName) {
        return fileManager.treemap.get(ProxyServerName);
    }

    public void save() {
        if (!containsProxyServerName(fileManager.currentSelectedProxyServerName))
            fileManager.currentSelectedProxyServerName = "";
        try {
            FileOutputStream fileOut = new FileOutputStream(savePath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(fileManager);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}

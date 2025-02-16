package Tools.ProxyServer;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

class FileManager implements Serializable {
    public TreeMap<String, String> treemap;

    public FileManager() {
        treemap = new TreeMap<>();
    }

}

package top.nserly.PicturePlayer.Loading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class Init<KEY, VALUE> {
    private static final AtomicBoolean isInit = new AtomicBoolean();
    private final File f = new File("data/configuration.ch");
    private final Properties properties = new Properties();
    private boolean EnableAutoUpdate;
    private static final String[] createDirectory = {"data", "lib", "cache", "cache/PictureCache", "cache/thum", "download"};
    private static final String[] createFile = {"data/PictureCacheManagement.obj"};
    private static final Logger logger = LoggerFactory.getLogger(Init.class);

    public static void init() {
        synchronized (isInit) {
            if (isInit.get()) return;
            File dire;
            try {
                for (String directory : createDirectory) {
                    dire = new File(directory);
                    if (!dire.exists()) {
                        dire.mkdir();
                    }
                }
                for (String file : createFile) {
                    dire = new File(file);
                    if (!dire.exists()) {
                        dire.createNewFile();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clearDirectory(new File("./download/"));
            clearDirectory(new File("replace.sh"));
            clearDirectory(new File("replace.bat"));
            clearDirectory(new File("runnable.bat"));
            isInit.set(true);
        }
    }

    public static void clearDirectory(File directory) {
        if (!directory.exists()) return;
        if (directory.isFile()) {
            directory.delete();
            return;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    clearDirectory(file);
                    file.delete();
                    continue;
                }
                file.delete();
            }
        }
    }

    public void Run() {
        if (!isInit.get()) init();
        try {
            if (!f.exists()) {
                Writer(setDefault());
            }
            properties.clear();
            properties.load(new BufferedReader(new FileReader(f)));
        } catch (IOException e) {
            logger.error("Failed to read the configuration file");
        }
    }

    public boolean containsKey(KEY key) {
        return properties.containsKey(key);
    }

    public void Loading() {
        if (!isInit.get()) init();
        try {
            properties.load(new BufferedReader(new FileReader(f)));
        } catch (IOException e) {
            logger.error("Failed to read the configuration file");
        }
    }

    public void SetUpdate(boolean EnableAutoUpdate) {
        this.EnableAutoUpdate = EnableAutoUpdate;
    }

    public Properties getProperties() {
        return (Properties) properties.clone();
    }

    public void ChangeValue(KEY key, VALUE value) {
        properties.remove(key, value);
        properties.put(key, value);
        if (EnableAutoUpdate) Store();
    }

    public void Remove(KEY key, VALUE value) {
        properties.remove(key, value);
        if (EnableAutoUpdate) Store();
    }

    public void Update() {
        Store();
    }

    @SafeVarargs
    public final void Remove(KEY... key) {
        for (KEY i : key) {
            properties.remove(key);
        }
        if (EnableAutoUpdate) Store();
    }

    public void Writer(Map<KEY, VALUE> map) {
        if (!isInit.get()) init();
        properties.putAll(map);
        Store();
    }

    public void Writer(KEY key, VALUE value) {
        if (!isInit.get()) init();
        properties.put(key, value);
        Store();
    }

    @DefaultArgs
    public Map setDefault() {
        HashMap<String, String> hashMap = new HashMap<>();
        var method = DefaultArgs.class.getDeclaredMethods();
        for (Method i : method) {
            hashMap.put(i.getName(), String.valueOf(i.getDefaultValue()));
        }
        return hashMap;
    }

    private void Store() {
        if (!isInit.get()) init();
        try {
            properties.store(new BufferedWriter(new FileWriter(f)), "");
        } catch (IOException e) {
            logger.error("Failed to save the configuration file");
        }
    }
}

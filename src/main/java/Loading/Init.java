package Loading;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Init<KEY, VALUE> {
    private final File f = new File("data/Properties.prpt");
    private final Properties properties = new Properties();
    private boolean EnableAutoUpdate;
    private final String[] createDirectory = {"data", "cache", "download"};

    public Init() {
        File dire;
        for (String directory : createDirectory) {
            dire = new File(directory);
            if (!dire.exists()) {
                dire.mkdir();
            }
        }
        clearDirectory(new File("./cache/"));
        clearDirectory(new File("./download/"));
        clearDirectory(new File("replace.sh"));
        clearDirectory(new File("replace.bat"));
        clearDirectory(new File("runnable.bat"));


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
        try {
            if (!f.exists()) {
                Writer(setDefault());
            }
            properties.clear();
            properties.load(new BufferedReader(new FileReader(f)));
        } catch (IOException e) {
            System.out.println("Error:Failed to read the configuration file");
        }
    }

    public boolean containsKey(KEY key) {
        return properties.containsKey(key);
    }

    public void Loading() {
        try {
            properties.load(new BufferedReader(new FileReader(f)));
        } catch (IOException e) {
            System.out.println("Error:Failed to read the configuration file");
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
        properties.putAll(map);
        Store();
    }

    public void Writer(KEY key, VALUE value) {
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
        try {
            properties.store(new BufferedWriter(new FileWriter(f)), "");
        } catch (IOException e) {
            System.out.println("Error:Failed to save the configuration file");
        }
    }
}

package Settings;

import Loading.DefaultArgs;
import Runner.Main;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class Centre {
    public static final Map<String, String> DefaultData = new HashMap<>();
    public final HashMap<String, String> CurrentData = new HashMap<String, String>();

    static {
        try {
            Class<?> clazz = DefaultArgs.class;
            // 获取注解
            AnnotatedElement element = clazz;
            // 获取注解中所有成员的方法
            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {
                if (!method.getName().equals("annotationType") && !method.isDefault()) { // 忽略内部的annotationType方法
                    String defaultValue = method.getDefaultValue().toString();
                    DefaultData.put(method.getName(), defaultValue);
                }
            }
        } catch (RuntimeException e) {
            System.out.println("Error:" + e);
        }
    }

    public Centre() {
        reFresh();
    }


    public void setDefault() {
        CurrentData.clear();
        CurrentData.putAll(DefaultData);
    }

    public void reFresh() {
        setDefault();
        try {
            Main.init.Run();
            Properties properties = Main.init.getProperties();
            for (Object obj : properties.keySet()) {
                if (DefaultData.containsKey((String) obj)) {
                    CurrentData.replace((String) obj, (String) properties.get(obj));
                }
            }
        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
    }

    private static boolean getBoolean(String Description, Map map) {
        String cache = map.get(Description).toString().replace(" ", "").toLowerCase();
        if (cache.equals("true")) {
            return true;
        } else if (cache.equals("false")) {
            return false;
        }
        return (boolean) map.get(Description);
    }

    public static double getDouble(String Description, Map map) {
        return getDouble(Description, map, -65, 150);
    }

    private static double getDouble(String Description, Map map, double min, double max) {
        if (min > max) {
            double temp = max;
            max = min;
            min = temp;
        }
        String cache = map.get(Description).toString().replace(" ", "");
        double result = 0;
        try {
            result = Double.parseDouble(cache);
        } catch (NumberFormatException e) {

        }
        if (result > max) {
            result = max;
        }
        if (result < min) {
            result = min;
        }
        return result;
    }

}

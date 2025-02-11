package Settings;

import Loading.DefaultArgs;
import Runner.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class Centre {
    public static final Map<String, String> DefaultData = new HashMap<>();
    public final HashMap<String, String> CurrentData = new HashMap<String, String>();
    private static final Logger logger = LoggerFactory.getLogger(Centre.class);

    //初始化
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
            logger.error(e.getMessage());
        }
    }

    public Centre() {
        reFresh();
    }

    //重置默认设置
    public void setDefault() {
        CurrentData.clear();
        CurrentData.putAll(DefaultData);
    }

    //恢复之前设置（保存时）
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
            logger.error(e.getMessage());
        }
    }

    //获取某建的对应布尔值
    public static boolean getBoolean(String Description, Map map) {
        String cache = map.get(Description).toString().replace(" ", "").toLowerCase();
        if (cache.equals("true")) {
            return true;
        } else if (cache.equals("false")) {
            return false;
        }
        return (boolean) map.get(Description);
    }

    //获取某建的对应浮点值
    public static double getDouble(String Description, Map map) {
        return getDouble(Description, map, -65, 150);
    }


    //获取某建的对应布尔值
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
            logger.error(e.getMessage());
        }
        if (result > max) {
            result = max;
        }
        if (result < min) {
            result = min;
        }
        return result;
    }

    //保存设置
    public void save() {
        Main.init.Writer(CurrentData);
    }

}

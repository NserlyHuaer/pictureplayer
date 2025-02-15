package Loading;

import java.util.Locale;
import java.util.ResourceBundle;

public class Bundle {
    private static ResourceBundle bundle;

    // 初始化默认语言（例如从系统配置读取）
    static {
        setLocale(Locale.getDefault());
    }

    public static void setLocale(Locale locale) {
        bundle = ResourceBundle.getBundle("messages", locale);
    }

    public static String getMessage(String key) {
        return bundle.getString(key);
    }
}
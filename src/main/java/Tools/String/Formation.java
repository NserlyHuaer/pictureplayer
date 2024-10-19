package Tools.String;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 本类适用于格式化（格式{sth}{sth}）
 */
public class Formation {
    private List<String> Name = null;//名称
    private final StringPro string;//修改的变量

    /**
     * 初始化，改变有效值格式为{sth}{sth}
     *
     * @param CastString 被转换字符串
     */
    public Formation(String CastString) {
        // 初始化 string 和 Name
        string = new StringPro(CastString);
        Name = new ArrayList<>();

        // 使用正则表达式分割字符串，并提取以 "{" 开头并以 "}" 结尾的子字符串
        var pattern = Pattern.compile("\\{(.*?)}");
        var matcher = pattern.matcher(CastString);
        while (matcher.find()) {
            Name.add(matcher.group());
        }
    }

    /**
     * 将数组转化为本类支持读取的字符串格式
     *
     * @param array 要转化的数组
     * @return 输出本类支持读取的字符串格式
     */
    public static String ArrayToString(String... array) {
        StringPro st = new StringPro();
        for (String i : array) {
            st.append("{" + i + "}");
        }
        return st.toString();
    }

    /**
     * 获取结果
     *
     * @return 获取结果，返回StringPro对象
     */

    public StringPro getResult() {
        return (StringPro) string.clone();
    }

    /**
     * 改变字符串
     *
     * @param revalued 改变其中文本（非{sth}格式）
     * @param value    改变它的值
     * @return
     */
    public void Change(String revalued, String value) {//revalued文本,value改变值
        if (Name == null & Name.isEmpty()) return;
        String cache;
        cache = string.toString();
        if (Name.contains("{" + revalued + "}")) {
            cache = cache.toString().replaceAll("\\{" + revalued + "\\}", value);
        }
        string.clear();
        string.append(cache);

    }

    /**
     * 改变字符串
     *
     * @param revalued 改变其中文本（非{sth}格式）
     * @param value    改变它的值
     * @return
     */
    public void Change(String[] revalued, String value) {//revalued文本,value改变值
        if (Name == null & Name.isEmpty()) return;
        var iterable = StringPro.StringToList(revalued).iterator();
        var cache = "";
        while (iterable.hasNext()) {
            StringPro cache1 = null;
            cache = iterable.next();
            if (Name.contains("{" + cache + "}")) {
                cache1 = new StringPro(string.toString().replaceAll("\\{" + revalued + "\\}", value));
                string.clear();
                string.append(cache1);
            }
        }
    }

    public List<String> getArray() {
        List<String> list = new ArrayList<>();
        for (String string1 : Name) {
            list.add(string1.substring(1, string1.length() - 1));
        }
        return list;
    }

    /**
     * @param c        修改某类的Class对象
     * @param variable 被改变类的变量名（区分大小写）
     * @param value    改变后的值
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static void Revise(Class c, String variable, Object value) throws NoSuchFieldException, IllegalAccessException {//c为类，variable为被修改变量，value为修改的变量
        var f = c.getDeclaredField(variable);//获取属性列表
        f.setAccessible(true);//设置为可修改
        f.set(variable, value);//将变量对应名称里面的值设置为指定值
    }

}

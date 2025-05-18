package Tools.String;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 本类适用于格式化（格式{sth}{sth}）
 */
public class Formation {
    //第一个：sth集合  第二个：将其设置的值
    private HashMap<String, String> information;//名称
    private List<String> keys;
    public final String originalString;

    /**
     * 初始化，改变有效值格式为{sth}{sth}
     *
     * @param CastString 被转换字符串
     */
    public Formation(String CastString) {
        originalString = CastString;
        // 初始化
        information = new HashMap<>();
        keys = new ArrayList<>();
        // 使用正则表达式分割字符串，并提取以 "{" 开头并以 "}" 结尾的子字符串
        var pattern = Pattern.compile("\\{(.*?)}");
        var matcher = pattern.matcher(CastString);
        while (matcher.find()) {
            String string = matcher.group();
            string = string.substring(1, string.length() - 1);
            information.put(string, "");
            keys.add(string);
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
     * @return 获取结果，返回String对象
     */

    public String getProcessingString() {
        if (information == null || information.isEmpty()) return originalString;
        String result = originalString;
        String cache;
        for (String key : keys) {
            cache = information.get(key);
            if (cache != null && !cache.isEmpty()) {
                result = result.replaceAll("\\{" + key + "}", cache);
            }
        }
        return result;
    }

    /**
     * 重置
     */
    public void reset() {
        for (String string : keys) {
            information.remove(string);
            information.put(string, "");
        }
    }


    /**
     * 改变字符串
     *
     * @param revalued 改变其中文本（非{sth}格式）
     * @param value    改变它的值
     * @return
     */
    public void add(String revalued, String value) {//revalued文本,value改变值
        information.remove(revalued);
        information.put(revalued, value);
    }

    /**
     * 改变字符串
     *
     * @param hashMap 添加整个到information
     * @return
     */
    public void add(HashMap<String, String> hashMap) {//revalued文本,value改变值
        information.putAll(hashMap);
    }

    /**
     * 改变字符串
     *
     * @param hashMap 删除information并且添加整个hashMap到information
     * @return
     */
    public void RemoveAndAdd(HashMap<String, String> hashMap) {//revalued文本,value改变值
        information = (HashMap<String, String>) hashMap.clone();
    }

    public List<String> getArray() {
        return keys;
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

package Version;

import Tools.String.Formation;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.TreeMap;

@Getter
@Setter
public class VersionID {
    //最新外部版本（稳定）
    private String NormalVersion;

    //最新内部版本（稳定）
    private String NormalVersionID;

    //最新稳定版本描述文件网址
    private String NormalVersionDescribe;

    //最新稳定版依赖（key:依赖名 ; value:依赖下载地址）
    private TreeMap<String, String> NormalDependencies;

    //最新外部版本（测试）
    private String TestVersion;

    //最新内部版本（测试）
    private String TestVersionID;

    //最新测试版本描述文件网址
    private TreeMap<String, String> TestVersionDescribe;

    //最新测试版依赖（key:依赖名 ; value:依赖下载地址）
    private TreeMap<String, String> TestDependencies;

    //特殊字段（使用{xxx}标注的）（key:特殊字段（不包括"{}"的，如特殊字段{demo}，key一定要时demo ; value:对应值（里面不能含有特殊字段）））
    private HashMap<String, String> SpecialFields;

    //还原含有特殊字段的字符串原本的字符串
    public static String getString(String str, HashMap<String, String> SpecialFields) {
        Formation formation = new Formation(str);
        formation.RemoveAndAdd(SpecialFields);
        return formation.getProcessingString();
    }
}

package VersionID;

import Version.VersionID;

import java.util.HashMap;
import java.util.TreeMap;

public class Generator {
    public static void main(String[] args) {
        VersionID versionID = new VersionID();
        versionID.setNormalVersion("{version}");
        versionID.setNormalVersionID("{versionID}");
        versionID.setNormalVersionDescribe("{MDWebsite}Describe/{versionID}.txt");
        versionID.setNormalVersionMainFile("{MDWebsite}/{version}.jar");

//        versionID.setTestVersion("{version}");
//        versionID.setTestVersionID("{versionID}");
//        versionID.setTestVersionDescribe("{MDWebsite}Describe/{versionID}.txt");
//        versionID.setTestVersionMainFile("{MDWebsite}/{version}.jar");


        HashMap<String, String> SpecialFields = new HashMap<>();
        SpecialFields.put("version", "V1.0.0beta14");
        SpecialFields.put("versionID", "1305");
        SpecialFields.put("MDWebsite", "https://gitee.com/nserly-huaer/ImagePlayer/raw/master/artifacts/PicturePlayer_jar/");
        SpecialFields.put("LibWebsite", "https://gitee.com/nserly-huaer/ImagePlayer/raw/master/artifacts/PicturePlayer_jar/lib/");


        TreeMap<String, String> dependencies = getTreeMap();


        versionID.setNormalDependencies(dependencies);
//        versionID.setTestDependencies(dependencies);
        versionID.setSpecialFields(SpecialFields);
        System.out.println(VersionID.gson.toJson(versionID));
    }

    private static TreeMap<String, String> getTreeMap() {
        TreeMap<String, String> dependencies = new TreeMap<>();
        dependencies.put("common-image", "{LibWebsite}common-image-3.12.01.jar");
        dependencies.put("common-io", "{LibWebsite}common-io-3.12.0.jar");
        dependencies.put("common-lang","{LibWebsite}common-lang-3.12.0.jar");
        dependencies.put("error_prone_annotations","{LibWebsite}error_prone_annotations-2.38.0.jar");
        dependencies.put("forms_rt","{LibWebsite}forms_rt-142.1.jar");
        dependencies.put("gson","{LibWebsite}gson-2.13.1.jar");
        dependencies.put("imageio-core","{LibWebsite}imageio-core-3.12.0.jar");
        dependencies.put("imageio-jpeg","{LibWebsite}imageio-jpeg-3.12.0.jar");
        dependencies.put("imageio-metadata","{LibWebsite}imageio-metadata-3.12.0.jar");
        dependencies.put("imageio-tiff","{LibWebsite}imageio-tiff-3.12.0.jar");
        dependencies.put("imageio-webp","{LibWebsite}imageio-webp-3.12.0.jar");
        dependencies.put("jna","{LibWebsite}jna-5.17.0.jar");
        dependencies.put("jna-platform","{LibWebsite}jna-platform-5.17.0.jar");
        dependencies.put("log4j-api","{LibWebsite}log4j-api-2.24.3.jar");
        dependencies.put("log4j-core","{LibWebsite}log4j-core-2.24.3.jar");
        dependencies.put("log4j-slf4j2-impl","{LibWebsite}log4j-slf4j2-impl-2.24.3.jar");
        dependencies.put("oshi-core","{LibWebsite}oshi-core-6.8.1.jar");
        dependencies.put("slf4j-api","{LibWebsite}slf4j-api-2.0.17.jar");
        dependencies.put("thumbnailator","{LibWebsite}thumbnailator-0.4.20");



        return dependencies;
    }
}

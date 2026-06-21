/*
 * Copyright 2026 PicturePlayer;Nserly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package Generate.VersionID;

import org.jspecify.annotations.NonNull;
import top.nserly.PicturePlayer.Version.PicturePlayerVersion;
import top.nserly.PicturePlayer.Version.VersionID;
import top.nserly.SoftwareCollections_API.FileHandle.FileContents;
import top.nserly.SoftwareCollections_API.FileHandle.JarFileRenamer;
import top.nserly.SoftwareCollections_API.FileHandle.JarVersionCleaner;
import top.nserly.SoftwareCollections_API.Hashcode.FileHashUtil;
import top.nserly.SoftwareCollections_API.Thread.ThreadControl;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Generator {
    public static final String DownloadWebSite = "https://PicturePlayer.nserly.top/artifacts/PicturePlayer_jar/";
    public static final String DownloadLibWebSite = DownloadWebSite + "lib/";

    void main() throws IOException, NoSuchAlgorithmException, InterruptedException {
        VersionID versionID = new VersionID();
        versionID.setStartMainFile("{MDWebsite}PicturePlayerRunner.exe");


        versionID.setNormalVersion("{version}");
        versionID.setNormalVersionID("{versionID}");
        versionID.setNormalVersionDescribe("{MDWebsite}Describe/{versionID}.txt");
        versionID.setNormalVersionMainFile("{MDWebsite}/{version}.jar");

//        versionID.setTestVersion("{version}");
//        versionID.setTestVersionID("{versionID}");
//        versionID.setTestVersionDescribe("{MDWebsite}Describe/{versionID}.txt");
//        versionID.setTestVersionMainFile("{MDWebsite}/{version}.jar");


        HashMap<String, String> SpecialFields = new HashMap<>();
        SpecialFields.put("version", PicturePlayerVersion.getShorterVersion());
        SpecialFields.put("versionID", PicturePlayerVersion.getVersionID());
        SpecialFields.put("MDWebsite", DownloadWebSite);
        SpecialFields.put("LibWebsite", DownloadLibWebSite);


        TreeMap<String, String> dependencies = getDependenciesVersionTreeMap();


        versionID.setNormalDependencies(dependencies);
//        versionID.setTestDependencies(dependencies);
        versionID.setSpecialFields(SpecialFields);

        changeMainFileName(SpecialFields);
        setHashCode(versionID);


        String versionIDJson = VersionID.gson.toJson(versionID);
        System.out.println(versionIDJson);
        FileContents.write("artifacts/PicturePlayer_jar/VersionID.sum", versionIDJson);
    }


    private static TreeMap<String, String> getDependenciesVersionTreeMap() throws IOException {
        TreeMap<String, String> dependencies = new TreeMap<>();
        HashSet<File> hashSet = verifierAndGetDependencyVersionSet(dependencies);

        System.out.println("Dependency counts:" + dependencies.size());
        if (hashSet.size() != dependencies.size()) {
            throw new RuntimeException("Dependencies are incomplete!");
        }
        return dependencies;
    }

    private static HashSet<File> verifierAndGetDependencyVersionSet(TreeMap<String, String> dependencies) throws IOException {
        File libFile = new File("artifacts/PicturePlayer_jar/lib/");
        JarVersionCleaner.cleanOldVersions(libFile.getPath(), false);

        HashSet<File> hashSet = JarFileRenamer.renameJarFile(libFile.getPath());
        for (File file : hashSet) {
            if (file.getName().endsWith(".jar") && file.isFile()) {
                String dependencyName = file.getName();
                if (dependencies.containsKey(dependencyName.substring(0, dependencyName.lastIndexOf("-")))) {
                    throw new RuntimeException("Dependency Name Conflict: " + dependencyName);
                }
                dependencies.put(dependencyName.substring(0, dependencyName.lastIndexOf("-")), "{LibWebsite}" + dependencyName);
            }
        }
        return hashSet;
    }

    private static void changeMainFileName(HashMap<String, String> SpecialFields) {
        File file = new File("artifacts/PicturePlayer_jar/PicturePlayer.jar");
        if (file.exists()) {
            File renameToFile = new File("artifacts/PicturePlayer_jar/" + SpecialFields.get("version") + ".jar");
            if (renameToFile.exists()) {
                if (!renameToFile.delete()) {
                    throw new RuntimeException("Delete old File Error");
                }
            }
            if (!file.renameTo(renameToFile)) {
                throw new RuntimeException("Rename Main File Error");
            }
        }
    }

    private static void setHashCode(VersionID versionID) throws IOException, NoSuchAlgorithmException, InterruptedException {
        versionID.setFileHashCodeType("SHA-256");
        versionID.setStartMainFile_SHA_256(FileHashUtil.getFileSHA256("artifacts/PicturePlayer_jar/PicturePlayerRunner.exe"));
        versionID.setNormalVersionDescribe_SHA_256(FileHashUtil.getFileSHA256("artifacts/PicturePlayer_jar/Describe/" + PicturePlayerVersion.getVersionID() + ".txt"));
        versionID.setNormalVersionMainFile_SHA_256(FileHashUtil.getFileSHA256("artifacts/PicturePlayer_jar/" + PicturePlayerVersion.getShorterVersion() + ".jar"));


        TreeMap<String, String> dependencies = new TreeMap<>();
        HashSet<?> hashSet = verifierAndGetDependencies_SHA_256_Set(dependencies);
        versionID.setNormalDependencies_SHA_256(dependencies);
        System.out.println("Dependency counts:" + dependencies.size());
        if (hashSet.size() != dependencies.size()) {
            throw new RuntimeException("Dependencies are incomplete!");
        }

        ThreadControl.virtualThreadsController.shutdown(0, TimeUnit.SECONDS);
    }

    private static HashSet<File> verifierAndGetDependencies_SHA_256_Set(TreeMap<String, String> dependencies) throws IOException, NoSuchAlgorithmException {
        File libFile = new File("artifacts/PicturePlayer_jar/lib/");
        JarVersionCleaner.cleanOldVersions(libFile.getPath(), false);

        // 重命名jar，获取全部处理后的文件集合
        HashSet<File> hashSet = JarFileRenamer.renameJarFile(libFile.getPath());
        // 存放合法、无名称冲突的jar文件
        List<File> validJarList = getFiles(dependencies, hashSet);

        // 批量并发计算所有合法jar的SHA256（虚拟线程批量提速）
        Map<File, String> fileShaMap = FileHashUtil.batchGetSHA256(validJarList);

        // 填充依赖映射
        for (Map.Entry<File, String> entry : fileShaMap.entrySet()) {
            File jarFile = entry.getKey();
            String fileName = jarFile.getName();
            int dashIndex = fileName.lastIndexOf("-");
            String depPrefix = dashIndex == -1 ? fileName : fileName.substring(0, dashIndex);
            dependencies.put(depPrefix, entry.getValue());
        }

        return hashSet;
    }

    private static @NonNull List<File> getFiles(TreeMap<String, String> dependencies, HashSet<File> hashSet) {
        List<File> validJarList = new ArrayList<>();

        // 单次遍历完成：过滤jar文件 + 检测依赖名冲突
        for (File file : hashSet) {
            String fileName = file.getName();
            // 只处理普通jar文件
            if (!file.isFile() || !fileName.endsWith(".jar")) continue;

            // 分割前缀：xxx-1.0.jar → 取 xxx；无横线则直接用完整文件名
            int dashIndex = fileName.lastIndexOf("-");
            String depPrefix;
            if (dashIndex == -1) depPrefix = fileName;
            else depPrefix = fileName.substring(0, dashIndex);

            validJarList.add(file);
        }
        return validJarList;
    }
}

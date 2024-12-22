package Tools.File.ImageThumbnailManage;

import java.io.*;

public class Center {
    public static FileManage fileManage;
    public static String storePlace = "data/obj.thum";

    static {
        File file = new File(storePlace);
        if (!(file.exists() && file.isFile())) {
            fileManage = new FileManage();
            System.out.println("Waring:Can't find thumbnail file or thumbnail file was broke");
            try {
                System.out.println("Info:Creating new thumbnail file...");
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Error:" + e.getMessage());
            }
        } else if (new File(storePlace).length() == 0) {
            fileManage = new FileManage();
        } else {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file))) {
                fileManage = (FileManage) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                fileManage = new FileManage();
                System.out.println("Error:" + e.getMessage());
                System.out.println("Waring:Can't find thumbnail file or thumbnail file was broke");
            }
        }
    }

    public void save() {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(storePlace))) {
            objectOutputStream.writeObject(fileManage);
        } catch (IOException e) {
            System.out.println("Error:" + e.getMessage());
        }
    }

}

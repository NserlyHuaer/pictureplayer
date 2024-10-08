package Dev;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class SerializationOperator {
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    public ArrayList<Temp> arrayList;
    public static final File file;

    static {
        file = new File("");
    }
    {
        arrayList = new ArrayList<>();
    }

    public SerializationOperator() {

    }

    public SerializationOperator(Temp... temp) {
        for (Temp t : temp) {
            assert arrayList != null;
            arrayList.add(t);
        }
    }

    /**
     * result[1]:返回保存失败的实例数，如果没有实例需保存返回值为-1 \t
     * result[2]:返回保存成功的实例数 \t
     * result[3]:返回已更改的实例数 \t
     * result[4]:返回新建实例数 \t
     * result[5]:返回一共实例数 \t
     */
    public int[] Save() {
        int TotalCount = arrayList.size();
        int failedCount = 0;
        int saved = 0;
        int changed = 0;
        int created = 0;
            return null;


    }
}

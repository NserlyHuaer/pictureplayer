package top.nserly.SoftwareCollections_API.String;

import java.util.ArrayList;
import java.util.Collections;

public final class StringPro implements Cloneable {
    private StringBuffer str;


    public void append(Object object) {
        str.append(object.toString());
    }

    public StringPro(String string) {
        str = new StringBuffer(string);
    }

    public void clear() {
        str = new StringBuffer();
    }

    public StringPro() {
        str = new StringBuffer();
    }

    public StringPro(StringPro stringPro) {
        str = new StringBuffer(stringPro.toString());
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<String> StringToList(String... strings) {
        ArrayList<String> arrayList = new ArrayList<>();
        Collections.addAll(arrayList, strings);
        return arrayList;
    }

    public void appendLn(Object object) {
        str.append(object).append("\n");
    }

    public String toString() {
        return str.toString();
    }

    public StringBuffer toStringBuffer() {
        return str;
    }

    public int hashCode() {
        return str.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        StringBuffer str = ((StringPro) object).toStringBuffer();
        return this.toString().contentEquals(str);
    }
}

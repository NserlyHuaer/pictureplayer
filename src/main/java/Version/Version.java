package Version;

public class Version {
    private static final String version = "1.0.0 beta1";
    private static final long VersionID = 1255L;

    public static String getVersion() {
        return version;
    }

    public static long getVersionID() {
        return VersionID;
    }
}

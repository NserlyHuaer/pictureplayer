package top.nserly.SoftwareCollections_API.Handler.Exception;

import org.slf4j.Logger;

public class ExceptionHandler {
    public static void setUncaughtExceptionHandler(Logger logger) {
        Thread.setDefaultUncaughtExceptionHandler((e1, e2) -> {
            logger.error(getExceptionMessage(e2));
        });
    }

    public static String getExceptionMessage(Throwable e) {
        if (e == null) return null;
        StringBuilder stringBuilder = new StringBuilder(e.getClass().getName() + ":" + e.getMessage() + "\n");
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        if (stackTraceElements != null) {
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                stringBuilder.append("at ").append(stackTraceElement.getClassName()).append("(line:").append(stackTraceElement.getLineNumber()).append(")\n");
            }
        }
        Throwable throwable = e.getCause();
        if (throwable != null) stringBuilder.append("Caused by:").append(getExceptionMessage(throwable));
        return stringBuilder.toString();
    }
}

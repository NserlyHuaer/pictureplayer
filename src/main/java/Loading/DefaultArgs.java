package Loading;

public @interface DefaultArgs {
    boolean DoNotThingOnClose() default false;

    boolean EnableConfirmExit() default false;

    boolean EnableHistoryLoader() default true;

    double MouseMoveOffsets() default 0.0;
}

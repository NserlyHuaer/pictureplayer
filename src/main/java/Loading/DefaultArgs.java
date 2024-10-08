package Loading;

public @interface DefaultArgs {
    boolean DoNotThingOnClose() default false;

    boolean EnableConfirmExit() default true;

    boolean EnableHistoryLoader() default true;

    double MouseMoveOffsets() default 0.59;
}

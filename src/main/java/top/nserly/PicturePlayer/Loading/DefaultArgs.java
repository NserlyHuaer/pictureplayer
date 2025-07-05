package top.nserly.PicturePlayer.Loading;

public @interface DefaultArgs {

    //启用退出提示
    boolean EnableConfirmExit() default true;

    //启用历史路径加载
    boolean EnableHistoryLoader() default true;

    //鼠标移动补偿
    double MouseMoveOffsets() default 0.0;

    //启用代理服务器
    boolean EnableProxyServer() default false;

    //代理服务器
    String ProxyServer() default "";

    //启用安全连接模式
    boolean EnableSecureConnection() default true;

    //启用自动检测更新
    boolean AutoCheckUpdate() default true;

    //启动光标显示
    boolean EnableCursorDisplay() default false;

    //启用硬件加速
    boolean EnableHardwareAcceleration() default true;

}

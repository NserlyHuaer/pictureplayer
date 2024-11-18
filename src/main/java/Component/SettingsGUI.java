package Component;

import Listener.ChangeFocusListener;
import Loading.DefaultArgs;
import Runner.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class SettingsGUI extends JFrame {
    public static final Map<String, String> DefaultData = new HashMap<>();
    public final HashMap<String, String> CurrentData = new HashMap<String, String>();
    private final JPanel panel;
    private final JCheckBox DoNotThingOnCloseCheckBox;
    private final JCheckBox EnableConfirmExitCheckBox;
    private final JCheckBox EnableHistoryLoaderCheckBox;
    private final JCheckBox EnableCursorDisplayCheckBox;
    private final JLabel MouseMoveOffsetsLabel;
    private final JSlider MouseMoveOffsetsSlider;
    private final JCheckBox EnableProxyServerCheckBox;
    private final JCheckBox EnableSecureConnectionCheckBox;
    private final JCheckBox AutoCheckUpdateCheckBox;
    private JLabel ProxyServerLabel;
    private final JButton setProxyServerButton;
    private final JButton saveButton;
    private final JButton exitButton;
    private final JButton ResetButton;
    private boolean isRevised;
    private static SettingsGUI object;

    static {
        try {
            Class<?> clazz = DefaultArgs.class;
            // 获取注解
            AnnotatedElement element = clazz;
            // 获取注解中所有成员的方法
            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {
                if (!method.getName().equals("annotationType") && !method.isDefault()) { // 忽略内部的annotationType方法
                    String defaultValue = method.getDefaultValue().toString();
                    DefaultData.put(method.getName(), defaultValue);
                }
            }
        } catch (RuntimeException e) {
            System.out.println("Error:" + e);
        }
    }

    public SettingsGUI() {
        super("Settings");
        object = this;
        setBounds(570, 405, 320, 510);
        // 设置窗口不可调整大小

        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        panel = new JPanel();
        ChangeFocusListener changeFocusListener = new ChangeFocusListener(this);
        //创建窗体监听器
        this.addWindowListener(new WindowAdapter() {
            //设置窗体在显示时自动获取焦点
            @Override
            public void windowActivated(WindowEvent e) {
                //当前窗体成为活动窗体时，让myCanvas获取焦点
                object.requestFocus();
            }

            public void windowClosing(WindowEvent e) {
                if (isRevised) {
                    int choose = JOptionPane.showConfirmDialog(object, "Settings Information will lose if you don't save it", "Save your changes?", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (choose == 0) {
                        Main.init.Writer(CurrentData);
                        Main.jFrame.setVisible(true);
                        setVisible(false);
                        isRevised = false;
                        setTitle("Settings");
                        System.out.println("Saving Settings...");
                    } else if (choose == 1) {
                        Main.jFrame.setVisible(true);
                        setVisible(false);
                        isRevised = false;
                        setTitle("Settings");
                    }
                } else {
                    Main.jFrame.setVisible(true);
                    setVisible(false);
                }
            }
        });
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (isRevised) {
                        int choose = JOptionPane.showConfirmDialog(object, "Settings Information will lose if you don't save it", "Save your changes?", JOptionPane.YES_NO_CANCEL_OPTION);
                        if (choose == 0) {
                            Main.init.Writer(CurrentData);
                            Main.jFrame.setVisible(true);
                            setVisible(false);
                            isRevised = false;
                            setTitle("Settings");
                            System.out.println("Saving Settings...");
                        } else if (choose == 1) {
                            Main.jFrame.setVisible(true);
                            setVisible(false);
                            isRevised = false;
                            setTitle("Settings");
                        }
                    } else {
                        Main.jFrame.setVisible(true);
                        setVisible(false);
                    }

                } else if (e.getKeyCode() == KeyEvent.VK_F10) {
                    Main.init.Writer(CurrentData);
                    Main.jFrame.setVisible(true);
                    setVisible(false);
                    isRevised = false;
                    setTitle("Settings");
                    System.out.println("Saving Settings...");
                }
            }
        });

        // 退出时，隐藏至系统托盘 - 复选框
        DoNotThingOnCloseCheckBox = new JCheckBox("退出时，隐藏至系统托盘");
        DoNotThingOnCloseCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CurrentData.replace("DoNotThingOnClose", String.valueOf(DoNotThingOnCloseCheckBox.isSelected()));
                isRevised = true;
                setTitle("Settings(Not saved)");
            }
        });

        // 启用退出提示 - 复选框
        EnableConfirmExitCheckBox = new JCheckBox("启用退出提示");
        EnableConfirmExitCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CurrentData.replace("EnableConfirmExit", String.valueOf(EnableConfirmExitCheckBox.isSelected()));
                isRevised = true;
                setTitle("Settings(Not saved)");
            }
        });

        // 启用历史路径加载 - 复选框
        EnableHistoryLoaderCheckBox = new JCheckBox("启用历史路径加载");
        EnableHistoryLoaderCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CurrentData.replace("EnableHistoryLoader", String.valueOf(EnableHistoryLoaderCheckBox.isSelected()));
                isRevised = true;
                setTitle("Settings(Not saved)");
            }
        });

        //启动光标显示
        EnableCursorDisplayCheckBox = new JCheckBox("启动光标显示");
        EnableCursorDisplayCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CurrentData.replace("EnableCursorDisplay", String.valueOf(EnableCursorDisplayCheckBox.isSelected()));
                isRevised = true;
                setTitle("Settings(Not saved)");
            }
        });

        // 鼠标移动补偿 - 滑动条
        MouseMoveOffsetsLabel = new JLabel();
        MouseMoveOffsetsSlider = new JSlider(-65, 150);
        MouseMoveOffsetsSlider.addChangeListener(e -> {
            CurrentData.replace("MouseMoveOffsets", String.valueOf(MouseMoveOffsetsSlider.getValue()));
            MouseMoveOffsetsLabel.setText("鼠标移动补偿: " + CurrentData.get("MouseMoveOffsets") + "%");
            isRevised = true;
            setTitle("Settings(Not saved)");
        });

        // 启用代理服务器 - 复选框
        EnableProxyServerCheckBox = new JCheckBox("启用代理服务器");
        EnableProxyServerCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CurrentData.replace("EnableProxyServer", String.valueOf(EnableProxyServerCheckBox.isSelected()));
                ChangeProxyServer(EnableProxyServerCheckBox.isSelected());
                isRevised = true;
                setTitle("Settings(Not saved)");
            }
        });

        // 代理服务器 - 文本框
        ProxyServerLabel = new JLabel("代理服务器: ");
        setProxyServerButton = new JButton("设置代理服务器");
        setProxyServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newProxyServer = null;
                if (!CurrentData.get("ProxyServer").toString().trim().isEmpty())
                    newProxyServer = JOptionPane.showInputDialog("Please type here for Proxy Server", CurrentData.get("ProxyServer"));
                else
                    newProxyServer = JOptionPane.showInputDialog("Please type here for Proxy Server", "代理服务器地址");
                if (newProxyServer != null && !newProxyServer.equals("代理服务器地址") && !newProxyServer.isEmpty()) {
                    CurrentData.replace("ProxyServer", newProxyServer);
                    ProxyServerLabel.setText("代理服务器: " + CurrentData.get("ProxyServer"));
                    isRevised = true;
                    setTitle("Settings(Not saved)");
                }
            }
        });

        // 启用安全连接模式 - 复选框
        EnableSecureConnectionCheckBox = new JCheckBox("启用安全连接模式");
        EnableSecureConnectionCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!EnableSecureConnectionCheckBox.isSelected()) {
                    EnableSecureConnectionCheckBox.setSelected(true);
                    int choose = JOptionPane.showConfirmDialog(object, "Are you sure it's closed?\nIt may make the computer more vulnerable", "Turn off Secure Connection", JOptionPane.YES_NO_OPTION);
                    if (choose == 1) {
                        return;
                    }
                    EnableSecureConnectionCheckBox.setSelected(false);
                }
                CurrentData.replace("EnableSecureConnection", String.valueOf(EnableSecureConnectionCheckBox.isSelected()));
                isRevised = true;
                setTitle("Settings(Not saved)");
            }
        });

        // 启用自动检测更新 - 复选框
        AutoCheckUpdateCheckBox = new JCheckBox("启用自动检测更新");
        AutoCheckUpdateCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CurrentData.replace("AutoCheckUpdate", String.valueOf(AutoCheckUpdateCheckBox.isSelected()));
                isRevised = true;
                setTitle("Settings(Not saved)");
            }
        });

        saveButton = new JButton("保存");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.init.Writer(CurrentData);
                Main.jFrame.setVisible(true);
                setVisible(false);
                isRevised = false;
                setTitle("Settings");
                System.out.println("Saving Settings...");
            }
        });

        exitButton = new JButton("退出（不保存）");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.jFrame.setVisible(true);
                setVisible(false);
                isRevised = false;
                setTitle("Settings");
            }
        });
        ResetButton = new JButton("ReSet");
        ResetButton.addActionListener(e -> {
            setDefault();
            CurrentData.clear();
            CurrentData.putAll(DefaultData);
            isRevised = true;
            setTitle("Settings(Not Saved)");
        });

        panel.setLayout(null);
        DoNotThingOnCloseCheckBox.setBounds(50, 0, 200, 30);
        EnableConfirmExitCheckBox.setBounds(50, 40, 200, 30);
        EnableHistoryLoaderCheckBox.setBounds(50, 80, 200, 30);
        EnableCursorDisplayCheckBox.setBounds(50, 120, 200, 30);
        MouseMoveOffsetsLabel.setBounds(50, 160, 180, 30);
        MouseMoveOffsetsSlider.setBounds(15, 190, 280, 30);
        EnableProxyServerCheckBox.setBounds(50, 230, 200, 30);

        panel.add(DoNotThingOnCloseCheckBox);
        DoNotThingOnCloseCheckBox.addMouseListener(changeFocusListener);
        panel.add(EnableConfirmExitCheckBox);
        EnableConfirmExitCheckBox.addMouseListener(changeFocusListener);
        panel.add(EnableHistoryLoaderCheckBox);
        EnableHistoryLoaderCheckBox.addMouseListener(changeFocusListener);
        panel.add(EnableCursorDisplayCheckBox);
        EnableCursorDisplayCheckBox.addMouseListener(changeFocusListener);
        panel.add(MouseMoveOffsetsLabel);
        panel.add(MouseMoveOffsetsSlider);
        MouseMoveOffsetsSlider.addMouseListener(changeFocusListener);
        panel.add(EnableProxyServerCheckBox);
        EnableProxyServerCheckBox.addMouseListener(changeFocusListener);
        panel.add(ProxyServerLabel);
        panel.add(setProxyServerButton);
        setProxyServerButton.addMouseListener(changeFocusListener);
        panel.add(EnableSecureConnectionCheckBox);
        EnableSecureConnectionCheckBox.addMouseListener(changeFocusListener);
        panel.add(AutoCheckUpdateCheckBox);
        AutoCheckUpdateCheckBox.addMouseListener(changeFocusListener);
        panel.add(saveButton);
        saveButton.addMouseListener(changeFocusListener);
        panel.add(exitButton);
        exitButton.addMouseListener(changeFocusListener);
        panel.add(ResetButton);
        ResetButton.addMouseListener(changeFocusListener);

        // 设置 JFrame 的默认布局为 BorderLayout
        getContentPane().setLayout(new BorderLayout());

        // 将 JPanel 添加到 JFrame 的 CENTER 区域，这将自动居中 JPanel
        getContentPane().add(panel, BorderLayout.CENTER);
    }


    public void setDefault() {
        DoNotThingOnCloseCheckBox.setSelected(getBoolean("DoNotThingOnClose", DefaultData));
        EnableConfirmExitCheckBox.setSelected(getBoolean("EnableConfirmExit", DefaultData));
        EnableHistoryLoaderCheckBox.setSelected(getBoolean("EnableHistoryLoader", DefaultData));
        EnableCursorDisplayCheckBox.setSelected(getBoolean("EnableCursorDisplay", DefaultData));
        MouseMoveOffsetsSlider.setValue((int) getDouble("MouseMoveOffsets", CurrentData, -65, 150));
        EnableProxyServerCheckBox.setSelected(getBoolean("EnableProxyServer", DefaultData));
        EnableSecureConnectionCheckBox.setSelected(getBoolean("EnableSecureConnection", DefaultData));
        AutoCheckUpdateCheckBox.setSelected(getBoolean("AutoCheckUpdate", DefaultData));
        ProxyServerLabel = new JLabel("代理服务器: " + DefaultData.get("ProxyServer"));
        ChangeProxyServer(getBoolean("EnableProxyServer", DefaultData));
    }

    public void ReadFile() {
        CurrentData.putAll(DefaultData);
        try {
            Main.init.Run();
            Properties properties = Main.init.getProperties();
            for (Object obj : properties.keySet()) {
                if (DefaultData.containsKey((String) obj)) {
                    CurrentData.replace((String) obj, (String) properties.get(obj));
                }
            }
        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
    }

    public void reFresh() {
        ReadFile();
        DoNotThingOnCloseCheckBox.setSelected(getBoolean("DoNotThingOnClose", CurrentData));
        EnableConfirmExitCheckBox.setSelected(getBoolean("EnableConfirmExit", CurrentData));
        EnableHistoryLoaderCheckBox.setSelected(getBoolean("EnableHistoryLoader", CurrentData));
        EnableCursorDisplayCheckBox.setSelected(getBoolean("EnableCursorDisplay", CurrentData));
        MouseMoveOffsetsSlider.setValue((int) getDouble("MouseMoveOffsets", CurrentData, -65, 150));
        EnableProxyServerCheckBox.setSelected(getBoolean("EnableProxyServer", CurrentData));
        EnableSecureConnectionCheckBox.setSelected(getBoolean("EnableSecureConnection", CurrentData));
        AutoCheckUpdateCheckBox.setSelected(getBoolean("AutoCheckUpdate", CurrentData));
        ProxyServerLabel = new JLabel("代理服务器: " + CurrentData.get("ProxyServer"));
        MouseMoveOffsetsLabel.setText("鼠标移动补偿: " + CurrentData.get("MouseMoveOffsets") + "%");
        ChangeProxyServer(getBoolean("EnableProxyServer", CurrentData));
    }

    public void setVisible(boolean visible) {
        setTitle("Settings");
        isRevised = false;
        if (visible) {
            reFresh();
        }
        super.setVisible(visible);
    }

    private void ChangeProxyServer(boolean enable) {
        if (enable) {
            ProxyServerLabel.setBounds(50, 260, 200, 30);
            setProxyServerButton.setBounds(50, 290, 200, 30);
            EnableSecureConnectionCheckBox.setBounds(50, 320, 200, 30);
            AutoCheckUpdateCheckBox.setBounds(50, 350, 200, 30);
            saveButton.setBounds(50, 390, 80, 30);
            exitButton.setBounds(150, 390, 120, 30);
            ResetButton.setBounds(90, 430, 125, 30);
            panel.add(ProxyServerLabel);
            panel.add(setProxyServerButton);
            return;
        }
        panel.remove(ProxyServerLabel);
        panel.remove(setProxyServerButton);
        EnableSecureConnectionCheckBox.setBounds(50, 260, 200, 30);
        AutoCheckUpdateCheckBox.setBounds(50, 300, 200, 30);
        saveButton.setBounds(50, 340, 80, 30);
        exitButton.setBounds(150, 340, 120, 30);
        ResetButton.setBounds(90, 380, 125, 30);
        revalidate();
    }


    private static boolean getBoolean(String Description, Map map) {
        String cache = map.get(Description).toString().replace(" ", "").toLowerCase();
        if (cache.equals("true")) {
            return true;
        } else if (cache.equals("false")) {
            return false;
        }
        return (boolean) map.get(Description);
    }

    public static double getDouble(String Description, Map map) {
        return getDouble(Description, map, -65, 150);
    }

    private static double getDouble(String Description, Map map, double min, double max) {
        if (min > max) {
            double temp = max;
            max = min;
            min = temp;
        }
        String cache = map.get(Description).toString().replace(" ", "");
        double result = 0;
        try {
            result = Double.parseDouble(cache);
        } catch (NumberFormatException e) {

        }
        if (result > max) {
            result = max;
        }
        if (result < min) {
            result = min;
        }
        return result;
    }

}

package Component;

import Command.CommandCenter;
import Runner.Main$$$;
import Version.Download.DownloadFile;
import Version.Download.DownloadUpdate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

public class AdvancedDownloadSpeedDisplay$$$ {

    private JProgressBar totalProgress;
    private JProgressBar currentFileProgress;
    private JLabel speedLabel;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public void createAndShowGUI(java.util.List<String> downloadWebSide) {
        // 创建一个顶级窗口
        JFrame frame = new JFrame("Update Window");

        // 创建确认更新的对话框
        int confirmResult = JOptionPane.showConfirmDialog(frame,
                "检测到新版本，是否立即更新？",
                "更新提示",
                JOptionPane.YES_NO_OPTION);

        if (confirmResult == JOptionPane.YES_OPTION) {
            // 用户选择了"是"
            // 创建进度条
            totalProgress = new JProgressBar(0, downloadWebSide.size());
            totalProgress.setValue(0);
            totalProgress.setStringPainted(true);

            // 创建另一个进度条，用于显示当前文件的进度
            currentFileProgress = new JProgressBar(0, 100);
            currentFileProgress.setValue(0);
            currentFileProgress.setStringPainted(true);

            // 创建标签显示下载速度
            speedLabel = new JLabel("speed: 0 B/s");

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(totalProgress, BorderLayout.NORTH);
            contentPanel.add(currentFileProgress, BorderLayout.CENTER);
            contentPanel.add(speedLabel, BorderLayout.SOUTH);

            // 添加面板到主要内容窗格
            frame.add(contentPanel);

            // 设置窗口大小及位置
            frame.setSize(300, 200);
            frame.setLocationRelativeTo(null);

            frame.setVisible(true);
            DownloadUpdate downloadUpdate = new DownloadUpdate(Main$$$.UPDATE_WEBSITE);
            Main$$$.DaemonUpdate = new Thread(() -> {
                Map<String, java.util.List> map = downloadUpdate.download(downloadUpdate.getUpdateWebSide());
                try {
                    CommandCenter.moveFileToDirectory((String) map.get(downloadUpdate.FilePath.getFirst()).getFirst());
                    String osType = CommandCenter.detectOSType();
                    contentPanel.removeAll();
                    CommandCenter.executeOSSpecificCommands(osType, (String) map.get(downloadUpdate.FilePath.getFirst()).getFirst());
                } catch (IOException e) {
                    System.out.println("Error:" + e);
                    JOptionPane.showConfirmDialog(frame,
                            "File replacement failed",
                            "ERROR",
                            JOptionPane.YES_NO_OPTION);
                    frame.setVisible(false);

                }
            });
            Main$$$.DaemonUpdate.start();
            new Thread(() -> {
                simulateDownload(downloadUpdate.CurrentDownloadingFile, downloadUpdate.TotalDownloadingFile - downloadUpdate.HaveDownloadedFile, downloadUpdate.TotalDownloadingFile);
            }).start();

        }
    }

    private void simulateDownload(DownloadFile downloadFile, int currentProgressFile, int totalFile) {
        final Timer totalTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (currentProgressFile < totalFile) {
                    // 更新总进度
                    totalProgress.setValue(currentProgressFile);
                } else {
                    totalProgress.setValue(totalFile);
                    ((Timer) actionEvent.getSource()).stop(); // 停止计时器
                }
            }
        });

        final Timer fileTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (downloadFile == null) return;
                if (downloadFile.progress < 100) {
                    // 更新当前文件进度
                    currentFileProgress.setValue((int) downloadFile.progress);
                    String speed = formatSpeed(downloadFile.bps);
                    speedLabel.setText("speed: " + speed);
                } else {
                    currentFileProgress.setValue(100);
                    String speed = formatSpeed(0);
                    speedLabel.setText("speed: " + speed);
                    ((Timer) actionEvent.getSource()).stop(); // 停止计时器
                }
            }
        });

        totalTimer.start();
        fileTimer.start();
    }


    private String formatSpeed(double bytesPerSecond) {
        if (bytesPerSecond >= 1024L * 1024 * 1024 * 8) {
            return decimalFormat.format((double) bytesPerSecond / (1024L * 1024 * 1024 * 8)) + " GB/s";
        } else if (bytesPerSecond >= 1024 * 1024 * 8) {
            return decimalFormat.format((double) bytesPerSecond / (1024 * 1024 * 8)) + " MB/s";
        } else if (bytesPerSecond >= 1024 * 8) {
            return decimalFormat.format((double) bytesPerSecond / 1024 * 8) + " KB/s";
        } else {
            return decimalFormat.format(bytesPerSecond) + " B/s";
        }
    }
}
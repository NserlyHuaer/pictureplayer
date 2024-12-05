package Component;

import Command.CommandCenter;
import Runner.Main;
import Tools.String.Formation;
import Version.Download.DownloadFile;
import Version.Download.DownloadUpdate;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class AdvancedDownloadSpeed {

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");
    public Thread DaemonUpdate;
    private JProgressBar totalProgress;
    private JProgressBar currentFileProgress;
    private JLabel speedLabel;
    private JLabel DownloadCountings;
    public static String speedPrefix;
    public static String totalPrefix;
    private Formation formation;
    private DownloadUpdate downloadUpdate;

    public AdvancedDownloadSpeed(DownloadUpdate downloadUpdate, JProgressBar totalProgress, JProgressBar currentFileProgress, JLabel speedLabel, JLabel DownloadCountings) {
        this.totalProgress = totalProgress;
        this.currentFileProgress = currentFileProgress;
        this.speedLabel = speedLabel;
        this.DownloadCountings = DownloadCountings;
        this.downloadUpdate = downloadUpdate;
        speedPrefix = speedLabel.getText();
        // 初始进度条
        totalProgress.setMaximum(downloadUpdate.getUpdateWebSide().size());
        totalProgress.setValue(0);
        totalProgress.setStringPainted(true);
        Formation formation = new Formation(DownloadCountings.getText());
        formation.Change("total", String.valueOf(downloadUpdate.getUpdateWebSide().size()));
        totalPrefix = String.valueOf(formation.getResult());

        // 初始另一个进度条，用于显示当前文件的进度
        currentFileProgress.setMaximum(100);
        currentFileProgress.setValue(0);
        currentFileProgress.setStringPainted(true);

        DaemonUpdate = new Thread(() -> {
            Map<String, List> map = downloadUpdate.download();
            String website = "";
            try {
                boolean isFound = false;
                for (String websites : downloadUpdate.FilePath) {
                    if (map.get(websites) != null) {
                        isFound = true;
                        website = websites;
                    }
                }
                if (!isFound) return;
                CommandCenter.moveFileToDirectory((String) map.get(website).getFirst());
                String osType = CommandCenter.detectOSType();
                CommandCenter.executeOSSpecificCommands(osType, (String) map.get(website).getFirst());
            } catch (IOException e) {
                System.out.println("Error:" + e);
            }
        });
        DaemonUpdate.start();
        new Thread(() -> {
            simulateDownload(downloadUpdate.CurrentDownloadingFile, downloadUpdate.TotalDownloadingFile - downloadUpdate.HaveDownloadedFile, downloadUpdate.TotalDownloadingFile);
        }).start();

    }


    private void simulateDownload(DownloadFile downloadFile, int currentProgressFile, int totalFile) {
        final Timer totalTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (currentProgressFile < totalFile) {
                    // 更新总进度
                    totalProgress.setValue(currentProgressFile);
                    formation = new Formation(totalPrefix);
                    formation.Change("current", String.valueOf(downloadUpdate.HaveDownloadedFile + 1));
                    DownloadCountings.setText(formation.getResult().toString());
                } else {
                    totalProgress.setValue(totalFile);
                    formation = new Formation(totalPrefix);
                    formation.Change("current", String.valueOf(downloadUpdate.TotalDownloadingFile));
                    DownloadCountings.setText(formation.getResult().toString());
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
                    speedLabel.setText(speedPrefix + speed);
                } else {
                    currentFileProgress.setValue(100);
                    String speed = formatSpeed(0);
                    speedLabel.setText(speedPrefix + speed);
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
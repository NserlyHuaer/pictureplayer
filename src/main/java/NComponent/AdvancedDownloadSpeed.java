package NComponent;

import Command.CommandCenter;
import Loading.Bundle;
import Runner.Main;
import Tools.String.Formation;
import Version.DownloadUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class AdvancedDownloadSpeed {
    private static DecimalFormat decimalFormat = new DecimalFormat("#.##");
    public Thread DaemonUpdate;
    private JProgressBar totalProgress;
    private JProgressBar currentFileProgress;
    private JLabel speedLabel;
    private JLabel DownloadCountings;
    private final Formation TotalFormation1 = new Formation("{Speed} - {FinishedSize}/{TotalSize},{NeedTime}");
    private final Formation TotalFormation2 = new Formation("{Speed} - {FinishedSize}/0B");
    private Formation currentFormation;
    private Formation totalFormation;
    private DownloadUpdate downloadUpdate;
    private static final Logger logger = LoggerFactory.getLogger(AdvancedDownloadSpeed.class);

    public AdvancedDownloadSpeed(DownloadUpdate downloadUpdate, JProgressBar totalProgress, JProgressBar currentFileProgress, JLabel speedLabel, JLabel DownloadCountings) {
        this.totalProgress = totalProgress;
        this.currentFileProgress = currentFileProgress;
        this.speedLabel = speedLabel;
        this.DownloadCountings = DownloadCountings;
        this.downloadUpdate = downloadUpdate;
        // 初始进度条
        totalProgress.setMaximum(downloadUpdate.getUpdateWebSide().size());
        totalProgress.setValue(0);
        totalProgress.setStringPainted(true);
        Formation formation = new Formation(DownloadCountings.getText());
        formation.change("total", String.valueOf(downloadUpdate.getUpdateWebSide().size()));
        totalFormation = new Formation(formation.getProcessingString());

        // 初始另一个进度条，用于显示当前文件的进度
        currentFileProgress.setMaximum(100);
        currentFileProgress.setValue(0);
        currentFileProgress.setStringPainted(true);

        DaemonUpdate = new Thread(() -> {
            Map<String, List> map = downloadUpdate.download();
            if (map == null) {
                DownloadUpdateFrame.downloadUpdateFrame.dispose();
                return;
            }
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
            } catch (IOException | NoClassDefFoundError | ExceptionInInitializerError e) {
                logger.error(e.toString());
                JOptionPane.showMessageDialog(DownloadUpdateFrame.downloadUpdateFrame, Bundle.getMessage("UpdateError_Content") + "\nCaused by:" + e, Bundle.getMessage("UpdateError_Title"), JOptionPane.ERROR_MESSAGE);
                DownloadUpdateFrame.downloadUpdateFrame.dispose();
                Main.main.setVisible(true);
            }
        });
        DaemonUpdate.start();
        new Thread(() -> {
            simulateDownload();
        }).start();
    }


    private void simulateDownload() {
        final Timer totalTimer = new Timer(350, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int totalFile = downloadUpdate.TotalDownloadingFile;
                int currentProgressFile = downloadUpdate.HaveDownloadedFile;
                totalProgress.setMaximum(downloadUpdate.getUpdateWebSide().size() * 100);
                if (downloadUpdate.CurrentDownloadingFile.isCompleted) {
                    if (downloadUpdate.CurrentDownloadingFile.isGettingFileSize) {
                        currentFormation = TotalFormation1;
                    } else {
                        currentFormation = TotalFormation2;
                    }
                    currentFormation.change("current", String.valueOf(totalFile));
                    DownloadCountings.setText(currentFormation.getProcessingString());
                    totalProgress.setValue(totalProgress.getMaximum());
                    ((Timer) actionEvent.getSource()).stop(); // 停止计时器
                } else {
                    // 更新总进度
                    totalProgress.setValue((100 * currentProgressFile + (int) downloadUpdate.CurrentDownloadingFile.progress));
                    totalFormation.change("current", String.valueOf(currentProgressFile + 1));
                    DownloadCountings.setText(totalFormation.getProcessingString());
                }
            }
        });

        final Timer fileTimer = new Timer(350, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (downloadUpdate.CurrentDownloadingFile == null) return;
                if (downloadUpdate.CurrentDownloadingFile.progress < 100) {
                    // 更新当前文件进度
                    currentFileProgress.setValue((int) downloadUpdate.CurrentDownloadingFile.progress);
                    if (currentFormation == null) currentFormation = TotalFormation1;
                    currentFormation.change("Speed", formatSpeed(downloadUpdate.CurrentDownloadingFile.BytesPerSecond));
                    currentFormation.change("FinishedSize", formatBytes(downloadUpdate.CurrentDownloadingFile.CurrentCompletedBytesRead));
                    if (downloadUpdate.CurrentDownloadingFile.isGettingFileSize) {
                        currentFormation.change("TotalSize", formatBytes(downloadUpdate.CurrentDownloadingFile.fileSize));
                        currentFormation.change("NeedTime", formatTimes(downloadUpdate.CurrentDownloadingFile.NeedDownloadTime));
                    }
                    speedLabel.setText(currentFormation.getProcessingString());
                } else {
                    currentFileProgress.setValue(100);
                    speedLabel.setText("0B/s - " + formatBytes(downloadUpdate.CurrentDownloadingFile.CurrentCompletedBytesRead));
                    ((Timer) actionEvent.getSource()).stop(); // 停止计时器
                }
            }
        });

        totalTimer.start();
        fileTimer.start();
    }


    private String formatSpeed(double bytesPerSecond) {
        return formatBytes(bytesPerSecond) + "/s";
    }

    public static String formatBytes(double bytes) {
        if (bytes >= 1099511627776L) {
            return decimalFormat.format(bytes / 1099511627776L) + "TB";
        } else if (bytes >= 1073741824) {
            return decimalFormat.format(bytes / (1073741824)) + "GB";
        } else if (bytes >= 1048576) {
            return decimalFormat.format(bytes / (1048576)) + "MB";
        } else if (bytes >= 1024) {
            return decimalFormat.format(bytes / 1024) + "KB";
        } else {
            return decimalFormat.format(bytes);
        }
    }

    private String formatTimes(long seconds) {
        if (seconds >= 2592000) {
            return decimalFormat.format(seconds / 2592000) + "months" + formatTimes(seconds % 2592000);
        } else if (seconds >= 86400) {
            return decimalFormat.format(seconds / (86400)) + "days" + formatTimes(seconds % 86400);
        } else if (seconds >= 3600) {
            return decimalFormat.format(seconds / (3600)) + "h" + formatTimes(seconds % 3600);
        } else if (seconds >= 60) {
            return decimalFormat.format(seconds / 60) + "min" + formatTimes(seconds % 60);
        } else {
            return seconds + "s";
        }
    }
}
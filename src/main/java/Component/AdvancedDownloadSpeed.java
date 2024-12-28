package Component;

import Command.CommandCenter;
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
    public static String speedPrefix = "{Speed} - {FinishedSize}/{TotalSize},{NeedTime}";
    public static String totalPrefix;
    private Formation Totalformation;
    private Formation currentFormation;
    private DownloadUpdate downloadUpdate;

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
        formation.Change("total", String.valueOf(downloadUpdate.getUpdateWebSide().size()));
        totalPrefix = String.valueOf(formation.getResult());

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
            } catch (IOException e) {
                System.out.println("Error:" + e);
            }
        });
        DaemonUpdate.start();
        new Thread(() -> {
            simulateDownload(downloadUpdate.TotalDownloadingFile - downloadUpdate.HaveDownloadedFile, downloadUpdate.TotalDownloadingFile);
        }).start();
    }


    private void simulateDownload(int currentProgressFile, int totalFile) {
        final Timer totalTimer = new Timer(350, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (downloadUpdate.CurrentDownloadingFile.isGettingFileSize)
                    speedPrefix = "{Speed} - {FinishedSize}/{TotalSize},{NeedTime}";
                else
                    speedPrefix = "{Speed} - {FinishedSize}/0B";
                if (currentProgressFile < totalFile) {
                    // 更新总进度
                    totalProgress.setValue(currentProgressFile);
                    Totalformation = new Formation(totalPrefix);
                    Totalformation.Change("current", String.valueOf(downloadUpdate.HaveDownloadedFile + 1));
                    DownloadCountings.setText(Totalformation.getResult().toString());
                } else {
                    totalProgress.setValue(totalFile);
                    Totalformation = new Formation(totalPrefix);
                    Totalformation.Change("current", String.valueOf(downloadUpdate.TotalDownloadingFile));
                    DownloadCountings.setText(Totalformation.getResult().toString());
                    ((Timer) actionEvent.getSource()).stop(); // 停止计时器
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
                    currentFormation = new Formation(speedPrefix);
                    currentFormation.Change("Speed", formatSpeed(downloadUpdate.CurrentDownloadingFile.BytesPerSecond));
                    currentFormation.Change("FinishedSize", formatBytes(downloadUpdate.CurrentDownloadingFile.CurrentCompletedBytesRead));
                    if (downloadUpdate.CurrentDownloadingFile.isGettingFileSize) {
                        currentFormation.Change("TotalSize", formatBytes(downloadUpdate.CurrentDownloadingFile.fileSize));
                        currentFormation.Change("NeedTime", formatTimes(downloadUpdate.CurrentDownloadingFile.NeedDownloadTime));
                    }
                    speedLabel.setText(currentFormation.getResult().toString());
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

    private String formatBytes(double bytes) {
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
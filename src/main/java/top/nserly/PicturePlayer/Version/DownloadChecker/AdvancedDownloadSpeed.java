package top.nserly.PicturePlayer.Version.DownloadChecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.nserly.GUIStarter;
import top.nserly.PicturePlayer.Command.CommandHandle;
import top.nserly.PicturePlayer.Loading.Bundle;
import top.nserly.PicturePlayer.NComponent.Frame.DownloadUpdateFrame;
import top.nserly.SoftwareCollections_API.Handler.Exception.ExceptionHandler;
import top.nserly.SoftwareCollections_API.String.StringFormation;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

public class AdvancedDownloadSpeed {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    public Thread DaemonUpdate;
    private final JProgressBar totalProgress;
    private final JProgressBar currentFileProgress;
    private final JLabel speedLabel;
    private final JLabel DownloadCountings;
    private final StringFormation TotalFormation1 = new StringFormation("{Speed} - {FinishedSize}/{TotalSize},{NeedTime}");
    private final StringFormation TotalFormation2 = new StringFormation("{Speed} - {FinishedSize}/0B");
    private final StringFormation totalFormation;
    private final CheckAndDownloadUpdate downloadUpdate;
    private StringFormation currentFormation;
    private static final Logger logger = LoggerFactory.getLogger(AdvancedDownloadSpeed.class);

    public AdvancedDownloadSpeed(CheckAndDownloadUpdate downloadUpdate, JProgressBar totalProgress, JProgressBar currentFileProgress, JLabel speedLabel, JLabel DownloadCountings) {
        this.totalProgress = totalProgress;
        this.currentFileProgress = currentFileProgress;
        this.speedLabel = speedLabel;
        this.DownloadCountings = DownloadCountings;
        this.downloadUpdate = downloadUpdate;
        // 初始进度条
        totalProgress.setMaximum(downloadUpdate.getUpdateWebSide().size());
        totalProgress.setValue(0);
        totalProgress.setStringPainted(true);
        StringFormation formation = new StringFormation(DownloadCountings.getText());
        formation.add("total", String.valueOf(downloadUpdate.getUpdateWebSide().size()));
        totalFormation = new StringFormation(formation.getProcessingString());

        // 初始另一个进度条，用于显示当前文件的进度
        currentFileProgress.setMaximum(100);
        currentFileProgress.setValue(0);
        currentFileProgress.setStringPainted(true);

        DaemonUpdate = new Thread(() -> {
            Map<String, ArrayList> map = downloadUpdate.download();
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
                ArrayList<String> arrayList = new ArrayList<>();
                for (String s : map.keySet()) {
                    arrayList.add((String) map.get(s).getFirst());
                }
                arrayList.remove((String) map.get(downloadUpdate.MainFileWebSite).getFirst());
                CommandHandle.moveFileToLibDirectory(arrayList);
                CommandHandle.moveFileToDirectory((String) map.get(downloadUpdate.MainFileWebSite).getFirst());
                String osType = CommandHandle.detectOSType();
                String OpenedPicturePath = null;
                if (GUIStarter.main != null && GUIStarter.main.paintPicture != null && GUIStarter.main.paintPicture.imageCanvas != null) {
                    OpenedPicturePath = GUIStarter.main.paintPicture.imageCanvas.getPath();
                }

                CommandHandle.executeOSSpecificCommands(osType, (String) map.get(website).getFirst(), OpenedPicturePath);
            } catch (IOException | NoClassDefFoundError | ExceptionInInitializerError e) {
                logger.error(ExceptionHandler.getExceptionMessage(e));
                JOptionPane.showMessageDialog(DownloadUpdateFrame.downloadUpdateFrame, Bundle.getMessage("UpdateError_Content") + "\nCaused by:" + e, Bundle.getMessage("UpdateError_Title"), JOptionPane.ERROR_MESSAGE);
                DownloadUpdateFrame.downloadUpdateFrame.dispose();
                GUIStarter.main.setVisible(true);
            }
        });
        DaemonUpdate.start();
        simulateDownload();
    }


    private void simulateDownload() {
        final Timer totalTimer = new Timer(350, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int totalFile = downloadUpdate.TotalDownloadingFile;
                int currentProgressFile = downloadUpdate.HaveDownloadedFile;
                totalProgress.setMaximum(downloadUpdate.getUpdateWebSide().size() * 100);
                if (downloadUpdate.CurrentFileDownloader.isCompleted()) {
                    if (downloadUpdate.CurrentFileDownloader.getFileSize() != -1) {
                        currentFormation = TotalFormation1;
                    } else {
                        currentFormation = TotalFormation2;
                    }
                    currentFormation.add("current", String.valueOf(totalFile));
                    DownloadCountings.setText(currentFormation.getProcessingString());
                    totalProgress.setValue(totalProgress.getMaximum());
                    ((Timer) actionEvent.getSource()).stop(); // 停止计时器
                } else {
                    // 更新总进度
                    totalProgress.setValue((100 * currentProgressFile + (int) downloadUpdate.CurrentFileDownloader.getProgress()));
                    totalFormation.add("current", String.valueOf(currentProgressFile + 1));
                    DownloadCountings.setText(totalFormation.getProcessingString());
                }
            }
        });

        final Timer fileTimer = new Timer(350, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (downloadUpdate.CurrentFileDownloader == null) return;
                if (downloadUpdate.CurrentFileDownloader.getProgress() < 100) {
                    // 更新当前文件进度
                    currentFileProgress.setValue((int) downloadUpdate.CurrentFileDownloader.getProgress());
                    if (currentFormation == null) currentFormation = TotalFormation1;
                    currentFormation.add("Speed", formatSpeed(downloadUpdate.CurrentFileDownloader.getSpeedBytesPerSecond()));
                    currentFormation.add("FinishedSize", formatBytes(downloadUpdate.CurrentFileDownloader.getBytesRead()));
                    if (downloadUpdate.CurrentFileDownloader.getFileSize() != -1) {
                        currentFormation.add("TotalSize", formatBytes(downloadUpdate.CurrentFileDownloader.getFileSize()));
                        currentFormation.add("NeedTime", formatTimes(downloadUpdate.CurrentFileDownloader.getRemainingSeconds()));
                    }
                    speedLabel.setText(currentFormation.getProcessingString());
                } else {
                    currentFileProgress.setValue(100);
                    speedLabel.setText("0B/s - " + formatBytes(downloadUpdate.CurrentFileDownloader.getBytesRead()));
                    ((Timer) actionEvent.getSource()).stop(); // 停止计时器
                }
            }
        });

        totalTimer.start();
        fileTimer.start();
    }


    private String formatSpeed(double bytesPerSecond) {
        return formatBytes(bytesPerSecond) + "B/s";
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
            return decimalFormat.format(bytes) + "B";
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
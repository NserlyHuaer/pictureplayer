package Tools.ImageManager;

import NComponent.PaintPicture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/**
 * 本类用于处理图像模糊（算法实现：高斯模糊）
 * 本类支持传入BufferedImage或图片路径
 * 1.若传入BufferedImage则默认一直使用这个类除非改变（一直占用内存除非更改图片）
 * 2.若传入图片路径则读取完图片并处理之后自动释放此文件（减少内存占用）
 */
public class MultiThreadBlur {
    private static final Logger log = LoggerFactory.getLogger(MultiThreadBlur.class);
    private final int TILE_SIZE = 64; // 缓存行友好的分块大小
    private final int[] RED_TABLE = new int[0x1000000];
    private final int[] GREEN_TABLE = new int[0x1000000];
    private final int[] BLUE_TABLE = new int[0x1000000];

    private BufferedImage src;

    private String srcPath;

    private BufferedImage dest;

    private int width;
    private int height;

    private int[] srcPixels;

    private int[] destPixels;

    private int[] tempPixels;

    // 动态计算kernelSize（若抗锯齿能修复则不返回1（不需要模糊））
    public static int calculateKernelSize(int width, int height, double scaleFactor) {
        //此代码块目前未实现
        if (1.75 * scaleFactor < PaintPicture.paintPicture.sizeOperate.getPictureOptimalSize()) return 15;
        if (scaleFactor > 1300) return 3;
        return 1;
    }

    public MultiThreadBlur(String srcPath) {
        this.srcPath = srcPath;
        for (int i = 0; i < 0x1000000; i++) {
            RED_TABLE[i] = (i >> 16) & 0xFF;
            GREEN_TABLE[i] = (i >> 8) & 0xFF;
            BLUE_TABLE[i] = i & 0xFF;
        }
        changeImage(srcPath);

    }

    public MultiThreadBlur(BufferedImage src) {
        // 初始化颜色分量查表
        for (int i = 0; i < 0x1000000; i++) {
            RED_TABLE[i] = (i >> 16) & 0xFF;
            GREEN_TABLE[i] = (i >> 8) & 0xFF;
            BLUE_TABLE[i] = i & 0xFF;
        }
        changeImage(src);
    }

    public void flushSrc() {
        if (src != null) src.flush();
    }

    public BufferedImage getSrc() {
        return src;
    }

    public void changeImage(BufferedImage src) {
        srcPath = null;
        this.src = src;
        width = src.getWidth();
        height = src.getHeight();
        srcPixels = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
        destPixels = new int[srcPixels.length];
        // 分离滤波处理
        tempPixels = new int[srcPixels.length];
    }

    public void changeImage(String srcPath) {
        src = getImageAndCastToTYPE_INT_RGB(srcPath);
        width = src.getWidth();
        height = src.getHeight();
        srcPixels = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
        destPixels = new int[srcPixels.length];
        // 分离滤波处理
        tempPixels = new int[srcPixels.length];
        src.flush();
        src = null;
    }

    public static BufferedImage getImageAndCastToTYPE_INT_RGB(String srcPath) {
        BufferedImage src = null;
        try {
            BufferedImage SimpleImage = ImageIO.read(new File(srcPath));
            src = GetImageInformation.CastToTYPE_INT_RGB(SimpleImage);
            SimpleImage.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
        return src;
    }

    public int calculateKernelSize(double scaleFactor) {
        return calculateKernelSize(width, height, scaleFactor);
    }


    public BufferedImage applyOptimizedBlur(int kernelSize) {
        if (srcPath != null) src = getImageAndCastToTYPE_INT_RGB(srcPath);
        if (src != null) {
            if (kernelSize % 2 == 0) throw new IllegalArgumentException("Kernel size must be odd");
            // 分离滤波处理
            horizontalBlur(srcPixels, tempPixels, width, height, kernelSize);
            verticalBlur(tempPixels, destPixels, width, height, kernelSize);

            dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            System.arraycopy(destPixels, 0, ((DataBufferInt) dest.getRaster().getDataBuffer()).getData(), 0, destPixels.length);
            if (srcPath != null) {
                src.flush();
                src = null;
            }
            return dest;
        }
        return null;
    }

    // 水平方向模糊
    private void horizontalBlur(int[] src, int[] dest, int width, int height, int kernelSize) {
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        try {
            pool.invoke(new BlurTask(src, dest, width, height, kernelSize, 0, height, true));
        } finally {
            pool.shutdown();
        }
    }

    // 垂直方向模糊
    private void verticalBlur(int[] src, int[] dest, int width, int height, int kernelSize) {
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        try {
            pool.invoke(new BlurTask(src, dest, width, height, kernelSize, 0, width, false));
        } finally {
            pool.shutdown();
        }
    }

    // 分块处理任务
    private class BlurTask extends RecursiveAction {
        private static final int THRESHOLD = 128; // 最小处理单位
        private final int[] src, dest;
        private final int width, height, kernelSize, start, end;
        private final boolean isHorizontal;

        BlurTask(int[] src, int[] dest, int width, int height, int kernelSize, int start, int end, boolean isHorizontal) {
            this.src = src;
            this.dest = dest;
            this.width = width;
            this.height = height;
            this.kernelSize = kernelSize;
            this.start = start;
            this.end = end;
            this.isHorizontal = isHorizontal;
        }

        @Override
        protected void compute() {
            if (end - start <= THRESHOLD) {
                processDirectly();
            } else {
                int mid = (start + end) >>> 1;
                invokeAll(new BlurTask(src, dest, width, height, kernelSize, start, mid, isHorizontal), new BlurTask(src, dest, width, height, kernelSize, mid, end, isHorizontal));
            }
        }

        private void processDirectly() {
            final int halfKernel = kernelSize / 2;
            final int bound = isHorizontal ? width : height;
            final int cacheSize = kernelSize * 2;
            int[][] cache = new int[cacheSize][];

            for (int i = start; i < end; i++) {
                // 初始化行缓存
                if (isHorizontal) {
                    cache[0] = new int[width];
                    System.arraycopy(src, i * width, cache[0], 0, width);
                } else {
                    cache[0] = new int[height];
                    for (int y = 0; y < height; y++) {
                        cache[0][y] = src[y * width + i];
                    }
                }

                // 滑动窗口处理
                for (int j = 0; j < bound; j++) {
                    int sumR = 0, sumG = 0, sumB = 0;
                    int count = 0;

                    int startPos = Math.max(j - halfKernel, 0);
                    int endPos = Math.min(j + halfKernel, bound - 1);

                    for (int k = startPos; k <= endPos; k++) {
                        int pixel = isHorizontal ? cache[0][k] : src[k * width + i];
                        sumR += RED_TABLE[pixel];
                        sumG += GREEN_TABLE[pixel];
                        sumB += BLUE_TABLE[pixel];
                        count++;
                    }

                    int avgR = sumR / count;
                    int avgG = sumG / count;
                    int avgB = sumB / count;
                    int result = (avgR << 16) | (avgG << 8) | avgB;

                    if (isHorizontal) {
                        dest[i * width + j] = result;
                    } else {
                        dest[j * width + i] = result;
                    }
                }
            }
        }
    }

    // 辅助方法：等待线程池终止
    private void awaitPoolTermination(ForkJoinPool pool) {
        try {
            if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("Thread pool did not terminate in time");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
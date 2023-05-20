package robot.crawler.anti;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public abstract class AntiCaptcha {

    private static final Logger log = LoggerFactory.getLogger(AntiCaptcha.class);

    private static boolean debug;

    private static final AtomicBoolean INITED = new AtomicBoolean(false);

    // https://www.garykessler.net/library/file_sigs.html
    private static final Function<byte[], String> IMG_FORMAT = (content) -> {
        if (content[0] ==  (byte) 0xFF && content[1] ==  (byte) 0xD8) {
            return ".jpg";
        } else if (content[0] ==  (byte) 0x42 && content[1] ==  (byte) 0x4D) {
            return ".bmp";
        } else if (content[0] ==  (byte) 0x89 && content[1] ==  (byte) 0x50 && content[2] ==  (byte) 0x4E && content[3] ==  (byte) 0x47
                && content[4] ==  (byte) 0x0D && content[5] ==  (byte) 0x0A && content[6] ==  (byte) 0x1A && content[7] ==  (byte) 0x0A) {
            return ".png";
        } else if(content[0] ==  (byte) 0x52 && content[1] ==  (byte) 0x49 && content[2] ==  (byte) 0x46 && content[3] ==  (byte) 0x46
                && content[8] ==  (byte) 0x57 && content[9] ==  (byte) 0x45 && content[10] ==  (byte) 0x42 && content[11] ==  (byte) 0x50) {
            return ".webp";
        } else {
            return null;
        }
    };

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            INITED.compareAndSet(false, true);
        } catch (UnsatisfiedLinkError e) { // SecurityException ignore?
            log.warn("opencv library should export to environment PATH, try openpnp way\n {}", e.getMessage());
            // openpnp support
            try {
                Class klazz = Class.forName("nu.pattern.OpenCV");
                Method loadJni = klazz.getDeclaredMethod("loadShared", new Class[0]); // 高版本JDK会自动切换到loadLocally
                loadJni.setAccessible(true);
                loadJni.invoke(klazz, new Object[0]);
            } catch (Exception openpnp) {
                log.error(openpnp.getMessage(), openpnp);
            }
        }
    }

    public static void setDebug(boolean debug) {
        AntiCaptcha.debug = debug;
    }

    public static org.openqa.selenium.Point edgeOffset(byte[] slideToDragImg) {
        MatOfByte slideToDrag = new MatOfByte(slideToDragImg);
        Mat slide = Imgcodecs.imdecode(slideToDrag, Imgcodecs.IMREAD_GRAYSCALE);
        Mat tmp = new Mat();
        // 二值化要求图像为8bit，可以读取时使用灰度，或转成灰度
        Imgproc.threshold(slide, tmp, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
        List<MatOfPoint> list = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(tmp, list, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        log.debug("检测出矩形数量：{}", list.size());
        for (MatOfPoint mp : list) {
            double v = Imgproc.contourArea(mp);
            if (v > 10000) { // XXX 图形大小多少合适
                Rect rect = Imgproc.boundingRect(mp);
                log.debug("滑块图片高[{}]宽[{}]，滑块图形高[{}]宽[{}]，边距:{}", slide.height(), slide.width(), rect.height, rect.width, rect.x);
                return new org.openqa.selenium.Point(slide.width() - rect.x - rect.width, slide.height() - rect.y - rect.height);
            }
        }
        log.info("未找到合适的矩形");
        return new org.openqa.selenium.Point(0, 0);
    }

    public static void passThroughSlideCaptcha(WebDriver webDriver, byte[] slideToDragImg, byte[] backgroundImg, double scale,
                                               int locXOffset) {
        passThroughSlideCaptcha(webDriver, slideToDragImg, backgroundImg, scale, locXOffset, 0,
                10, 100, 1,
                -5, 5, Integer.MAX_VALUE,
                5, 0.75, 300);
    }

    /**
     * <uL>
     *     <li>人操作滑动到指定位置需要时间（基本上1秒以上）</li>
     *     <li>人操作时不会绝对水平，存在些许垂直位移</li>
     *     <li>人操作时会先快速滑动到接近位置，再慢慢滑动使图像重合</li>
     *     <li>人操作过程可能存在滑过头再往回现象</li>
     * </uL>
     * @param webDriver 浏览器驱动
     * @param slideToDragImg 可滑动的小图
     * @param backgroundImg 背景图
     * @param scale 图形缩放倍数
     * @param locXOffset 小图基于匹配位置仍存在的水平偏移
     * @param locYOffset 小图基于匹配位置仍存在的垂直偏移
     * @param minXMovement 水平最小位移
     * @param maxXMovement 水平最大位移
     * @param xDeviation 水平移动距离与匹配距离容忍的误差
     * @param minYMovement  垂直最小位移
     * @param maxYMovement  垂直最大位移
     * @param yDeviation  垂直移动距离与匹配距离容忍的误差
     * @param cooldown 每次移动停顿时间
     * @param speedBump 移动到此比例减速（移动距离减少，停顿时间增加）
     * @param thinkTime 减速后的每次停顿时间
     */
    public static void passThroughSlideCaptcha(WebDriver webDriver, byte[] slideToDragImg, byte[] backgroundImg, double scale,
                                               int locXOffset, int locYOffset,
                                               int minXMovement, int maxXMovement, int xDeviation,
                                               int minYMovement, int maxYMovement, int yDeviation,
                                               int cooldown, double speedBump, int thinkTime) {
        if (xDeviation < 1 || yDeviation < 1 || scale < 0) {
            throw new IllegalArgumentException("误差至少1像素，缩放不能为负");
        }
        if (!INITED.get()) {
            return;
        }
        Point point = calculateDistance(slideToDragImg, backgroundImg, locXOffset, locYOffset);
        double xOffset = (point.x + locXOffset) * scale, yOffset = (point.y + locYOffset) * scale;
        int deltaX = 0, deltaY = 0, times = 0;
        while (Math.abs(deltaX - xOffset) > xDeviation || Math.abs(deltaY - yOffset) > yDeviation) {
            int xMovement = ThreadLocalRandom.current().nextInt(minXMovement, maxXMovement);
            if (Math.abs(xOffset) > 1 && deltaX > xOffset) { // 小于1则视为无需移动，滑过头则往回滑
                xMovement = -xMovement;
            }
            int yMovement = ThreadLocalRandom.current().nextInt(minYMovement, maxYMovement);
            if (Math.abs(yOffset) > 1 && deltaY > yOffset) {
                yMovement = - yMovement;
            }
            if (times >= 3) { // 多次后强制滑动到位
                if (Math.abs(xOffset) > 1 && Math.abs(deltaX -xOffset) < maxXMovement) {
                    xMovement = (int) (xOffset - deltaX);
                    yMovement = Math.abs(yOffset) < 1 ? (int) (yOffset - deltaY) : yMovement;
                }
                if (Math.abs(yOffset) > 1 && Math.abs(deltaY - yOffset) < maxYMovement) {
                    yMovement = (int) (yOffset - deltaY);
                    xMovement = Math.abs(xOffset) < 1 ? (int) (xOffset - deltaX) : xMovement;
                }
            }

            new Actions(webDriver).moveByOffset(xMovement, yMovement).perform();

            deltaX += xMovement;
            deltaY += yMovement;

            if (deltaX / xOffset >= speedBump) { // 减速
                maxXMovement = (int) Math.max(Math.min(Math.abs(deltaX - xOffset) / 2, maxXMovement), minXMovement + 1);
                maxYMovement = (int) Math.max(Math.min(Math.abs(deltaY - yOffset) / 2, maxYMovement), minYMovement + 1);
                sleep(thinkTime * Math.sqrt(times));
            } else {
                sleep(cooldown * Math.sqrt(times));
            }
            times++;
        }
    }

    private static void sleep(double time) {
        try {
            Thread.sleep((int) time);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /* test */ static Point calculateDistance(byte[] slideToDragImg, byte[] backgroundImg, int xOffset, int yOffset) {
        MatOfByte slideToDrag = new MatOfByte(slideToDragImg);
        MatOfByte backgroundToMatch = new MatOfByte(backgroundImg);
        Mat slide = Imgcodecs.imdecode(slideToDrag, Imgcodecs.IMREAD_COLOR);
        log.debug("slide width={}, height={}", slide.width(), slide.height());
        Mat background = Imgcodecs.imdecode(backgroundToMatch, Imgcodecs.IMREAD_COLOR);
        log.debug("background width={}, height={}", background.width(), background.height());

        Imgproc.GaussianBlur(slide, slide, new Size(9, 9), 0, 0, Core.BORDER_DEFAULT);
        Mat dragTmp = new Mat();
        Imgproc.Canny(slide, dragTmp, 100, 100);

        Imgproc.GaussianBlur(background, background, new Size(9, 9), 0, 0, Core.BORDER_DEFAULT);
        Mat bgTmp = new Mat();
        Imgproc.Canny(background, bgTmp, 100, 200);

        Mat tmp = new Mat();
        Imgproc.matchTemplate(bgTmp, dragTmp, tmp, Imgproc.TM_CCORR_NORMED); // TM_CCORR_NORMED, TM_CCOEFF_NORMED效果较好
        Core.normalize(tmp, tmp, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Core.MinMaxLocResult result = Core.minMaxLoc(tmp);
        log.info("max movement: {}, min movement:{}", result.maxLoc, result.minLoc);
        mask(background, slide, result.maxLoc, xOffset, yOffset, IMG_FORMAT.apply(slideToDragImg));
        return result.maxLoc;
    }

    private static void mask(Mat background, Mat slide, Point maxLoc, int xOffset, int yOffset, String imgFmt) {
        if (debug) {
            Mat write = background.clone();
            Point end = new Point(maxLoc.x + slide.cols(), maxLoc.y + slide.rows());
            Imgproc.rectangle(write, maxLoc, end, new Scalar(0, 0, 255), 2, Imgproc.LINE_8, 0);
//            HighGui.imshow("locate", write);
//            HighGui.waitKey();
            Imgproc.rectangle(write, new Point(maxLoc.x + xOffset, maxLoc.y + yOffset), new Point(end.x + xOffset, end.y + yOffset), new Scalar(0, 255, 0), 2, Imgproc.LINE_8, 0);
//            HighGui.imshow("slide", write);
//            HighGui.waitKey();
//            HighGui.destroyAllWindows();
            File tmpFile = null;
            try {
                tmpFile = File.createTempFile(UUID.randomUUID().toString(), imgFmt);
                Imgcodecs.imwrite(tmpFile.getAbsolutePath(), write);
                log.info("debug output match result: {}", tmpFile.getAbsolutePath());
            } catch (Exception ignore) {

            } finally {
                if (tmpFile != null) {
                    tmpFile.deleteOnExit();
                }
            }
        }
    }
}

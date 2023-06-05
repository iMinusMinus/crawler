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
            log.debug("loaded opencv library from java.library.path");
        } catch (UnsatisfiedLinkError e) { // SecurityException ignore?
            log.warn("opencv library should export to environment PATH, try openpnp way\n {}", e.getMessage());
            // openpnp support
            try {
                Class klazz = Class.forName("nu.pattern.OpenCV");
                Method loadJni = klazz.getDeclaredMethod("loadShared", new Class[0]); // 高版本JDK会自动切换到loadLocally
                loadJni.setAccessible(true);
                loadJni.invoke(klazz, new Object[0]);
                log.debug("loaded opencv library from openpnp unzip temporary directory");
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
            if (v > 8000 && v < slide.width() * slide.height()) { //  图形大小多少合适(dianping为8352)
                Rect rect = Imgproc.boundingRect(mp);
//                Imgproc.rectangle(tmp, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 2, Imgproc.LINE_8, 0);
//                HighGui.imshow("rect", tmp);
//                HighGui.waitKey();
                log.debug("滑块图片高[{}]宽[{}]，滑块图形高[{}]宽[{}]，边距:{}", slide.height(), slide.width(), rect.height, rect.width, rect.x);
                return new org.openqa.selenium.Point(rect.x, rect.y);
            }
        }
        log.info("未找到合适的矩形");
        return new org.openqa.selenium.Point(0, 0);
    }

    public static void passThroughSlideCaptcha(WebDriver webDriver, byte[] slideToDragImg, byte[] backgroundImg, double scale,
                                               int locXOffset) {
        passThroughSlideCaptcha(webDriver, slideToDragImg, backgroundImg, scale, locXOffset, 0,
                5, 1, 20, ThreadLocalRandom.current().nextInt(500, 2000), 0.75d, 0.2d,
                0, 5);
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
     * @param xDeviation 水平随机扰动距离
     * @param locYOffset 小图基于匹配位置仍存在的垂直偏移
     * @param yDeviation 垂直随机扰动
     * @param interval 间隔时间
     * @param slidingTime 滑动时间
     * @param speedBump 移动到此比例减速（移动距离减少，停顿时间增加）
     * @param speedUpTimeRate 加速时间占比
     * @param timeOffset 初始时间偏移（模拟思考时间）
     * @param timeDeviation 每个时间单位的时间扰动
     */
    public static void passThroughSlideCaptcha(WebDriver webDriver, byte[] slideToDragImg, byte[] backgroundImg, double scale,
                                               int locXOffset, int locYOffset, int xDeviation, int yDeviation,
                                               int interval, int slidingTime,
                                               double speedBump, double speedUpTimeRate, int timeOffset, int timeDeviation) {
        if (xDeviation < 1 || yDeviation < 1 || scale < 0) {
            throw new IllegalArgumentException("误差至少1像素，缩放不能为负");
        }
        if (!INITED.get()) {
            return;
        }
        MatOfByte slideToDrag = new MatOfByte(slideToDragImg);
        MatOfByte backgroundToMatch = new MatOfByte(backgroundImg);
        Mat slide = Imgcodecs.imdecode(slideToDrag, Imgcodecs.IMREAD_COLOR);
        Mat background = Imgcodecs.imdecode(backgroundToMatch, Imgcodecs.IMREAD_COLOR);
        org.openqa.selenium.Point point = calculateDistance(slide, background, locXOffset, locYOffset, IMG_FORMAT.apply(backgroundImg));
        org.openqa.selenium.Point target = new org.openqa.selenium.Point((int) (point.x * scale), (int) (point.y * scale));
        org.openqa.selenium.Point border = new org.openqa.selenium.Point((int) (background.width() * scale), (int) (background.height() * scale));
        int[][] tracks = generateTrack(target, interval, slidingTime, border,
                speedBump, speedUpTimeRate, timeOffset, timeDeviation, locXOffset, xDeviation, locYOffset, yDeviation);
        slideFollowTrack(webDriver, tracks, timeOffset);
    }

    public static void slideFollowTrack(WebDriver webDriver, int[][] tracks, int timeOffset) {
        long now = System.currentTimeMillis();
        for (int i =0; i < tracks.length; i++) {
            int[] track = tracks[i];
            int xMovement = i == 0 ? track[1] : track[1] - tracks[i - 1][1];
            int yMovement = i == 0 ? track[2] : track[2] - tracks[i - 1][2];
            new Actions(webDriver).moveByOffset(xMovement, yMovement).perform();
            long elapsed = System.currentTimeMillis() - now;
            long estimateElapsed = track[0] - timeOffset;
            // elapsed time should near estimate
            if (elapsed - estimateElapsed > 15) {
                log.warn("slide time exceed estimate: {}ms", elapsed - estimateElapsed);
            } else if (estimateElapsed - elapsed > 15) {
                sleep(estimateElapsed - elapsed);
            }
        }
    }

    private static void sleep(double time) {
        try {
            Thread.sleep((int) time);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static int[][] generateTrack(org.openqa.selenium.Point target, org.openqa.selenium.Point border,
                                        int xOffset, int yOffset) {
        return generateTrack(target, 20, ThreadLocalRandom.current().nextInt(500, 1500), border,
                0.75, 0.2, ThreadLocalRandom.current().nextInt(1000, 3000), 0, xOffset, 0, yOffset, 0);
    }

    /**
     * 生成滑动轨迹
     * @param target 目标点
     * @param interval 轨迹间隔时间（毫秒）
     * @param slidingTime 滑动时间（毫秒）
     * @param border 边界
     * @param speedBump 加速距离占比
     * @param speedUpCostTimeRate 加速时间占比
     * @param coldStartTime 时间整体偏移
     * @param timeDeviation 每次时间扰动（毫秒）
     * @param xOffset 初始水平偏移
     * @param xDeviation 每次x轴扰动
     * @param yOffset 初始垂直偏移
     * @param yDeviation 每次y轴扰动
     * @return [[timeOffset, x, y], ...]
     */
    public static int[][] generateTrack(org.openqa.selenium.Point target, int interval, int slidingTime, org.openqa.selenium.Point border,
                                        double speedBump, double speedUpCostTimeRate,
                                        int coldStartTime, int timeDeviation,
                                        int xOffset, int xDeviation,
                                        int yOffset, int yDeviation) {
        assert timeDeviation < interval;
        assert speedBump > 0 && speedBump < 1;
        assert speedUpCostTimeRate > 0 && speedUpCostTimeRate < 1;
        int steps = slidingTime / interval;
        int[][] track = new int[steps][3];
        boolean overhead = ThreadLocalRandom.current().nextBoolean();
        double exceedRate = Math.min(1.15d, (double) border.x / (double) target.x); // 不能超出背景图边界
        double targetX = overhead ? target.x * ThreadLocalRandom.current().nextDouble(1d, exceedRate) : target.x;
        int speedUpDistance = (int) (targetX * speedBump);
        int fastSteps = (int) (steps * speedUpCostTimeRate);
        int speedUpTime = (int) (slidingTime * speedUpCostTimeRate);
        double a = speedUpDistance * 2d/ (speedUpTime * speedUpTime);
        int timeUnit = speedUpTime/ fastSteps;
        log.debug("time offset: {}, expect speed up distance: {}, expect speed up cost time: {}", coldStartTime, speedUpDistance, speedUpTime);
        for (int i = 0; i < fastSteps; i++) {
            int time = timeDeviation <= 0 ? 0 : ThreadLocalRandom.current().nextInt(0, timeDeviation);
            track[i][0] = time + timeUnit + ( i == 0 ? coldStartTime : track[i - 1][0]);
            int x = xDeviation <= 0 ? 0 : ThreadLocalRandom.current().nextInt(0, xDeviation);
            track[i][1] = xOffset + (int) (a * (track[i][0] - coldStartTime) * (track[i][0] - coldStartTime) / 2) + x;
            int y = yDeviation <= 0 ? 0 : ThreadLocalRandom.current().nextInt(0, yDeviation);
            track[i][2] = y + ( i == 0 ? yOffset : track[i - 1][2]);
        }
        if (fastSteps == steps) {
            return track;
        }
        log.debug("fast move '{}' step, pause at point: {}", fastSteps, track[fastSteps - 1]);
        boolean beyond = false;
        timeUnit = (slidingTime - speedUpTime) / (steps - fastSteps);
        int surplus = (int) (targetX - speedUpDistance + fastSteps - steps); // 假设每个间隔时间最小1单位距离时，多出的距离再分配
        log.debug("expect slow park step: {}, expect park point: {}", steps - fastSteps, target);
        for (int i = fastSteps; i < steps; i++) {
            // 逐渐减小随机干扰
            timeDeviation = timeDeviation / (fastSteps + i);
            xDeviation = Math.min(surplus / 2, track[i - 1][1] - track[i - 2][1]); // 不大于上一步距离，不大于待分配一半
            yDeviation = yDeviation / (fastSteps + i);

            int lowBound = surplus / (steps - fastSteps) + xDeviation / 2; // 越靠前距离越大

            int time = timeDeviation <= 0 ? 0 : ThreadLocalRandom.current().nextInt(0, timeDeviation);
            track[i][0] = track[i - 1][0] + timeUnit + time;
            int x = xDeviation <= lowBound
                    ? (xDeviation > 1 ? ThreadLocalRandom.current().nextInt(xDeviation / 2, xDeviation) : xDeviation)
                    : ThreadLocalRandom.current().nextInt(lowBound, xDeviation); // 不小于平均再分配，保证能分配完
            surplus -= x;
            int distance = (i == fastSteps ? speedUpDistance : track[i - 1][1]) + 1 + x;
            track[i][1] = beyond ? (int) (2 * targetX - distance) : Math.min(distance, border.x); // 滑超返回
            if (track[i][1] >= (int) targetX) {
                beyond = true;
            }
            int y = yDeviation <= 0 ? 0 : ThreadLocalRandom.current().nextInt(0, yDeviation);
            track[i][2] = y + track[i - 1][2];
        }
        log.debug("expect time: {}, expect target: {}, last track point: {}", slidingTime, target, track[track.length - 1]);
        return track;
    }

    /* test */static org.openqa.selenium.Point calculateDistance(byte[] slideToDragImg, byte[] backgroundImg, int xOffset, int yOffset) {
        MatOfByte slideToDrag = new MatOfByte(slideToDragImg);
        MatOfByte backgroundToMatch = new MatOfByte(backgroundImg);
        Mat slide = Imgcodecs.imdecode(slideToDrag, Imgcodecs.IMREAD_COLOR);
        Mat background = Imgcodecs.imdecode(backgroundToMatch, Imgcodecs.IMREAD_COLOR);
        return calculateDistance(slide, background, xOffset, yOffset, IMG_FORMAT.apply(slideToDragImg));
    }

     static org.openqa.selenium.Point calculateDistance(Mat slide, Mat background, int xOffset, int yOffset, String format) {
        log.debug("slide width={}, height={}. background width={}, height={}", slide.width(), slide.height(), background.width(), background.height());

        Imgproc.GaussianBlur(slide, slide, new Size(9, 9), 0, 0, Core.BORDER_DEFAULT);
        Mat dragTmp = new Mat();
        Imgproc.Canny(slide, dragTmp, 150, 300);

        // ksize越小细节越丰富，大于21时可能丢失所有细节
        Imgproc.GaussianBlur(background, background, new Size(9, 9), 0, 0, Core.BORDER_DEFAULT);
        Mat bgTmp = new Mat();
        Imgproc.Canny(background, bgTmp, 50, 100, 3); // threshold2太大会导致边界非常少
//        HighGui.imshow("GaussianBlur_Canny_bg", bgTmp);
//        HighGui.waitKey(0);

        Mat tmp = new Mat();
        Imgproc.matchTemplate(bgTmp, dragTmp, tmp, Imgproc.TM_CCORR_NORMED); // TM_CCORR_NORMED, TM_CCOEFF_NORMED效果较好
        Core.normalize(tmp, tmp, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Core.MinMaxLocResult result = Core.minMaxLoc(tmp);
        log.info("max movement: {}, min movement:{}", result.maxLoc, result.minLoc);
        mask(background, slide, result.maxLoc, xOffset, yOffset, format);
        return new org.openqa.selenium.Point((int) result.maxLoc.x, (int) result.maxLoc.y);
    }

    private static void mask(Mat background, Mat slide, Point maxLoc, int xOffset, int yOffset, String imgFmt) {
        if (debug) {
            Mat write = background.clone();
            Point end = new Point(maxLoc.x + slide.cols(), maxLoc.y + slide.rows());
            Imgproc.rectangle(write, maxLoc, end, new Scalar(0, 0, 255), 2, Imgproc.LINE_8, 0);
//            HighGui.imshow("rectangle_locate", write);
//            HighGui.waitKey(0);
            Imgproc.rectangle(write, new Point(maxLoc.x + xOffset, maxLoc.y + yOffset), new Point(end.x + xOffset, end.y + yOffset), new Scalar(0, 255, 0), 2, Imgproc.LINE_8, 0);
//            HighGui.imshow("rectangle_slide", write);
//            HighGui.waitKey(0);
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

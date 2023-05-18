package robot.crawler.anti;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AntiCaptcha {

    private static final AtomicBoolean INITED = new AtomicBoolean(false);

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            INITED.compareAndSet(false, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void passThroughSlideCaptcha(WebDriver webDriver, byte[] slideToDragImg, byte[] backgroundImg,
                                               int locXOffset) {
        passThroughSlideCaptcha(webDriver, slideToDragImg, backgroundImg, locXOffset, 0,
                10, 100, 0,
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
    public static void passThroughSlideCaptcha(WebDriver webDriver, byte[] slideToDragImg, byte[] backgroundImg,
                                               int locXOffset, int locYOffset,
                                               int minXMovement, int maxXMovement, int xDeviation,
                                               int minYMovement, int maxYMovement, int yDeviation,
                                               int cooldown, double speedBump, int thinkTime) {
        if (!INITED.get()) {
            return;
        }
        Point point = calculateDistance(slideToDragImg, backgroundImg);
        double xOffset = point.x + locXOffset, yOffset = point.y + locYOffset;
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
                sleep(thinkTime);
            } else {
                sleep(cooldown);
            }
            times++;
        }
    }

    private static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Point calculateDistance(byte[] slideToDragImg, byte[] backgroundImg) {
        MatOfByte slideToDrag = new MatOfByte(slideToDragImg);
        MatOfByte backgroundToMatch = new MatOfByte(backgroundImg);
        Mat slide = Imgcodecs.imdecode(slideToDrag, Imgcodecs.IMREAD_COLOR);
        Mat background = Imgcodecs.imdecode(backgroundToMatch, Imgcodecs.IMREAD_COLOR);

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
        return result.maxLoc;
    }
}

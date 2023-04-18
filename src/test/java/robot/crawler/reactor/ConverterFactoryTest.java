package robot.crawler.reactor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ConverterFactoryTest {

    @Test
    public void testCity() {
        String subIndustry = (String) ConverterFactory.getConverter("arg.split(\"/\")[3]").convert("https://www.dianping.com/anshun/ch0");
        Assertions.assertEquals("anshun", subIndustry);
    }

    @Test
    @Disabled // FIXME 正则在其他地方正常匹配，ide中不正确
    public void testStar() {
        String arg = "star star_35 star_sml";
        String star = (String) ConverterFactory.getConverter("new java.math.BigDecimal(java.util.regex.Pattern.compile(\"star_(?<score>\\\\d+)\").matcher(arg).group(\"score\")).divide(java.math.BigDecimal.TEN, 1, java.math.RoundingMode.HALF_UP).toString()").convert(arg);
        Assertions.assertEquals("3.5", star);
    }

    @Test
    public void testCommentScore() {
        String arg = "sml-rank-stars sml-str50";
        String score = (String) ConverterFactory.getConverter("new java.math.BigDecimal(arg.split(\" \")[1].substring(7)).divide(java.math.BigDecimal.TEN, 1, java.math.RoundingMode.HALF_UP).toString()").convert(arg);
        Assertions.assertEquals("5.0", score);
    }

    @Test
    public void testSubIndustry() {
        String subIndustry = (String) ConverterFactory.getConverter("arg.split(\"/\")[4]").convert("https://www.dianping.com/wuhan/ch10/g116");
        Assertions.assertEquals("ch10", subIndustry);
    }

    @Test
    public void testCategory() {
        String subIndustry = (String) ConverterFactory.getConverter("arg.substring(arg.lastIndexOf(\"/\") + 1)").convert("https://www.dianping.com/wuhan/ch10/g116");
        Assertions.assertEquals("g116", subIndustry);
    }

    @Test
    public void testArea() {
        String quarter = (String) ConverterFactory.getConverter("arg.substring(arg.lastIndexOf(\"/\") + 1)").convert("https://www.dianping.com/wuhan/ch10/r8181");
        Assertions.assertEquals("r8181", quarter);
    }

    @Test
    public void testScore() {
        String score = (String) ConverterFactory.getConverter("arg.split(\"：\")[1]").convert("环境：4.8");
        Assertions.assertEquals("4.8", score);
    }

    @Test
    public void testTel() {
        String tels = (String) ConverterFactory.getConverter("\"电话： 无 添加\".equals(arg) ? null : arg.split(\"：\")[1].trim()").convert("电话：027-87377077 17362976059");
        Assertions.assertEquals("027-87377077 17362976059", tels);
    }

    @Test
    public void testReviewQuantity() {
        Integer quantity = (Integer) ConverterFactory.getConverter("Integer.parseInt(arg.substring(1, arg.length() - 1))").convert("(110)");
        Assertions.assertEquals(110, quantity);
    }

    @Test
    public void testAddress() {
        String arg = "\\n        花园路洪楼印象城聚隆广场3号楼2705(琥珀安泊酒店楼上)\\n";
        String addr = (String) ConverterFactory.getConverter("arg.translateEscapes().trim()").convert(arg);
        Assertions.assertEquals("花园路洪楼印象城聚隆广场3号楼2705(琥珀安泊酒店楼上)", addr);
    }
}

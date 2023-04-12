package robot.crawler.spec;

import java.util.List;

public record TaskDefinition(String id, String name, String url, String version,
                             TaskSettingDefinition settings,
                             List<Step> steps) {
}

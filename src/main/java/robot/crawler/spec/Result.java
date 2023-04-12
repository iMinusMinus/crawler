package robot.crawler.spec;

import java.util.List;
import java.util.Map;

public record Result(String taskId, List<Map<String, Object>> data) {
}

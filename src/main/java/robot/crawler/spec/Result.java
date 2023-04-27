package robot.crawler.spec;

import java.util.List;
import java.util.Map;

public record Result(String taskId, long enterAt, long leaveAt,
                     List<Map<String, Object>> data, boolean corrupt, String currentUrl) {
}

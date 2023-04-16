package robot.crawler.spec;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record Result(String taskId, LocalDateTime enterAt, LocalDateTime leaveAt,
                     List<Map<String, Object>> data, boolean corrupt) {
}

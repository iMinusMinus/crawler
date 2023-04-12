package robot.crawler.spec;

import java.time.LocalDateTime;

public record Progress(String taskId, String status, String executorId, LocalDateTime timestamp, int fetched) {
}

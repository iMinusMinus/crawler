package robot.crawler.spec;

public record Progress(String taskId, String status, String executorId, long epoch, int fetched) {
}

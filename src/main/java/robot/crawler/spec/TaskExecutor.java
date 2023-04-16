package robot.crawler.spec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TaskExecutor {

    Logger log = LoggerFactory.getLogger(TaskExecutor.class);

    void setUp(TaskSettingDefinition settings);

    default Result execute(TaskDefinition task) {
        LocalDateTime enterAt = LocalDateTime.now();
        try {
            setUp(task.settings());
            List<Map<String, Object>> data = doExecute(task.url(), task.steps());
            return new Result(task.id(), enterAt, LocalDateTime.now(), data, false);
        } catch (Exception e) {
            return new Result(task.id(), enterAt, LocalDateTime.now(), doHandleException(e), true);
        } finally {
            tearDown();
        }
    }

    List<Map<String, Object>> doExecute(String url, List<? extends Step> steps);

    default List<Map<String, Object>> doHandleException(Exception e) {
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);
    }

    void tearDown();
}

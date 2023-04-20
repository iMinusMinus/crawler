package robot.crawler.spec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public interface TaskExecutor {

    Logger log = LoggerFactory.getLogger(TaskExecutor.class);

    void setUp(TaskSettingDefinition settings);

    default Result execute(TaskDefinition task) {
        long enterAt = System.currentTimeMillis();
        try {
            setUp(task.settings());
            List<Map<String, Object>> data = doExecute(task.url(), task.steps());
            return new Result(task.id(), enterAt, System.currentTimeMillis(), data, false);
        } catch (RuntimeException e) {
            return new Result(task.id(), enterAt, System.currentTimeMillis(), doHandleException(e), true);
        } finally {
            tearDown();
        }
    }

    List<Map<String, Object>> doExecute(String url, List<? extends Step> steps);

    default List<Map<String, Object>> doHandleException(RuntimeException e) {
        log.error(e.getMessage(), e);
        throw e;
    }

    void tearDown();
}

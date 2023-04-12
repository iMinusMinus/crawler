package robot.crawler.reactor;

import robot.crawler.spec.TaskExecutor;

public abstract class TaskExecutorFactory {

    public static TaskExecutor getTaskExecutor(String executorType) {
        assert executorType != null;
        TaskExecutor executor;
        switch (executorType) {
            case "jsoup" -> executor = new JsoupTaskExecutor();
            case "webdriver" -> executor = new WebDriverTaskExecutor();
            default -> throw new IllegalArgumentException("unknown executor type: " + executorType);
        }
        return executor;
    }
}

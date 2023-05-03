package robot.crawler.spec;

import java.util.Map;

public record AndroidEmulator(String packageName, String activityName, String serial,
                              Boolean attachToRunningApp, Map<String, Object> options) {
}

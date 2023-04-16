package robot.crawler.reactor;

import org.codehaus.janino.ScriptEvaluator;
import org.openqa.selenium.WebDriver;

public abstract class WebDriverStepHookExecutor {

    private static final String WEB_DRIVER_PARAMETER_NAME = "webDriver";

    private static final String CONTEXT_PARAMETER_NAME = "context";

    private static final int SOURCE_VERSION = 8;

    private static final int TARGET_VERSION = 8;

    public static Object execute(WebDriver webDriver, WebDriverContext context, String expression) {
        try {
            ScriptEvaluator evaluator = new ScriptEvaluator();
            evaluator.setSourceVersion(SOURCE_VERSION);
            evaluator.setTargetVersion(TARGET_VERSION);
            evaluator.setParameters(new String[]{WEB_DRIVER_PARAMETER_NAME, CONTEXT_PARAMETER_NAME}, new Class[]{WebDriver.class, WebDriverContext.class});
            evaluator.setReturnType(Object.class);
            evaluator.cook(expression);
            return evaluator.evaluate(webDriver, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

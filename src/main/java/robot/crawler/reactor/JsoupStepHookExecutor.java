package robot.crawler.reactor;

import org.codehaus.janino.ScriptEvaluator;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public abstract class JsoupStepHookExecutor {

    private static final String CONNECTION_PARAMETER_NAME = "connection";

    private static final String CONTEXT_PARAMETER_NAME = "context";

    private static final int SOURCE_VERSION = 8;

    private static final int TARGET_VERSION = 8;

    public static Object execute(Connection connection, Context<Element> context, String expression) {
        try {
            ScriptEvaluator evaluator = new ScriptEvaluator();
            evaluator.setSourceVersion(SOURCE_VERSION);
            evaluator.setTargetVersion(TARGET_VERSION);
            evaluator.setParameters(new String[]{CONNECTION_PARAMETER_NAME, CONTEXT_PARAMETER_NAME}, new Class[]{Connection.class, Context.class});
            evaluator.setReturnType(Object.class);
            evaluator.cook(expression);
            return evaluator.evaluate(connection, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object execute(Connection connection, Document doc, Context context, String expression) {
        Puppet webDriver = new Puppet(connection, doc);
        return WebDriverStepHookExecutor.execute(webDriver, context, expression);
    }
}

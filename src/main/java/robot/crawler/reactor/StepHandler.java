package robot.crawler.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import robot.crawler.spec.Step;

public interface StepHandler<CTX extends Context<E>, STEP extends Step, E> {

    Logger log = LoggerFactory.getLogger(StepHandler.class);

    default void execute(CTX context, STEP step) {
        try {
            if (!beforeHandle(context, step)) {
                return;
            }
            handle(context, step);
            afterHandle(context, step);
        } catch (RuntimeException e) {
            onThrow(context, step, e);
        }
    }

    /**
     * 步骤处理前的动作
     * @param context 执行上下文
     * @param step 步骤定义
     * @return 是否按步骤处理，false则不执行本步骤
     */
    default boolean beforeHandle(CTX context, STEP step) {
        return true;
    }

    /**
     * 执行定义的步骤
     * @param context 执行上下文
     * @param step 步骤定义
     */
    void handle(CTX context, STEP step);

    default boolean afterHandle(CTX context, STEP step) {
        return true;
    }

    default void onThrow(CTX context, STEP step, RuntimeException e) {
        log.error("handle step[name={}] error", step.name());
        throw e;
    }
}

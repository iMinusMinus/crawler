package robot.crawler.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import robot.crawler.spec.Step;

public interface StepHandler<CTX extends Context, STEP extends Step> {

    Logger log = LoggerFactory.getLogger(StepHandler.class);

    void handle(CTX context, STEP step);
}

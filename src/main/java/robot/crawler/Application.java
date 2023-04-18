package robot.crawler;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.TypeNameIdResolver;
import com.fasterxml.jackson.databind.type.SimpleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import robot.crawler.reactor.JsoupTaskExecutor;
import robot.crawler.reactor.Register;
import robot.crawler.reactor.TaskExecutorFactory;
import robot.crawler.reactor.WebDriverTaskExecutor;
import robot.crawler.spec.Action;
import robot.crawler.spec.Finder;
import robot.crawler.spec.Locator;
import robot.crawler.spec.Progress;
import robot.crawler.spec.Result;
import robot.crawler.spec.Step;
import robot.crawler.spec.TaskDefinition;
import robot.crawler.spec.TaskExecutor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.List;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final String FILE_PROTOCOL = "file://";

    private static final String HTTP_PROTOCOL = "http://";

    private static final String HTTPS_PROTOCOL = "https://";

    private static ObjectMapper om;

    public static class Args {

        @Parameter(names = {"-r", "--read"}, required = true, description = "read job definition from: file:// or http[s]://")
        private String taskSource;

        @Parameter(names = {"-e", "--executor"}, description = "job executor type: webdriver, jsoup")
        private String executorType = "webdriver";

        @Parameter(names = {"-f", "--feedback"}, description = "progress feedback to: console, http[s]://")
        private String feedback = "console";

        @Parameter(names = {"-w", "--write"}, required = true, description = "write to place: file:// or http[s]://")
        private String outputDestination;

        public String getTaskSource() {
            return taskSource;
        }

        public void setTaskSource(String taskSource) {
            this.taskSource = taskSource;
        }

        public String getExecutorType() {
            return executorType;
        }

        public void setExecutorType(String executorType) {
            this.executorType = executorType;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }

        public String getOutputDestination() {
            return outputDestination;
        }

        public void setOutputDestination(String outputDestination) {
            this.outputDestination = outputDestination;
        }
    }

    private static void configureObjectMapper() {
        om = new ObjectMapper();
        om.findAndRegisterModules();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Unexpected token (START_OBJECT), expected VALUE_STRING: need JSON String that contains type id (for subtype of java.util.List)
//        TypeNameIdResolver typeResolver = TypeNameIdResolver.construct(om.getDeserializationConfig(),
//                SimpleType.constructUnsafe(Step.class),
//                List.of(new NamedType(Locator.class, Step.Type.LOCATOR.getValue()),
//                        new NamedType(Action.class, Step.Type.ACTION.getValue()),
//                        new NamedType(Finder.class, Step.Type.FINDER.getValue())),
//                false, true);
//        TypeResolverBuilder polymorphic = ObjectMapper.DefaultTypeResolverBuilder
//                .construct(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, new LaissezFaireSubTypeValidator());
//        polymorphic.inclusion(JsonTypeInfo.As.EXISTING_PROPERTY)
//                .typeProperty("type")
//                .init(JsonTypeInfo.Id.NAME, typeResolver)
//                .typeIdVisibility(true);
//        om.setDefaultTyping(polymorphic);


        // Cannot construct instance of `robot.crawler.spec.Step` (no Creators, like default constructor, exist): abstract types either need to be mapped to concrete types, have custom deserializer, or contain additional type information
//        om.registerSubtypes(new NamedType(Locator.class, Step.Type.LOCATOR.getValue()),
//                new NamedType(Action.class, Step.Type.ACTION.getValue()),
//                new NamedType(Finder.class, Step.Type.FINDER.getValue()));

    }

    public static void main(String[] args) throws Exception {
        Args commandArgs = new Args();
        JCommander.newBuilder().addObject(commandArgs).build().parse(args);

        String taskSource = commandArgs.getTaskSource();
        String destination = commandArgs.getOutputDestination();
        String to = commandArgs.getFeedback();

        configureObjectMapper();

        Register.initialize();

        TaskDefinition task = pollTask(taskSource);
        if (task == null) {
            log.warn("no task to executor when polling {}", taskSource);
            return;
        }
        TaskExecutor taskExecutor = TaskExecutorFactory.getTaskExecutor(commandArgs.getExecutorType());

        feedback(to, new Progress(task.id(), "ACCEPT", null, LocalDateTime.now(), 0));

        Result crawResult = taskExecutor.execute(task);

        feedback(to, new Progress(task.id(), "CRAW_FINISH", null, LocalDateTime.now(), crawResult.data().size()));

        pushResult(destination, crawResult);

        feedback(to, new Progress(task.id(), "UPLOADED", null, LocalDateTime.now(), crawResult.data().size()));

    }

    private static TaskDefinition pollTask(String source) throws Exception {
        if (source.startsWith(FILE_PROTOCOL)) {
            try (FileInputStream fis =new FileInputStream(source.substring(FILE_PROTOCOL.length()))) {
                return om.readValue(fis, TaskDefinition.class);
            }
        } else if (source.startsWith(HTTP_PROTOCOL) || source.startsWith(HTTPS_PROTOCOL)) {
            URLConnection connection = new URL(source).openConnection();
            connection.setReadTimeout(30);
            connection.setConnectTimeout(5);
            connection.setDoInput(true);
            connection.connect();
            return om.readValue(connection.getInputStream(), TaskDefinition.class);
        }
        return null;
    }

    private static void feedback(String to, Progress progress) throws Exception {
        if ("console".equals(to)) {
            log.info("task execute success!");
        } else {
            URLConnection connection = new URL(to).openConnection();
            connection.setReadTimeout(30);
            connection.setConnectTimeout(5);
            connection.setDoOutput(true);
            connection.getOutputStream().write(om.writeValueAsBytes(progress));
            connection.connect();
        }
    }

    private static void pushResult(String destination, Result crawResult) throws Exception {
        if (destination.startsWith(FILE_PROTOCOL)) {
            try (FileOutputStream fos = new FileOutputStream(destination.substring(FILE_PROTOCOL.length()))) {
                fos.write(om.writeValueAsBytes(crawResult));
            }
        } else if (destination.startsWith(HTTP_PROTOCOL) || destination.startsWith(HTTPS_PROTOCOL)) {
            URLConnection connection = new URL(destination).openConnection();
            connection.setReadTimeout(30);
            connection.setConnectTimeout(5);
            connection.setDoOutput(true);
            connection.getOutputStream().write(om.writeValueAsBytes(crawResult));
            connection.connect();
        }
    }

}

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final String FILE_PROTOCOL = "file://";

    private static final String HTTP_PROTOCOL = "http://";

    private static final String HTTPS_PROTOCOL = "https://";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String APPLICATION_JSON_VALUE = "application/json;charset=UTF-8";

    private static final String JSON_SUFFIX = ".json";

    private static final Set<String> PROCESSED = new HashSet<>();

    private static ObjectMapper om;

    private static HttpClient httpClient;

    public static class Args {

        @Parameter(names = {"-r", "--read"}, required = true, description = "read job definition from: file:// or http[s]://")
        private String taskSource;

        @Parameter(names = {"-e", "--executor"}, description = "job executor type: webdriver, jsoup")
        private String executorType = "webdriver";

        @Parameter(names = {"-f", "--feedback"}, description = "progress feedback to: console, http[s]://")
        private String feedback = "console";

        @Parameter(names = {"-w", "--write"}, required = true, description = "write to place: file:// or http[s]://")
        private String outputDestination;

        @Parameter(names = {"-t", "--times"}, description = "max fetch task times from remote server")
        private int fetchTaskMaxTimes = 1;

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

        public int getFetchTaskMaxTimes() {
            return fetchTaskMaxTimes;
        }

        public void setFetchTaskMaxTimes(int fetchTaskMaxTimes) {
            this.fetchTaskMaxTimes = fetchTaskMaxTimes;
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
        int maxTimes = commandArgs.getFetchTaskMaxTimes();

        String executorId = System.getProperty("user.name") + "@" + resolveHostName();

        configureObjectMapper();

        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

        Register.initialize();

        int times = 0;

        while(times < maxTimes) {
            try {
                TaskDefinition task = pollTask(taskSource);
                if (task == null) {
                    log.warn("no task to executor when polling {}", taskSource);
                } else {
                    TaskExecutor taskExecutor = TaskExecutorFactory.getTaskExecutor(commandArgs.getExecutorType());

                    feedback(to, new Progress(task.id(), "ACCEPT", executorId, System.currentTimeMillis(), 0));

                    Result crawResult = taskExecutor.execute(task);

                    feedback(to, new Progress(task.id(), "CRAW_FINISH", executorId, System.currentTimeMillis(), crawResult.data().size()));

                    pushResult(destination, crawResult);

                    feedback(to, new Progress(task.id(), "UPLOADED", executorId, System.currentTimeMillis(), crawResult.data().size()));

                    // wait async http request execute success
                    Thread.currentThread().join(10000);
                }
            } catch (Exception ignore) {
                log.error(ignore.getMessage(), ignore);
            }

            times++;

            if (!sleep()) {
                break;
            }
        }
    }

    private static String resolveHostName() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getHostName();
        } catch (UnknownHostException uhe) {
            return null;
        }
    }

    private static boolean sleep() {
        try {
            Thread.sleep(30000);
            return true;
        } catch (InterruptedException ie) {
            Thread.interrupted();
            return false;
        }
    }

    private static TaskDefinition pollTask(String source) throws Exception {
        if (source.startsWith(FILE_PROTOCOL)) {
            File src = new File(source.substring(FILE_PROTOCOL.length()));
            String srcFile = source;
            if (src.isDirectory()) {
                File[] files = src.listFiles(f -> f.getName().endsWith(JSON_SUFFIX) && !PROCESSED.contains(f.getName()));
                if (files.length == 0) {
                    return null;
                }
                PROCESSED.add(files[0].getName());
                srcFile += File.pathSeparator + files[0].getName();
            }
            try (FileInputStream fis =new FileInputStream(srcFile.substring(FILE_PROTOCOL.length()))) {
                return om.readValue(fis, TaskDefinition.class);
            }
        } else if (source.startsWith(HTTP_PROTOCOL) || source.startsWith(HTTPS_PROTOCOL)) {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(source))
                    .GET()
                    .build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            return om.readValue(response.body(), TaskDefinition.class);
        }
        return null;
    }

    private static void feedback(String to, Progress progress) throws Exception {
        if ("console".equals(to)) {
            log.info("task progress: {}", progress);
        } else {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(to))
                    .header(CONTENT_TYPE_HEADER, APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(om.writeValueAsBytes(progress)))
                    .build();
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(x -> log.debug("feedback to [{}] and response status: {}", to, x.statusCode()));
        }
    }

    private static void pushResult(String destination, Result crawResult) throws Exception {
        if (destination.startsWith(FILE_PROTOCOL)) {
            String destFile = destination;
            File dest = new File(destination.substring(FILE_PROTOCOL.length()));
            if (dest.isDirectory()) {
                destFile += File.pathSeparator + crawResult.taskId() + JSON_SUFFIX;
            }
            try (FileOutputStream fos = new FileOutputStream(destFile.substring(FILE_PROTOCOL.length()))) {
                fos.write(om.writeValueAsBytes(crawResult));
            }
        } else if (destination.startsWith(HTTP_PROTOCOL) || destination.startsWith(HTTPS_PROTOCOL)) {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(destination))
                    .header(CONTENT_TYPE_HEADER, APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(om.writeValueAsBytes(crawResult)))
                    .build();
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(x -> log.debug("submit result to [{}] and response status: {}", destination, x.statusCode()));
        }
    }

}

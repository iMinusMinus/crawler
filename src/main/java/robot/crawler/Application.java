package robot.crawler;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import robot.crawler.reactor.Register;
import robot.crawler.spec.ForceStopException;
import robot.crawler.spec.Progress;
import robot.crawler.spec.Result;
import robot.crawler.spec.TaskDefinition;
import robot.crawler.spec.TaskExecutor;
import robot.crawler.spec.TaskSettingDefinition;
import robot.crawler.spec.VerifyStopException;

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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final String FILE_PROTOCOL = "file://";

    private static final String HTTP_PROTOCOL = "http://";

    private static final String HTTPS_PROTOCOL = "https://";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String APPLICATION_JSON_VALUE = "application/json;charset=UTF-8";

    private static final String JSON_SUFFIX = ".json";

    private static final String APPLICATION_JSON_PROP_KEY = "crawler.application.json";

    private static final String APPLICATION_JSON_ENV_KEY = "CRAWLER_APPLICATION_JSON";

    private static final Set<String> PROCESSED = new HashSet<>();

    private static ObjectMapper om;

    private static HttpClient httpClient;


    public static class Args {

        @Parameter(names = {"-r", "--read"}, required = true, description = "read job definition from: file:// or http[s]://")
        private String taskSource;

        @Parameter(names = {"-e", "--executor"}, description = "job executor type: webdriver, jsoup")
        private String executorType;

        @Parameter(names = {"-f", "--feedback"}, description = "progress feedback to: console, http[s]://")
        private String feedback = "console";

        @Parameter(names = {"-w", "--write"}, required = true, description = "write to place: file:// or http[s]://")
        private String outputDestination;

        @Parameter(names = {"--connect-timeout"}, description = "http connection timeout")
        private long connectionTimeout = 3000;

        @Parameter(names = {"--read-timeout"}, description = "http connection timeout")
        private long readTimeout = 5000;

        @Parameter(names = {"-t", "--times"}, description = "max fetch task times from remote server")
        private int fetchTaskMaxTimes = 1;

        public String getTaskSource() {
            return taskSource;
        }

        @SuppressWarnings({"unused"})
        public void setTaskSource(String taskSource) {
            this.taskSource = taskSource;
        }

        public String getExecutorType() {
            return executorType;
        }

        @SuppressWarnings({"unused"})
        public void setExecutorType(String executorType) {
            this.executorType = executorType;
        }

        public String getFeedback() {
            return feedback;
        }

        @SuppressWarnings({"unused"})
        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }

        public String getOutputDestination() {
            return outputDestination;
        }

        @SuppressWarnings({"unused"})
        public void setOutputDestination(String outputDestination) {
            this.outputDestination = outputDestination;
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        @SuppressWarnings({"unused"})
        public void setConnectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public long getReadTimeout() {
            return readTimeout;
        }

        @SuppressWarnings({"unused"})
        public void setReadTimeout(long readTimeout) {
            this.readTimeout = readTimeout;
        }

        public int getFetchTaskMaxTimes() {
            return fetchTaskMaxTimes;
        }

        @SuppressWarnings({"unused"})
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

    private static TaskSettingDefinition nodeSettings() {
        String json = System.getProperty(APPLICATION_JSON_PROP_KEY, System.getenv(APPLICATION_JSON_ENV_KEY));
        if (json == null) {
            return null;
        }
        try {
            return om.readValue(json, TaskSettingDefinition.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        Args commandArgs = new Args();
        JCommander.newBuilder().addObject(commandArgs).build().parse(args);

        String taskSource = commandArgs.getTaskSource();
        String destination = commandArgs.getOutputDestination();
        String to = commandArgs.getFeedback();
        long connectionTimeout = commandArgs.getConnectionTimeout();
        long readTimeout = commandArgs.getReadTimeout();
        int maxTimes = commandArgs.getFetchTaskMaxTimes();
        String preferExecutor = commandArgs.getExecutorType();

        String executorId = System.getProperty("user.name") + "@" + resolveHostName();

        configureObjectMapper();

        TaskSettingDefinition defaultNodeSettings = nodeSettings();

        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(connectionTimeout)).build();

        Register.initialize();

        boolean disableJsoup = false;
        TaskDefinition retryTask = null;

        int times = 0;

        while(times < maxTimes) {
            try {
                TaskDefinition taskDefinition = retryTask != null ? retryTask : pollTask(taskSource);
                if (taskDefinition == null) {
                    log.warn("no task to executor when polling {}", taskSource);
                    continue;
                }
                TaskDefinition task = defaultNodeSettings == null ?
                        taskDefinition :
                        new TaskDefinition(taskDefinition.id(), taskDefinition.name(), taskDefinition.url(),
                                taskDefinition.version(), defaultNodeSettings, taskDefinition.steps());
                retryTask = task;

                String executorType = preferExecutor != null ? preferExecutor :
                        (disableJsoup ? Register.EXECUTOR_WEBDRIVER : Register.EXECUTOR_JSOUP);
                TaskExecutor taskExecutor = Register.getApplicationScopeObject(executorType, TaskExecutor.class);

                feedback(to, new Progress(task.id(), "ACCEPT", executorId, System.currentTimeMillis(), 0), readTimeout);

                Result crawResult = taskExecutor.execute(task);

                feedback(to, new Progress(task.id(), "CRAW_FINISH", executorId, System.currentTimeMillis(), crawResult.data().size()), readTimeout);

                pushResult(destination, crawResult, readTimeout);

                feedback(to, new Progress(task.id(), "UPLOADED", executorId, System.currentTimeMillis(), crawResult.data().size()), readTimeout);

                // wait async http request execute success
                Thread.currentThread().join(readTimeout);

                disableJsoup = false;
                if (!crawResult.corrupt()) {
                    retryTask = null;
                }

            } catch (VerifyStopException vse) {
                if (disableJsoup) {
                    log.warn("jsoup request fail too many times! try not set -e or '--executor', and let program choose!");
                    break;
                }
                disableJsoup = true;
            } catch (ForceStopException fte) {
                log.error("force stop, exit now!");
                break;
            } catch (Exception ignore) {
                log.error(ignore.getMessage(), ignore);
            }

            times++;

            if (!sleep()) {
                break;
            }
        }

        Register.destroy();
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
            Thread.sleep(ThreadLocalRandom.current().nextLong(1000, 5000));
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
                if (files == null || files.length == 0) {
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
            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) {
                    log.error("poll task error: {}", new String(body.readAllBytes(), StandardCharsets.UTF_8));
                }
                return om.readValue(body, TaskDefinition.class);
            }
        }
        return null;
    }

    private static void feedback(String to, Progress progress, long readTimeout) throws Exception {
        if ("console".equals(to)) {
            log.info("task progress: {}", progress);
        } else {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(to))
                    .header(CONTENT_TYPE_HEADER, APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(om.writeValueAsBytes(progress)))
                    .timeout(Duration.ofMillis(readTimeout))
                    .build();
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(x -> log.debug("feedback to [{}] and response status: {}", to, x.statusCode()));
        }
    }

    private static void pushResult(String destination, Result crawResult, long readTimeout) throws Exception {
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
                    .timeout(Duration.ofMillis(readTimeout))
                    .build();
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(x -> log.debug("submit result to [{}] and response status: {}", destination, x.statusCode()));
        }
    }

}

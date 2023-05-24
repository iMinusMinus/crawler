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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final String FILE_PROTOCOL = "file://";

    private static final String HTTP_PROTOCOL = "http://";

    private static final String HTTPS_PROTOCOL = "https://";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    // http://www.iana.org/assignments/http-authschemes/http-authschemes.xhtml
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String APPLICATION_JSON_VALUE = "application/json;charset=UTF-8";

    private static final String JSON_SUFFIX = ".json";

    private static final String APPLICATION_JSON_PROP_KEY = "crawler.application.json";

    private static final String APPLICATION_JSON_ENV_KEY = "CRAWLER_APPLICATION_JSON";

    private static final String AUTHORIZATION_ENV_KEY = "CRAWLER_AUTHORIZATION_TOKEN";

    private static final Set<String> PROCESSED = new HashSet<>();

    private static ObjectMapper om;

    private static HttpClient httpClient;

    private static char[] authorizationToken;


    public static class Args {

        @Parameter(names = {"-r", "--read"}, required = true, description = "read job definition from: file:// or http[s]://")
        private String taskSource;

        @Parameter(names = {"-e", "--executor"}, description = "job executor type: webdriver, jsoup")
        private String executorType;

        @Parameter(names = {"-f", "--feedback"}, description = "progress feedback to: null for console, or use http[s]://")
        private String feedback;

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
        final int maxTimes = commandArgs.getFetchTaskMaxTimes();
        String preferExecutor = commandArgs.getExecutorType();

        String executorId = System.getProperty("user.name") + "@" + resolveHostName();

        configureObjectMapper();

        TaskSettingDefinition defaultNodeSettings = nodeSettings();
        log.debug("node settings: {}", defaultNodeSettings);

        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(connectionTimeout)).build();
        authorizationToken = Optional.ofNullable(System.getenv(AUTHORIZATION_ENV_KEY))
                .map(String::toCharArray)
                .orElse(new char[0]);


        Register.initialize();

        boolean disableJsoup = false;
        TaskDefinition retryTask = null;

        int times = 0;

        CompletableFuture<Void> out = null;

        while(times < maxTimes || maxTimes < 0) {
            try {
                TaskDefinition taskDefinition = retryTask != null ? retryTask : pollTask(taskSource, executorId, readTimeout);
                if (taskDefinition == null) {
                    log.warn("no task to executor when polling {}", taskSource);
                    times++;
                    sleep();
                    continue;
                }
                TaskDefinition task = defaultNodeSettings == null ?
                        taskDefinition :
                        new TaskDefinition(taskDefinition.id(), taskDefinition.name(), taskDefinition.url(),
                                taskDefinition.version(), defaultNodeSettings, taskDefinition.steps());
                retryTask = task;

                // TODO choose strategy depends on user specify or statistics
                String executorType = preferExecutor != null ? preferExecutor :
                        (disableJsoup ? Register.EXECUTOR_WEBDRIVER : Register.EXECUTOR_JSOUP);
                TaskExecutor taskExecutor = Register.getApplicationScopeObject(executorType, TaskExecutor.class);

                CompletableFuture<Void> init = feedback(to, new Progress(task.id(), "ACCEPT", executorId, System.currentTimeMillis(), 0), readTimeout);

                Result crawResult = taskExecutor.execute(task);

                CompletableFuture<Void> executed = feedback(to, new Progress(task.id(), "CRAW_FINISH", executorId, System.currentTimeMillis(), crawResult.data().size()), readTimeout);

                CompletableFuture<Void> submitted = pushResult(destination, crawResult, readTimeout);

                CompletableFuture<Void> uploaded = feedback(to, new Progress(task.id(), "UPLOADED", executorId, System.currentTimeMillis(), crawResult.data().size()), readTimeout);

                // wait async http request execute success
                CompletableFuture<Void> asyncResult = CompletableFuture.allOf(init, executed, submitted, uploaded);
                asyncResult.get(readTimeout, TimeUnit.MILLISECONDS);

                disableJsoup = false;
                if (!crawResult.corrupt()) {
                    retryTask = null;
                }

            } catch (VerifyStopException vse) {
                out = feedback(to, new Progress(Optional.ofNullable(retryTask).map(TaskDefinition::id).orElse(null), "ACCOUNT_VERIFY", executorId, System.currentTimeMillis(), 0), readTimeout);
                if (disableJsoup) {
                    log.warn("jsoup request fail too many times! try not set -e or '--executor', and let program choose!");
                    break;
                }
                disableJsoup = true;
            } catch (ForceStopException fte) {
                log.error("force stop, exit now!");
                out = feedback(to, new Progress(Optional.ofNullable(retryTask).map(TaskDefinition::id).orElse(null), "ACCOUNT_FORBIDDEN", executorId, System.currentTimeMillis(), 0), readTimeout);
                break;
            } catch (Exception ignore) {
                log.error(ignore.getMessage(), ignore);
                out = feedback(to, new Progress(Optional.ofNullable(retryTask).map(TaskDefinition::id).orElse(null), "UNKNOWN_EXCEPTION", executorId, System.currentTimeMillis(), 0), readTimeout);
            }

            times++;

            if (!sleep()) {
                break;
            }
        }

        Register.destroy();
        if (out != null && !out.isDone() && !out.isCancelled() && !out.isCompletedExceptionally()) {
            out.get(readTimeout, TimeUnit.MILLISECONDS);
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
            Thread.sleep(ThreadLocalRandom.current().nextLong(1000, 5000));
            return true;
        } catch (InterruptedException ie) {
            Thread.interrupted();
            return false;
        }
    }

    private static TaskDefinition pollTask(String source, String executorId, long readTimeout) throws Exception {
        if (source.startsWith(FILE_PROTOCOL)) {
            File src = new File(source.substring(FILE_PROTOCOL.length()));
            String srcFile = source;
            if (src.isDirectory()) {
                File[] files = src.listFiles(f -> f.getName().endsWith(JSON_SUFFIX) && !PROCESSED.contains(f.getName()));
                if (files == null || files.length == 0) {
                    return null;
                }
                PROCESSED.add(files[0].getName());
                srcFile += File.separator + files[0].getName();
            }
            try (FileInputStream fis =new FileInputStream(srcFile.substring(FILE_PROTOCOL.length()))) {
                return om.readValue(fis, TaskDefinition.class);
            }
        } else if (source.startsWith(HTTP_PROTOCOL) || source.startsWith(HTTPS_PROTOCOL)) {
            char separator = source.indexOf('?') > 0 ? '&' : '?';
            String url = source + separator + "executorId=" + executorId;
            HttpRequest request = makeRequest(true, url, null, readTimeout);
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() == 200) {
                    return om.readValue(body, TaskDefinition.class);
                } else if (response.statusCode() == 204) {
                    log.info("no content");
                } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                    throw new ForceStopException("未授权或没有权限访问指定资源，请检查系统环境变量是否设置：" + AUTHORIZATION_ENV_KEY);
                } else {
                    log.error("poll task error: {}", new String(body.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        }
        return null;
    }

    private static CompletableFuture<Void> feedback(String to, Progress progress, long readTimeout) throws Exception {
        log.info("task progress: {}", progress);
        if (to != null && (to.startsWith(HTTP_PROTOCOL) || to.startsWith(HTTPS_PROTOCOL))) {
            HttpRequest request = makeRequest(false, to, progress, readTimeout);
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(x -> log.info("feedback to [{}] and response status: {}", to, x.statusCode()));
        }
        return CompletableFuture.completedFuture(null);
    }

    private static CompletableFuture<Void> pushResult(String destination, Result crawResult, long readTimeout) throws Exception {
        if (destination.startsWith(FILE_PROTOCOL)) {
            String destFile = destination;
            File dest = new File(destination.substring(FILE_PROTOCOL.length()));
            if (dest.isDirectory()) {
                destFile += File.separator + crawResult.taskId() + JSON_SUFFIX;
            }
            try (FileOutputStream fos = new FileOutputStream(destFile.substring(FILE_PROTOCOL.length()))) {
                fos.write(om.writeValueAsBytes(crawResult));
            }
        } else if (destination.startsWith(HTTP_PROTOCOL) || destination.startsWith(HTTPS_PROTOCOL)) {
            HttpRequest request = makeRequest(false, destination, crawResult, readTimeout);
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(x -> log.info("submit result to [{}] and response status: {}", destination, x.statusCode()));
        }
        return CompletableFuture.completedFuture(null);
    }

    private static HttpRequest makeRequest(boolean get, String url, Object data, long readTimeout) throws Exception {
        HttpRequest.Builder rb = HttpRequest.newBuilder().uri(new URI(url));
        if (authorizationToken.length != 0) {
            rb.header(AUTHORIZATION_HEADER, new String(authorizationToken));
        }
        if (!get) {
            rb.header(CONTENT_TYPE_HEADER, APPLICATION_JSON_VALUE);
        }
        rb.timeout(Duration.ofMillis(readTimeout));
        return get ? rb.GET().build() : rb.POST(HttpRequest.BodyPublishers.ofByteArray(om.writeValueAsBytes(data))).build();
    }

}

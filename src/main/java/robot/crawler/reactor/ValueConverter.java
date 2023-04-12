package robot.crawler.reactor;

@FunctionalInterface
public interface ValueConverter<T> {

    T convert(String raw);

}

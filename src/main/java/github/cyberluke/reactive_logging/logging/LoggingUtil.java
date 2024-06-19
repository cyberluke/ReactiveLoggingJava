package github.cyberluke.reactive_logging.logging;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Signal;
import reactor.util.context.ContextView;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * logging context into MDC
 */
public class LoggingUtil {

    public static final String MDC_KEY = "ReactiveTraceId";
    public static final String CONTEXT_KEY = "CONTEXT_KEY";
    public static final String X_REQUEST_ID = "X-Request-ID";


    public static String getTraceId(HttpHeaders headers) {
        List<String> requestIdHeaders = headers.get(X_REQUEST_ID);
        return requestIdHeaders == null || requestIdHeaders.isEmpty()
            ? UUID.randomUUID().toString()
            : requestIdHeaders.get(0);
    }


    public static <T> Consumer<Signal<T>> logOnEach(Consumer<T> logStatement) {
        return signal -> {
            String contextValue = signal.getContextView().get(CONTEXT_KEY);

            try (MDC.MDCCloseable cMdc = MDC.putCloseable(MDC_KEY, contextValue)) {
                logStatement.accept(signal.get());
            }
        };
    }

    public static String camelCaseToSpaced(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Remove common prefixes.
        input = input.replaceAll("^(get|set|is)", "");

        // Split the string at each uppercase letter, except for the first character.
        String[] words = input.split("(?<!^)(?=[A-Z])");

        // Transform the first character to uppercase and the rest to lowercase (if desired).
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(" ");
            }
            result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
        }

        return result.toString();
    }

    public static String camelCaseToUpperSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return input.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
    }

    public static Map<Object, Object> getEntries(ContextView ctx) {
        return ctx.stream().filter(r -> r.getValue() instanceof String || r.getValue() instanceof UUID)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    public static String formatEntries(Map<Object, Object> entries) {
        return entries.entrySet().stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

}

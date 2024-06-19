package github.cyberluke.reactive_logging.logging;


import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static github.cyberluke.reactive_logging.logging.LoggingUtil.CONTEXT_KEY;
import static github.cyberluke.reactive_logging.logging.LoggingUtil.MDC_KEY;
import static github.cyberluke.reactive_logging.logging.LoggingUtil.camelCaseToSpaced;
import static github.cyberluke.reactive_logging.logging.LoggingUtil.camelCaseToUpperSnakeCase;
import static github.cyberluke.reactive_logging.logging.LoggingUtil.getEntries;


@Aspect
@Component
@Slf4j
@AllArgsConstructor
/**
 * Aspect intended to add the values from Context into the MDC for logging purposes
 */
public class MdcAspectController {

    private final String EXCEPTION_MSG = "exception while setting MDC in aspect";

    /**
     * @param joinPoint to extract returntype and to proceed with execuiton
     * @return Any Publisher types depending on controller endpoint return
     */
    @Around("within(github.cyberluke*..*Controller)")
    @SneakyThrows()
    public Publisher<?> aroundEndpointExecution(ProceedingJoinPoint joinPoint) {
        //log.info("setting value from context into MDC for logging before controller endpoint execution");
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = methodSignature.getReturnType();
        if (Mono.class.isAssignableFrom(returnType)) {
            return handleMono(joinPoint, returnType);
        } else if (Flux.class.isAssignableFrom(returnType)) {
            return handleFlux(joinPoint, returnType);
        } else {
            return (Publisher<?>) joinPoint.proceed();
        }
    }

    private Flux<?> handleFlux(ProceedingJoinPoint joinPoint, Class<?> returnType) {
        return Flux.deferContextual(contextView -> {

            try (MDC.MDCCloseable mdcCloseable = MDC.putCloseable(MDC_KEY, contextView.get(CONTEXT_KEY))) {
                Map<String, Object> entries = getMethodParameters(joinPoint);
                MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
                String methodName = methodSignature.getName();
                Logger classLogger = LoggerFactory.getLogger(methodSignature.getDeclaringType());
                Logger logger = (classLogger != null) ? classLogger : log;

                Map<Object, Object> entriesWithCtx = getEntries(contextView);
                entriesWithCtx.putAll(entries);

                entriesWithCtx.forEach((key, value) -> MDC.put(key.toString(), value.toString()));

                return ((Flux<?>) joinPoint.proceed())
                    .contextWrite(context -> context.putAllMap(entries))
                    .doOnSubscribe(subscription -> logger.info("Processing request for {}", camelCaseToSpaced(methodName)))
                    .doOnComplete(() -> logger.info("Successfully executed method: {}", joinPoint.getSignature()))
                    .doOnError(throwable -> logger.error("Error while processing {} request", camelCaseToSpaced(methodName), throwable));
            } catch (Throwable e) {
                log.error(e.getMessage());
                throw new CustomRuntimeException(e, EXCEPTION_MSG);
            }
        });
    }

    private Mono<Object> handleMono(ProceedingJoinPoint joinPoint, Class<?> returnType) {

        return Mono.deferContextual(contextView -> {

            try (MDC.MDCCloseable mdcCloseable = MDC.putCloseable(MDC_KEY, contextView.get(CONTEXT_KEY))) {
                Map<String, Object> entries = getMethodParameters(joinPoint);
                MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
                String methodName = methodSignature.getName();
                Logger classLogger = LoggerFactory.getLogger(methodSignature.getDeclaringType());
                Logger logger = (classLogger != null) ? classLogger : log;

                Map<Object, Object> entriesWithCtx = getEntries(contextView);
                entriesWithCtx.putAll(entries);

                entriesWithCtx.forEach((key, value) -> MDC.put(key.toString(), value.toString()));

                return ((Mono<?>) joinPoint.proceed())
                    .contextWrite(context -> context.putAllMap(entries))
                    .doOnSubscribe(subscription -> logger.info("Processing request for {}", camelCaseToSpaced(methodName)))
                    .doOnError(throwable -> logger.error("Error while processing {} request", camelCaseToSpaced(methodName), throwable));
            } catch (Throwable e) {
                log.error(e.getMessage());
                throw new CustomRuntimeException(e, EXCEPTION_MSG);
            }
        });

    }

    private Map<String, Object> getMethodParameters(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();

        Map<String, Object> entries = new HashMap<>();
        if (parameterNames != null) {

            for (int i = 0; i < parameterNames.length; i++) {
                String key = parameterNames[i];
                Object value = parameterValues[i];
                entries.put(camelCaseToUpperSnakeCase(key), value);
            }
        }
        return entries;
    }
}

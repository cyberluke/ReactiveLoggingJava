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

import java.util.Map;

import static github.cyberluke.reactive_logging.logging.LoggingUtil.CONTEXT_KEY;
import static github.cyberluke.reactive_logging.logging.LoggingUtil.MDC_KEY;
import static github.cyberluke.reactive_logging.logging.LoggingUtil.camelCaseToSpaced;
import static github.cyberluke.reactive_logging.logging.LoggingUtil.getEntries;


@Aspect
@Component
@Slf4j
@AllArgsConstructor
/**
 * Aspect intended to add the values from Context into the MDC for logging purposes
 */
public class MdcAspectService {

    private final String EXCEPTION_MSG = "exception while setting MDC in aspect";

    /**
     * @param joinPoint to extract returntype and to proceed with execuiton
     * @return Any Publisher types depending on controller endpoint return
     */
    @Around("within(*..*Service)")
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
                MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
                String methodName = methodSignature.getName();
                Logger classLogger = LoggerFactory.getLogger(methodSignature.getDeclaringType());
                Logger logger = (classLogger != null) ? classLogger : log;

                Map<Object, Object> entriesWithCtx = getEntries(contextView);

                entriesWithCtx.forEach((key, value) -> MDC.put(key.toString(), value.toString()));

                return ((Flux<?>) joinPoint.proceed())
                    .doOnSubscribe(subscription -> logger.info("Processing request for {}", camelCaseToSpaced(methodName)))
                    .doOnComplete(() -> logger.info("Successfully executed method: {}", joinPoint.getSignature()))
                    .doOnError(throwable -> logger.error("Error while processing {} request", camelCaseToSpaced(methodName), throwable));
            } catch (Throwable e) {
                throw new CustomRuntimeException(e, EXCEPTION_MSG);
            }
        });
    }

    private Mono<Object> handleMono(ProceedingJoinPoint joinPoint, Class<?> returnType) {
        return Mono.deferContextual(contextView -> {
            String contextKey = contextView.get(CONTEXT_KEY);

            try (MDC.MDCCloseable mdcCloseable = MDC.putCloseable(MDC_KEY, contextKey)) {

                MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
                String methodName = methodSignature.getName();
                String className = methodSignature.getDeclaringType().getSimpleName();
                Logger classLogger = LoggerFactory.getLogger(methodSignature.getDeclaringType());
                Logger logger = (classLogger != null) ? classLogger : log;

                Map<Object, Object> entriesWithCtx = getEntries(contextView);

                entriesWithCtx.forEach((key, value) -> MDC.put(key.toString(), value.toString()));

                return ((Mono<?>) joinPoint.proceed())
                    .doOnSubscribe(subscription -> logger.info("Calling service for {}", camelCaseToSpaced(methodName)))
                    .doOnSuccess(result -> logger.debug("Successfully called service {} and {}", className, methodName))
                    .doOnError(throwable -> logger.error("Error while processing {} request in {}", camelCaseToSpaced(methodName), className, throwable));
            } catch (Throwable e) {
                log.error(e.getMessage());
                throw new CustomRuntimeException(e, EXCEPTION_MSG);
            }
        });
    }
}

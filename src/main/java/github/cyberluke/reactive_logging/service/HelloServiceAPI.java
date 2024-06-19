package github.cyberluke.reactive_logging.service;

import reactor.core.publisher.Mono;

public interface HelloServiceAPI {

    public Mono<String> getHelloMessage(String name);
}

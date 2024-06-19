package github.cyberluke.reactive_logging.dao;

import reactor.core.publisher.Mono;

public interface HelloRepositoryAPI {
    public Mono<String> getHelloMessageForToday(String name);
}

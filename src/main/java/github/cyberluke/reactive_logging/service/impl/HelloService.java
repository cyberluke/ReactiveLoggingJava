package github.cyberluke.reactive_logging.service.impl;

import github.cyberluke.reactive_logging.dao.HelloRepositoryAPI;
import github.cyberluke.reactive_logging.service.HelloServiceAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service
public class HelloService implements HelloServiceAPI {

    HelloRepositoryAPI helloRepository;

    @Override
    public Mono<String> getHelloMessage(String name) {
        return helloRepository.getHelloMessageForToday(name);
    }
}

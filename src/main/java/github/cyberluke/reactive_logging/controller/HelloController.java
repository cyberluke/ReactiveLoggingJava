package github.cyberluke.reactive_logging.controller;

import github.cyberluke.reactive_logging.dto.input.UserDto;
import github.cyberluke.reactive_logging.dto.output.HelloResponseDto;
import github.cyberluke.reactive_logging.service.HelloServiceAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@RestController
public class HelloController implements HelloAPI {

    HelloServiceAPI helloService;

    @Override
    public Mono<HelloResponseDto> helloWorld(UserDto user) {
        return helloService.getHelloMessage(user.name()).flatMap(
                message -> Mono.just(HelloResponseDto.builder().response(message).build())
                );
    }
}

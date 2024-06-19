package github.cyberluke.reactive_logging.controller;

import github.cyberluke.reactive_logging.dto.input.UserDto;
import github.cyberluke.reactive_logging.dto.output.HelloResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@RequestMapping(value = HelloAPI.PATH)
@SecurityScheme(
        type = SecuritySchemeType.HTTP)
@Tag(name = "Hello API", description = "Used for handling hello world requests. Accessible by external and internal users.")
public interface HelloAPI {
    String PATH = "/api";

    @Operation(summary = "Hello World endpoint")
    @GetMapping("/hello")
    Mono<HelloResponseDto> helloWorld(@RequestBody UserDto name);
}

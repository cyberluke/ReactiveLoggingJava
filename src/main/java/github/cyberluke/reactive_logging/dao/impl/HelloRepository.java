package github.cyberluke.reactive_logging.dao.impl;

import github.cyberluke.reactive_logging.dao.HelloRepositoryAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.time.DayOfWeek;

@Slf4j
@Component
@RequiredArgsConstructor
public class HelloRepository implements HelloRepositoryAPI {

    @Override
    public Mono<String> getHelloMessageForToday(String name) {
        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Get the day of the week
        DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

        // Get the name of the day
        String dayName = dayOfWeek.name();

        return Mono.just("Hello " + name +", Reactive World on " + dayName + "!");
    }
}

package spongecell.backend.metricsagregatator.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;


/**
 * Created by abyakimenko on 29.06.2019.
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ErrorController {

    //@ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    public void handleNotFoundException(NoHandlerFoundException e) {
        log.error(e.getMessage());
    }
}

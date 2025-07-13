package tolliu.esstress.regular.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tolliu.esstress.regular.domain.InsufficientBalanceException;

@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {
    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInsufficientBalanceException(InsufficientBalanceException e) {
        return new ErrorResponse(e.getMessage());
    }

    public record ErrorResponse(String message) {}
}

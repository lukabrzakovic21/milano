package com.master.milano.exception;

import com.master.milano.exception.invoice.InvoiceBadRequest;
import com.master.milano.exception.invoice.InvoiceInsufficientFundsException;
import com.master.milano.exception.invoice.InvoiceNotFoundException;
import com.master.milano.exception.item.ItemBadRequest;
import com.master.milano.exception.item.ItemCanBeBoughtException;
import com.master.milano.exception.item.ItemNotFoundException;
import com.master.milano.exception.item.NoMoreItemsException;
import com.master.milano.exception.item.UserAlreadyInterestInItem;
import com.master.milano.exception.util.ErrorResponse;
import com.master.milano.exception.util.UnauthorizedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = ItemBadRequest.class)
    protected ResponseEntity<Object> handleItemBadRequest(RuntimeException exception, WebRequest webRequest) {

        return  handleExceptionInternal(exception, new ErrorResponse(400, exception.getMessage()),
                new HttpHeaders(), HttpStatusCode.valueOf(400), webRequest);
    }

    @ExceptionHandler(value = ItemNotFoundException.class)
    protected ResponseEntity<Object> handleItemNotFound(RuntimeException exception, WebRequest webRequest) {

        return  handleExceptionInternal(exception, new ErrorResponse(404, exception.getMessage()),
                new HttpHeaders(), HttpStatusCode.valueOf(404), webRequest);
    }

    @ExceptionHandler(value = InvoiceBadRequest.class)
    protected ResponseEntity<Object> handleInvoiceBadRequest(RuntimeException exception, WebRequest webRequest) {

        return  handleExceptionInternal(exception, new ErrorResponse(400, exception.getMessage()),
                new HttpHeaders(), HttpStatusCode.valueOf(400), webRequest);
    }

    @ExceptionHandler(value = InvoiceNotFoundException.class)
    protected ResponseEntity<Object> handleInvoiceNotFound(RuntimeException exception, WebRequest webRequest) {

        return  handleExceptionInternal(exception, new ErrorResponse(404, exception.getMessage()),
                new HttpHeaders(), HttpStatusCode.valueOf(404), webRequest);
    }

        @ExceptionHandler(value = InvoiceInsufficientFundsException.class)
    protected ResponseEntity<Object> handleInvoiceInsufficientFunds(RuntimeException exception, WebRequest webRequest) {

        return  handleExceptionInternal(exception, new ErrorResponse(409, exception.getMessage()),
                new HttpHeaders(), HttpStatusCode.valueOf(409), webRequest);
    }

    @ExceptionHandler(value = NoMoreItemsException.class)
    protected ResponseEntity<Object> handleNoMoreItemsException(RuntimeException exception, WebRequest webRequest) {

        return  handleExceptionInternal(exception, new ErrorResponse(422, exception.getMessage()),
                new HttpHeaders(), HttpStatusCode.valueOf(422), webRequest);
    }

    @ExceptionHandler(value = ItemCanBeBoughtException.class)
    protected ResponseEntity<Object> handleItemCanBeBoughtException(RuntimeException exception, WebRequest webRequest) {

        return  handleExceptionInternal(exception, new ErrorResponse(409, exception.getMessage()),
                new HttpHeaders(), HttpStatusCode.valueOf(409), webRequest);
    }

    @ExceptionHandler(value = UserAlreadyInterestInItem.class)
    protected ResponseEntity<Object> handleUserAlreadyInterestedInItem(RuntimeException exception, WebRequest webRequest) {

        return  handleExceptionInternal(exception, new ErrorResponse(409, exception.getMessage()),
                new HttpHeaders(), HttpStatusCode.valueOf(409), webRequest);
    }

    @ExceptionHandler(value = UnauthorizedException.class)
    protected ResponseEntity<Object> handleUnauthorizedException(RuntimeException exception, WebRequest webRequest) {

        return  handleExceptionInternal(exception, new ErrorResponse(403, exception.getMessage()),
                new HttpHeaders(), HttpStatusCode.valueOf(403), webRequest);
    }
}

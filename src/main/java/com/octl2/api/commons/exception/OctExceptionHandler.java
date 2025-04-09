package com.octl2.api.commons.exception;

import com.octl2.api.commons.OctResponse;
import com.octl2.api.commons.suberror.ApiSubError;
import com.octl2.api.commons.suberror.ApiValidatorError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class OctExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<OctResponse<String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("handleMethodArgumentNotValid");

        List<ApiSubError> details = new ArrayList<>();
        ex.getBindingResult().getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    Object rejectValue = ((FieldError) error).getRejectedValue();
                    String message = error.getDefaultMessage();

                    details.add(new ApiValidatorError(fieldName, rejectValue, message));
                });
        return new ResponseEntity<>(OctResponse.build(ErrorMessages.INVALID_VALUE, details), HttpStatus.OK);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<OctResponse<String>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("handleConstraintViolationException. Message = {}", ex.getMessage(), ex.getCause());
        return new ResponseEntity<>(OctResponse.build(ex.getMessage(), HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<OctResponse<String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("handleHttpMessageNotReadable. Message = {}", ex.getMessage(), ex.getCause(), ex);
        OctInvalidInputException exception = (OctInvalidInputException) ex.getCause().getCause();
        return new ResponseEntity<>(OctResponse.build(exception.getErrMsg(), exception.getApiSubErrors()), HttpStatus.OK);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<OctResponse<String>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.error("handleNoHandlerFoundException. Message = {}", ex.getMessage(), ex.getCause(), ex);
        return new ResponseEntity<>(OctResponse.build(ErrorMessages.NOT_FOUND), HttpStatus.OK);
    }

    @ExceptionHandler(OctException.class)
    protected ResponseEntity<OctResponse<String>> handleOctException(OctException ex) {
        log.error("handleOctException. Msg = {}", ex.getErrMsg().getMessage(), ex.getCause(), ex);
        return new ResponseEntity<>(OctResponse.build(ErrorMessages.BAD_REQUEST), HttpStatus.OK);
    }

    @ExceptionHandler(OctEntityNotFoundException.class)
    protected ResponseEntity<OctResponse<String>> handleOctEntityNotFound(OctEntityNotFoundException ex) {
        log.error("handleOctEntityNotFound. Msg = {}", ex.getErrMsg().getMessage(), ex.getCause(), ex);
        return new ResponseEntity<>(OctResponse.build(ex.getErrMsg(), Collections.singletonList(ex.getApiMessageError())), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OctInvalidInputException.class)
    protected ResponseEntity<OctResponse<String>> handleInvalidInputRequest(OctInvalidInputException ex) {
        log.error("handleInvalidInputRequest. Msg = {}", ex.getErrMsg().getMessage(), ex.getCause(), ex);
        return new ResponseEntity<>(OctResponse.build(ex.getErrMsg(), ex.getApiSubErrors()), HttpStatus.OK);
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<OctResponse<String>> handleRuntimeException(RuntimeException ex) {
        log.error("handleRuntimeException. Msg = {}", ex.getMessage(), ex.getCause(), ex);
        return new ResponseEntity<>(OctResponse.build(ex.getMessage(), HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<OctResponse<String>> handleException(Exception ex) {
        log.error("handleException. Msg = {}", ex.getMessage(), ex.getCause(), ex);
        return new ResponseEntity<>(OctResponse.build(ex.getMessage(), HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
    }


    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    protected ResponseEntity<OctResponse<String>> handleInvalidDataException(InvalidDataAccessResourceUsageException ex) {
        log.error("handleInvalidDataException. Msg = {}", ex.getMessage(), ex.getCause(), ex);
        return new ResponseEntity<>(OctResponse.build(ex.getMessage(), HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
    }

    @ExceptionHandler(BeanCreationException.class)
    private ResponseEntity<OctResponse<String>> buildResponseEntity(BeanCreationException ex) {
        log.error("handleBeanCreationException. Msg = {}", ex.getMessage(), ex.getCause(), ex);
        return new ResponseEntity<>(OctResponse.build(ex.getMessage(), HttpStatus.BAD_REQUEST.value()), HttpStatus.OK);
    }


    //    @ExceptionHandler
    private ResponseEntity<OctResponse<String>> buildResponseEntity(OctException ex) {
        log.error("handleOctException. Msg = {}", ex.getMessage(), ex.getCause(), ex);
        return new ResponseEntity<>(OctResponse.buildApplicationException(ex), HttpStatus.OK);
    }


}

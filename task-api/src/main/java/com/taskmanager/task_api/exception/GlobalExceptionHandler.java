package com.taskmanager.task_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> handleValidationErrors(MethodArgumentNotValidException ex){
        Map<String, String> errors=new HashMap<>();
        List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();

        for(int i=0;i<allErrors.size();i++){
            FieldError fieldError=(FieldError)allErrors.get(i);
            String fieldName= fieldError.getField();
            String message=allErrors.get(i).getDefaultMessage();
            errors.put(fieldName,message);
        }

//        ex.getBindingResult().getAllErrors().forEach(error ->{
//            String fieldName=((FieldError) error).getField();
//            String message =error.getDefaultMessage();
//            errors.put(fieldName,message);
//        });
        return errors;
    }
}

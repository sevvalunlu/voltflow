package com.voltflow.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model) throws Exception {

        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            throw ex;
        }
        
        model.addAttribute("errorMessage", ex.getMessage() != null ? ex.getMessage() : "Bilinmeyen hata.");
        model.addAttribute("errorDetails", ex.toString());
        return "error"; 
    }

    
}
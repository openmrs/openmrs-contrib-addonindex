package org.openmrs.addonindex.rest;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(NullPointerException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleNPE(Exception ex) {
		return ex.getMessage();
	}
	
	/**
	 * For example these are throws by Optional.get()
	 *
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(NoSuchElementException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleNoSuchElement(Exception ex) {
		return ex.getMessage();
	}
	
}

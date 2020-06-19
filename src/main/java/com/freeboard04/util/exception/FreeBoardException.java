package com.freeboard04.util.exception;

import com.freeboard04.domain.BaseExceptionType;
import lombok.Getter;

public class FreeBoardException extends RuntimeException {

    @Getter
    private BaseExceptionType exceptionType;

    public FreeBoardException(BaseExceptionType exceptionType){
        super(exceptionType.getErrorMessage());
        this.exceptionType = exceptionType;
    }

}

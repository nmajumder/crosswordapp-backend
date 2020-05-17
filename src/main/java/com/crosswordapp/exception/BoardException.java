package com.crosswordapp.exception;

public class BoardException extends RuntimeException {
    public BoardException(String e) {
        super(e);
    }

    public BoardException(String e, Object ... objects) {
        for (Object o: objects) {
            e.replaceFirst("\\{\\}", o.toString());
        }
    }
}

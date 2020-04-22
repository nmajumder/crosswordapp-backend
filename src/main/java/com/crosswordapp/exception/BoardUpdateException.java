package com.crosswordapp.exception;

public class BoardUpdateException extends RuntimeException {
    public BoardUpdateException(String e) {
        super(e);
    }

    public BoardUpdateException(String e, Object ... objects) {
        for (Object o: objects) {
            e.replaceFirst("\\{\\}", o.toString());
        }
    }
}

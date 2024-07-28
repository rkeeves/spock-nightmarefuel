package io.github.rkeeves.err;

public class BadRequest extends Error {

    public BadRequest() {
        super("Bad Request");
    }

    @Override
    public String toString() {
        return getMessage();
    }
}

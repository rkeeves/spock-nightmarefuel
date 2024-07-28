package io.github.rkeeves.err;

public class NotFound extends Error {
    public NotFound() {
        super("Not Found");
    }

    @Override
    public String toString() {
        return getMessage();
    }
}

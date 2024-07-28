package io.github.rkeeves.persistence;

import io.github.rkeeves.model.P;

public interface Repository {
    P find(int pid);

    void save(P p);

    void insert(P p);
}

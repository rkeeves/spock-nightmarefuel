package io.github.rkeeves.service;

import io.github.rkeeves.err.BadRequest;
import io.github.rkeeves.err.NotFound;
import io.github.rkeeves.model.P;
import io.github.rkeeves.model.R;
import io.github.rkeeves.persistence.Repository;
import io.github.rkeeves.persistence.Sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Service {
    private final Sequence sequence;
    private final Repository repository;

    public Service(Sequence sequence, Repository repository) {
        this.sequence = sequence;
        this.repository = repository;
    }

    public List<R> list(int pid) {
        if (pid < 1) { throw new BadRequest(); }
        var p = this.repository.find(pid);
        if (p == null) { return Collections.emptyList(); }
        return p.rs == null ? Collections.emptyList() : p.rs;
    }

    public R find(int pid, int rid) {
        if (pid < 1 || rid < 1) { throw new BadRequest(); }
        var p = this.repository.find(pid);
        if (p == null || p.rs == null) { throw new NotFound(); }
        return p.rs.stream().filter(r -> r.rid == rid).findFirst().orElseThrow(NotFound::new);
    }

    public int upsert(int pid, R r) {
        if (pid < 1 || r == null || r.rid < -1 || r.rid == 0) { throw new BadRequest(); }
        if (r.rid == -1) {
            r.rid = this.sequence.next();
            var p = this.repository.find(pid);
            if (p == null) {
                p = new P();
                p.pid = pid;
                p.rs = List.of(r);
                this.repository.insert(p);
            } else {
                if (p.rs == null) {
                    p.rs = new ArrayList<>();
                }
                p.rs.add(r);
                this.repository.save(p);
            }
            return r.rid;
        } else {
            var p = this.repository.find(pid);
            if (p == null || p.rs == null) {
                throw new NotFound();
            }
            for (int i = 0; i < p.rs.size(); i++) {
                if (p.rs.get(i).rid == r.rid) {
                    p.rs.set(i, r);
                    this.repository.save(p);
                    return r.rid;
                }
            }
            throw new NotFound();
        }
    }

    public int remove(int pid, int rid) {
        if (pid < 1 || rid < 1) { throw new BadRequest(); }
        var p = this.repository.find(pid);
        if (p == null || p.rs == null) { throw new NotFound(); }
        var noDelete = !p.rs.removeIf(r -> r.rid == rid);
        if (noDelete) { throw new NotFound(); }
        this.repository.save(p);
        return rid;
    }
}

package io.github.rkeeves.service

import io.github.rkeeves.common.Either
import io.github.rkeeves.common.IOEither
import io.github.rkeeves.err.BadRequest
import io.github.rkeeves.err.NotFound
import io.github.rkeeves.model.P
import io.github.rkeeves.model.R
import io.github.rkeeves.persistence.Repository
import io.github.rkeeves.persistence.Sequence
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors

import static org.assertj.core.api.Assertions.assertThat



class ServiceTest extends Specification {

    static final int X = 1

    static final int Y = 2

    static R A(int rid) { return new R(rid: rid, name: "A") }

    static R B(int rid) { return new R(rid: rid, name: "B") }

    static R C(int rid) { return new R(rid: rid, name: "C") }

    @Unroll
    def "[#iterationIndex] list"() {
        when:
        var actual = compute(state_before, service -> service.list(pid))
        then:
        actual.assertThatHasAnswer(answer)
        actual.assertThatHasStateAfter(state_after)
        where:
        state_before               | pid || answer           | state_after
        [:]                        | -1  || err(BadRequest)  | [:]
        [:]                        | 0   || err(BadRequest)  | [:]
        [:]                        | X   || ok([])           | [:]
        [(X): null]                | X   || ok([])           | [(X): null]
        [(X): []]                  | X   || ok([])           | [(X): []]
        [(X): [A(1)]]              | X   || ok([A(1)])       | [(X): [A(1)]]
        [(X): [A(1), B(2)]]        | X   || ok([A(1), B(2)]) | [(X): [A(1), B(2)]]
        [(X): [A(1)], (Y): [B(2)]] | Y   || ok([B(2)])       | [(X): [A(1)], (Y): [B(2)]]
    }

    @Unroll
    def "[#iterationIndex] find"() {
        when:
        var actual = compute(state_before, service -> service.find(pid, rid))
        then:
        actual.assertThatHasAnswer(answer)
        actual.assertThatHasStateAfter(state_after)
        where:
        state_before               | pid | rid || answer          | state_after
        [:]                        | -1  | 1   || err(BadRequest) | [:]
        [:]                        | 0   | 1   || err(BadRequest) | [:]
        [:]                        | X   | 1   || err(NotFound)   | [:]
        [(X): null]                | X   | -1  || err(BadRequest) | [(X): null]
        [(X): null]                | X   | 0   || err(BadRequest) | [(X): null]
        [(X): null]                | X   | 1   || err(NotFound)   | [(X): null]
        [(X): []]                  | X   | 1   || err(NotFound)   | [(X): []]
        [(X): [A(1)]]              | X   | 2   || err(NotFound)   | [(X): [A(1)]]
        [(Y): [A(1)]]              | X   | 1   || err(NotFound)   | [(Y): [A(1)]]
        [(X): [A(1)]]              | X   | 1   || ok(A(1))        | [(X): [A(1)]]
        [(X): [A(1), B(2)]]        | X   | 1   || ok(A(1))        | [(X): [A(1), B(2)]]
        [(X): [A(1), B(2)]]        | X   | 2   || ok(B(2))        | [(X): [A(1), B(2)]]
        [(X): [A(1)], (Y): [B(2)]] | X   | 1   || ok(A(1))        | [(X): [A(1)], (Y): [B(2)]]
        [(X): [A(1)], (Y): [B(2)]] | Y   | 2   || ok(B(2))        | [(X): [A(1)], (Y): [B(2)]]
    }

    @Unroll
    def "[#iterationIndex] upsert"() {
        when:
        var actual = compute(state_before, service -> service.upsert(pid, r))
        then:
        actual.assertThatHasAnswer(answer)
        actual.assertThatHasStateAfter(state_after)
        where:
        state_before               | pid | r     || answer          | state_after
        [:]                        | -1  | C(1)  || err(BadRequest) | [:]
        [:]                        | 0   | C(1)  || err(BadRequest) | [:]
        [(X): []]                  | X   | null  || err(BadRequest) | [(X): []]
        [(X): []]                  | X   | C(0)  || err(BadRequest) | [(X): []]
        [(X): []]                  | X   | C(-2) || err(BadRequest) | [(X): []]
        // insert
        [(Y): [A(1)]]              | X   | C(-1) || ok(2)           | [(Y): [A(1)], (X): [C(2)]]
        [(Y): [A(1)], (X): null]   | X   | C(-1) || ok(2)           | [(Y): [A(1)], (X): [C(2)]]
        [(Y): [A(1)], (X): [B(2)]] | X   | C(-1) || ok(3)           | [(Y): [A(1)], (X): [B(2), C(3)]]
        // update
        [(Y): [A(1)]]              | X   | C(1)  || err(NotFound)   | [(Y): [A(1)]]
        [(Y): [A(1)], (X): null]   | X   | C(1)  || err(NotFound)   | [(Y): [A(1)], (X): null]
        [(Y): [A(1)], (X): [B(2)]] | X   | C(1)  || err(NotFound)   | [(Y): [A(1)], (X): [B(2)]]
        [(Y): [A(1)], (X): [B(2)]] | X   | C(2)  || ok(2)           | [(Y): [A(1)], (X): [C(2)]]
        [(X): [A(1), B(2)]]        | X   | C(1)  || ok(1)           | [(X): [C(1), B(2)]]
        [(X): [A(1), B(2)]]        | X   | C(2)  || ok(2)           | [(X): [A(1), C(2)]]
    }

    @Unroll
    def "[#iterationIndex] remove"() {
        when:
        var actual = compute(state_before, service -> service.remove(pid, rid))
        then:
        actual.assertThatHasAnswer(answer)
        actual.assertThatHasStateAfter(state_after)
        where:
        state_before               | pid | rid || answer           | state_after
        [:]                        | -1  | 1   || err(BadRequest)  | [:]
        [:]                        | 0   | 1   || err(BadRequest)  | [:]
        [(X): null]                | X   | -1  || err(BadRequest)  | [(X): null]
        [(X): null]                | X   | 0   || err(BadRequest)  | [(X): null]
        [:]                        | X   | 1   || err(NotFound)    | [:]
        [(X): null]                | X   | 1   || err(NotFound)    | [(X): null]
        [(X): []]                  | X   | 1   || err(NotFound)    | [(X): []]
        [(X): [A(1)]]              | X   | 2   || err(NotFound)    | [(X): [A(1)]]
        [(Y): [A(1)]]              | X   | 1   || err(NotFound)    | [(Y): [A(1)]]
        [(X): [A(1)]]              | X   | 1   || ok(1)            | [(X): []]
        [(X): [A(1), B(2)]]        | X   | 1   || ok(1)            | [(X): [B(2)]]
        [(X): [A(1), B(2)]]        | X   | 2   || ok(2)            | [(X): [A(1)]]
        [(X): [A(1)], (Y): [B(2)]] | X   | 1   || ok(1)            | [(X): [], (Y): [B(2)]]
        [(X): [A(1)], (Y): [B(2)]] | Y   | 2   || ok(2)            | [(X): [A(1)], (Y): []]
    }

    static class Actual<A> {
        Either<String, A> answer
        Map<Integer, List<R>> state_after

        void assertThatHasAnswer(Either<String, Object> expected) {
            assertThat(answer).usingRecursiveComparison().isEqualTo(expected)
        }

        void assertThatHasStateAfter(Map<Integer, List<R>> expected) {
            assertThat(state_after).usingRecursiveComparison().isEqualTo(expected)
        }
    }

    static <A> Actual<A> compute(Map<Integer, List<R>> state_before, Function<Service, A> sideEffect) {
        Sequence sequence = new DummySequence(state_before.values().stream()
                .flatMap((c) -> c.stream()).mapToInt((r) -> r.rid).max().orElse(0))
        Repository repository = new DummyRepository(state_before)
        var answer = unsafe(() -> sideEffect.apply(new Service(sequence, repository)))
        return new Actual<A>(answer: answer, state_after: repository.m)
    }

    static <A> Either<String, A> ok(A a) {
        return Either.right(a);
    }

    static <A> Either<String, A> err(Class<? extends Throwable> cls) {
        return Either.left(cls.simpleName);
    }

    static <A> Either<String, A> unsafe(Supplier<A> effect) {
        return IOEither.fromUnsafe(effect, t -> t.getClass().simpleName).run()
    }

    static class DummySequence implements Sequence {
        private int last
        DummySequence(int start) { this.last = start }
        @Override
        int next() {  return ++this.last }
    }

    static class DummyRepository implements Repository {
        public Map<Integer, List<R>> m
        static List<R> copy(List<R> rs) {
            if (rs == null) return null
            return rs.stream().map {it -> return R.copy(it) }.collect(Collectors.toList())
        }
        DummyRepository(Map<Integer, List<R>> m) {
            this.m = m
        }
        @Override
        P find(int pid) {
            if (!m.containsKey(pid)) {
                return null
            }
            var rs = m.get(pid)
            return new P(pid: pid, rs: rs == null ? rs : copy(rs))
        }
        @Override
        void save(P p) {
            if (m.containsKey(p.pid)) {
                m.put(p.pid, p.rs)
            }
        }
        @Override
        void insert(P p) {
            if (m.containsKey(p.pid)) return
            m.put(p.pid, p.rs)
        }
    }
}

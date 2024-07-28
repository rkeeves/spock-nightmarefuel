package io.github.rkeeves.common;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PUBLIC)
public class IO<A> {
    private final Supplier<A> effect;

    public <B> IO<B> fmap(Function<A, B> f) {
        return of(() -> f.apply(unsafeRun()));
    }

    public <B> IO<B> apply(IO<Function<A, B>> m) {
        return of(() -> {
            var x = unsafeRun();
            var f = m.unsafeRun();
            return f.apply(x);
        });
    }

    public <B> IO<B> flatMap(Function<A, IO<B>> fm) {
        return of(() -> fm.apply(unsafeRun()).unsafeRun());
    }

    public A unsafeRun() {
        return this.effect.get();
    }
}

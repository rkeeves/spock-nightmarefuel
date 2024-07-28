package io.github.rkeeves.common;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PUBLIC)
public final class IOEither<E, A> {

    private final Supplier<Either<E, A>> effect;

    public static <E, A> IOEither<E, A> fromUnsafe(Supplier<A> effect, Function<Throwable, E> f) {
        return new IOEither<>(() -> {
            try {
                return Either.right(effect.get());
            } catch (Throwable t) {
                return Either.left(f.apply(t));
            }
        });
    }

    public static <A> IOEither<Throwable, A> fromUnsafe(Supplier<A> effect) {
        return fromUnsafe(effect, t -> t);
    }

    public static <A> IOEither<Class<? extends Throwable>, A> fromUnsafeWithThrowableClass(Supplier<A> effect) {
        return fromUnsafe(effect, Throwable::getClass);
    }

    public <B> IOEither<E, B> fmap(Function<A, B> f) {
        return of(() -> run().fmap(f));
    }

    public <EE> IOEither<EE, A> mapLeft(Function<E, EE> f) {
        return of(() -> run().mapLeft(f));
    }

    public <B> IOEither<E, B> apply(IOEither<E, Function<A, B>> m) {
        return of(() -> run().flatMap(a -> m.run().fmap(f -> f.apply(a))));
    }

    public <B> IOEither<E, B> flatMap(Function<A, IOEither<E, B>> mf) {
        return of(() -> run().flatMap(a -> mf.apply(a).run()));
    }

    public Either<E, A> run() {
        return this.effect.get();
    }
}

package io.github.rkeeves.common;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

public interface Either<L, R> {

    static <L, R> Either<L, R> right(R r) {
        return new Right<>(r);
    }

    static <L, R> Either<L, R> left(L l) {
        return new Left<>(l);
    }

    <B> B match(Function<L, B> onLeft, Function<R, B> onRight);

    default <RR> Either<L, RR> fmap(Function<R, RR> f) {
        return this.match(Either::left, r -> Either.right(f.apply(r)));
    }

    default <LL> Either<LL, R> mapLeft(Function<L, LL> f) {
        return this.match(l -> Either.left(f.apply(l)), Either::right);
    }

    default <RR> Either<L, RR> apply(Either<L, Function<R, RR>> e) {
        return this.match(Either::left, r -> e.fmap(f -> f.apply(r)));
    }

    default <RR> Either<L, RR> flatMap(Function<R, Either<L, RR>> fe) {
        return this.match(Either::left, fe);
    }

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class Left<L, R> implements Either<L, R> {

        private final L left;

        @Override
        public <B> B match(Function<L, B> onLeft, Function<R, B> onRight) {
            return onLeft.apply(left);
        }

        @Override
        public String toString() {
            return "Left(" + left + ")";
        }
    }

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class Right<L, R> implements Either<L, R> {

        private final R right;

        @Override
        public <B> B match(Function<L, B> onLeft, Function<R, B> onRight) {
            return onRight.apply(right);
        }

        @Override
        public String toString() {
            return "Right(" + right + ")";
        }
    }
}

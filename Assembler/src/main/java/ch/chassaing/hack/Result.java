package ch.chassaing.hack;

import io.vavr.collection.List;
import io.vavr.control.Option;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * A success value cannot be null.
 */
public sealed interface Result<T> {
    @SuppressWarnings("rawtypes")
    Result NONE = new None();

    default boolean isSuccess() {
        return false;
    }

    /**
     * The filter will only be applied to {@link Result.Success}es.
     * If the test does not pass, then a {@link Result.None} will
     * be returned, otherwise the same Success will be returned.
     */
    default Result<T> filter(Predicate<T> test) {
        return this;
    }

    <U> Result<U> flatMap(Function<T, Result<U>> mapper);

    <U> Result<U> map(Function<T, U> mapper);

    /**
     * Flatten the Result to an {@link Option} using the supplied
     * function to resolve {@link Result.Error} cases.
     */
    Option<T> asOption(Function<Result.Error<T>, Option<T>> ifError);

    static <U> Result<U> none() {
        //noinspection unchecked
        return NONE;
    }

    static <U> Result<U> error(String reason) {
        return new Error<>(reason);
    }

    static <U> Result<U> success(U value) {
        return new Success<>(value);
    }

    record None<T>() implements Result<T> {

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            return none();
        }

        @Override
        public <U> Result<U> map(Function<T, U> mapper) {
            requireNonNull(mapper);
            return none();
        }

        @Override
        public Option<T> asOption(Function<Error<T>, Option<T>> ifError) {
            return Option.none();
        }
    }

    record Error<T>(String reason) implements Result<T> {
        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            requireNonNull(mapper);
            return error(reason);
        }

        @Override
        public <U> Result<U> map(Function<T, U> mapper) {
            requireNonNull(mapper);
            return error(reason);
        }

        @Override
        public Option<T> asOption(Function<Error<T>, Option<T>> ifError) {
            requireNonNull(ifError);
            return ifError.apply(this);
        }
    }

    record Success<T>(T value) implements Result<T>
    {
        public Success {
            requireNonNull(value);
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public Result<T> filter(Predicate<T> test) {
            requireNonNull(test);
            return test.test(value) ? this : none();
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            requireNonNull(mapper);
            return mapper.apply(value);
        }

        @Override
        public <U> Result<U> map(Function<T, U> mapper) {
            requireNonNull(mapper);
            return success(mapper.apply(value));
        }

        @Override
        public Option<T> asOption(Function<Error<T>, Option<T>> ifError) {
            return Option.some(value);
        }
    }
}

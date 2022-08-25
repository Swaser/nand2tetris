package ch.chassaing.hack;

import io.vavr.collection.List;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * A success value cannot be null.
 */
public sealed interface Result<T>
    extends Iterable<T>
{
    @SuppressWarnings("rawtypes")
    Result NONE = new None();

    default boolean isSuccess() {return false;}

    default Result<T> filter(Predicate<T> predicate) {return this;}

    <U> Result<U> flatMap(Function<T,Result<U>> mapping);

    <U> Result<U> map(Function<T,U> mapper);

    static <U> Result<U> none()
    {
        //noinspection unchecked
        return NONE;
    }

    static <U> Result<U> error(String reason)
    {
        return new Error<>(reason);
    }

    static <U> Result<U> success(U value)
    {
        return new Success<>(value);
    }

    record None<T>() implements Result<T> {

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapping)
        {
            return none();
        }

        @Override
        public <U> Result<U> map(Function<T, U> mapper)
        {
            return none();
        }

        @Override
        public Iterator<T> iterator()
        {
            return List.<T>empty().iterator();
        }
    }

    record Error<T>(String reason) implements Result<T> {
        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapping)
        {
            return error(reason);
        }

        @Override
        public <U> Result<U> map(Function<T, U> mapper)
        {
            return error(reason);
        }

        @Override
        public Iterator<T> iterator()
        {
            throw new IllegalStateException("iterator() on error Result called");
        }
    }

    record Success<T>(T value) implements Result<T>
    {
        public Success
        {
            requireNonNull(value);
        }

        @Override
        public boolean isSuccess() {return true;}

        // TODO add try-catch

        @Override
        public Result<T> filter(Predicate<T> predicate)
        {
            requireNonNull(predicate);
            return predicate.test(value) ? this : none();
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapping)
        {
            requireNonNull(mapping);
            return mapping.apply(value);
        }

        @Override
        public <U> Result<U> map(Function<T, U> mapper)
        {
            requireNonNull(mapper);
            return success(mapper.apply(value));
        }

        @Override
        public Iterator<T> iterator()
        {
            return Iterators.singleElement(value);
        }
    }
}

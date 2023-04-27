package ch.chassaing.hack;

import io.vavr.collection.List;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class Iterators
{
    private Iterators() {}

    public static <T> Iterator<T> empty() {
        return new Iterator<>()
        {
            @Override
            public boolean hasNext()
            {
                return false;
            }

            @Override
            public T next()
            {
                throw new NoSuchElementException("next() on empty Iterator called");
            }
        };
    }

    public static <T> Iterator<T> singleElement(T element)
    {
        return List.of(element).iterator();
    }
}

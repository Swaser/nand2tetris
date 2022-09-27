package ch.chassaing.hack.vm;

import java.util.Iterator;

public final class Iterators
{
    public static <T> Iterator<T> combine(Iterator<T> first,
                                          Iterator<T> second) {

        return new Iterator<T>()
        {
            @Override
            public boolean hasNext()
            {
                return first.hasNext() || second.hasNext();
            }

            @Override
            public T next()
            {
                return first.hasNext() ? first.next() : second.next();
            }
        };
    }
}

package com.yammer.metrics.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class FilteredIterator<T> implements Iterator<T> {
    private final Iterator<T> iterator;
    private T element;

    public FilteredIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        if (element != null) {
            return true;
        }

        while (iterator.hasNext()) {
            final T possibleElement = iterator.next();
            if (matches(possibleElement)) {
                this.element = possibleElement;
                return true;
            }
        }
        return false;
    }

    protected abstract boolean matches(T possibleElement);

    @Override
    public T next() {
        if (hasNext()) {
            final T e = element;
            this.element = null;
            return e;
        }
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        iterator.remove();
    }
}

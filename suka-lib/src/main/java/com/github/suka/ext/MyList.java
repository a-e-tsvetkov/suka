package com.github.suka.ext;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class MyList<T extends MyList<T>> {
    abstract T getPrev();

    abstract T getThis();

    List<T> toList() {
        List<T> list = Stream.<T>iterate(
                getThis(),
                x -> x.getPrev() != null,
                MyList::getPrev)
                .collect(Collectors.toList());
        Collections.reverse(list);
        return list;
    }
}


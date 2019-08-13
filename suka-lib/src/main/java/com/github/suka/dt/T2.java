package com.github.suka.dt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
public class T2<C1, C2> {
    private final C1 _1;
    private final C2 _2;

    public <C3> T3<C1, C2, C3> and(C3 p3) {
        return T3.of(_1, _2, p3);
    }

    public static <C1, C2, C3> T3<C1, C2, C3> appender(T2<C1, C2> t2, C3 p3) {
        return t2.and(p3);
    }

}


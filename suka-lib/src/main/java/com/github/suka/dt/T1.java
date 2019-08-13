package com.github.suka.dt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
public class T1<C1> {
    private final C1 _1;

    public <C2> T2<C1, C2> and(C2 p2) {
        return T2.of(_1, p2);
    }
}

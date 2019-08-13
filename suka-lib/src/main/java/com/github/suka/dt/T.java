package com.github.suka.dt;

public class T {
    public static <C1> T1<C1> of(C1 p1) {
        return T1.of(p1);
    }

    public static <C1, C2> T2<C1, C2> of(C1 p1, C2 p2) {
        return T2.of(p1, p2);
    }

    public static <C1, C2, C3> T3<C1, C2, C3> of(C1 p1, C2 p2, C3 p3) {
        return T3.of(p1, p2, p3);
    }
}

package com.github.suka.dt;


import org.junit.Test;

class TTest {

    @Test
    void of() {
//        T1<Integer> t1 = T1.of(1);
//        T2<Integer, String> t2 = t1.and("2");
//        T3<Integer, String, Double> t3 = t2.and(3.0);

        var t1 = T1.of(1);
        var t2 = t1.and("2");
        var t3 = t2.and(3.0);
        var t3_alt1 = T1.of(1).and("2").and(3);
        var t3_alt2 = T1.of(1).and("2").and(3);
    }
}
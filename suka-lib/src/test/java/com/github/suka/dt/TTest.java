package com.github.suka.dt;


import org.junit.Test;

public class TTest {

    @Test
    public void of() {
        var t1 = T.of(1);
        var t2 = t1.and("2");
        var t3 = t2.and(3.0);
        var t3_alt1 = T.of(1, "2").and(3);
        var t3_alt2 = T.of(1, "2", 3);
    }
}
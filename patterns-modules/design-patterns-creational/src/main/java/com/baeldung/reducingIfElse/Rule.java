package com.baeldung.reducingifelse;

public interface Rule {

    boolean evaluate(Expression expression);

    Result getResult();
}

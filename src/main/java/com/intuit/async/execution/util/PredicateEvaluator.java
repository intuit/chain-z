package com.intuit.async.execution.util;

import java.util.function.Predicate;


/**
 * @param <T>
 * @author jatin14493
 * Generic PredicateEvaluator class
 */
public class PredicateEvaluator<T> {
    private final Predicate<T> predicate;
    private final T variable;

    public PredicateEvaluator(Predicate<T> predicate, T variable) {
        this.predicate = predicate;
        this.variable = variable;
    }

    public PredicateEvaluator(Predicate<T> predicate) {
        this(predicate, null);
    }

    public boolean evaluate(T value) {
        return predicate.test(value);
    }

    public Predicate<T> getPredicate() {
        return predicate;
    }

    public T getVariable() {
        return variable;
    }
}
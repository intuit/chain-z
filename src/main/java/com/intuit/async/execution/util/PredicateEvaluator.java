package com.intuit.async.execution.util;

import java.util.function.Predicate;


/**
 * @param <T>
 * @author jatin14493
 * Generic PredicateEvaluator class
 */
public class PredicateEvaluator<T> {
    private Predicate<T> predicate;
    // List Types would not be supported and hence used it is as of type Object
    private Object variable;

    public PredicateEvaluator(Predicate<T> predicate, Object variable) {
        this.predicate = predicate;
        this.variable = variable;
    }

    /**
     * @param variable
     * @return Returns true if predicate matches the value
     */
    public boolean evaluate(Object variable) {
        return predicate.test((T) variable) ? Boolean.TRUE.booleanValue() : Boolean.FALSE.booleanValue();
    }

    public Predicate<T> getPredicate() {
        return predicate;
    }

    public Object getVariable() {
        return variable;
    }
}

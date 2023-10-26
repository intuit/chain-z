package com.intuit.async.task;

import com.intuit.async.execution.Task;
import com.intuit.async.execution.request.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * @author Nishant-Sehgal
 */


public class MDCTaskA implements Task {

    private static final Logger log = LoggerFactory.getLogger("MDCTaskA");
    private String mdcKey;

    public MDCTaskA(String mdcKey) {
        this.mdcKey = mdcKey;
    }

    @Override
    public State execute(State inputRequest) {
        log.info("Value from MDC : {} ", MDC.get(mdcKey));
        inputRequest.addValue(mdcKey, MDC.get(mdcKey));
        return inputRequest;
    }
}

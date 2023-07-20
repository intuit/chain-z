package com.intuit.async.task;

import com.intuit.async.execution.Task;
import com.intuit.async.execution.request.State;

import org.slf4j.MDC;

/** @author Nishant-Sehgal */
public class MDCTaskB implements Task {

  private String mdcKey;
  private String mdcVal;

  public MDCTaskB(String mdcKey, String mdcVal) {
    this.mdcKey = mdcKey;
    this.mdcVal = mdcVal;
  }

  @Override
  public State execute(State inputRequest) {
    MDC.put(mdcKey, mdcVal);

    MDC.getCopyOfContextMap()
        .forEach(
            (key, value) -> {
              inputRequest.addValue(key, value);
            });
    return inputRequest;
  }
}

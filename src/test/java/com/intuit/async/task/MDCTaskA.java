package com.intuit.async.task;

import com.intuit.async.execution.Task;
import com.intuit.async.execution.request.State;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;

/** @author Nishant-Sehgal */
@Slf4j
public class MDCTaskA implements Task {

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

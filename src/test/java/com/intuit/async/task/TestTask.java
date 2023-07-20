package com.intuit.async.task;

import com.intuit.async.execution.Task;
import com.intuit.async.execution.request.State;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/** @author Nishant-Sehgal */
@Slf4j
public class TestTask implements Task {

  private String requestKey;
  private String responseKey;
  private String response;

  public TestTask(String requestKey, String responseKey, String response) {
    this.requestKey = requestKey;
    this.responseKey = responseKey;
    this.response = response;
  }

  @Override
  public State execute(State inputRequest) {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // verifies if values set by preceding chain tasks are present or not in the next set of chain
    // tasks
    if ("D".equals(requestKey) || "E".equals(requestKey)) {
      verify(inputRequest.getValue("A-RESPONSE"));
      verify(inputRequest.getValue("B-RESPONSE"));
      verify(inputRequest.getValue("C-RESPONSE"));
    }
    log.info(
        "Request Key :: "
            + inputRequest.getValue(requestKey)
            + "  Thread :: "
            + Thread.currentThread().getName());
    inputRequest.addValue(responseKey, response);
    return inputRequest;
  }

  @Override
  public State onError(State inputRequest) {
    inputRequest.addValue(requestKey + "-ERROR", "Rollback");
    List<String> rollbackSequence = inputRequest.getValue("rollbackSequence");
    if (rollbackSequence == null) {
      rollbackSequence = new ArrayList<>();
    }
    rollbackSequence.add(requestKey);
    inputRequest.addValue("rollbackSequence", rollbackSequence);
    return inputRequest;
  }

  private void verify(Object value) {
    if (null == value) {
      throw new NullPointerException("Value not present");
    }
  }
}

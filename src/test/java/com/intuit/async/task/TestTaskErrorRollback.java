package com.intuit.async.task;

import com.intuit.async.execution.request.State;

import lombok.SneakyThrows;

/** @author ragarwal7 */
public class TestTaskErrorRollback extends TestTask {

  public TestTaskErrorRollback(String requestKey, String responseKey, String response) {
    super(requestKey, responseKey, response);
  }

  @SneakyThrows
  @Override
  public State onError(State inputRequest) {
    throw new Exception("Error during rollback");
  }
}

package com.intuit.async.task;

import com.intuit.async.execution.request.State;

/** @author ragarwal7 */
public class TestTaskErrorRollback extends TestTask {

  public TestTaskErrorRollback(String requestKey, String responseKey, String response) {
    super(requestKey, responseKey, response);
  }

  @Override
  public State onError(State inputRequest) throws Exception {
    throw new Exception("Error during rollback");
  }
}

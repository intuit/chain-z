package com.intuit.async.task;

import com.intuit.async.execution.Task;
import com.intuit.async.execution.impl.ChainConstantTest;
import com.intuit.async.execution.request.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Nishant-Sehgal */
public class DummyTaskWithExceptionTest implements Task {

  private static final Logger log = LoggerFactory.getLogger("DummyTaskWithExceptionTest");

  private String key;

  public DummyTaskWithExceptionTest(String key) {
    this.key = key;
  }

  @Override
  public State execute(State inputRequest) {
    Map<String, List<Integer>> req = inputRequest.getValue(ChainConstantTest.DUMMYTASKREQ);
    if (key.equals("A")) {
      int val = 1 / 0;
      log.debug(String.valueOf(val));
    }

    log.info(
        "Executed Task for request :: "
            + req.get(key)
            + " Thread "
            + Thread.currentThread().getName());
    State state = new State();
    state.addValue("RESPONSE" + "-" + key, "DONE" + "-" + key);
    return state;
  }

  @Override
  public State onError(State inputRequest) {
    inputRequest.addValue(key + "-ERROR", "Rollback");

    List<String> rollbackSequence = new ArrayList<>();
    rollbackSequence.add(key);
    inputRequest.addValue("rollbackSequence", rollbackSequence);
    return inputRequest;
  }
}

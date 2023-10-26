package com.intuit.async.task;

import com.intuit.async.execution.Task;
import com.intuit.async.execution.impl.ChainConstantTest;
import com.intuit.async.execution.request.State;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Nishant-Sehgal */
public class DummyTaskTest implements Task {

  private static final Logger log = LoggerFactory.getLogger("DummyTaskTest");

  private String key;

  public DummyTaskTest(String key) {
    this.key = key;
  }

  @Override
  public State execute(State inputRequest) {
    Map<String, List<Integer>> req = inputRequest.getValue(ChainConstantTest.DUMMYTASKREQ);
  
    log.info(
        "Executed Task for request :: "
            + req.get(key)
            + " Thread "
            + Thread.currentThread().getName());
    State state = new State();
    state.addValue("RESPONSE" + "-" + key, "DONE" + "-" + key);
    return state;
  }
}

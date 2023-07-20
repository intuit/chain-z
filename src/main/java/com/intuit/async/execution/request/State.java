package com.intuit.async.execution.request;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * State that manages the request and response for all the task in Execution Chain.
 *
 * @author Nishant-Sehgal
 */
public class State {

  /**
   * since State is shared in multiple child threads in order to avoid any concurrency issue due to
   * any task changing value in each other keys.Ideally that should not happen but just to be safe.
   */
  private Map<String, Object> state = new ConcurrentHashMap<>();
  private Throwable error;
  private Integer exceptionTaskIndex;

  /**
   * @param key input state key
   * @return the state value for given key
   */
  @SuppressWarnings("unchecked")
  public <T> T getValue(String key) {
    if (isNull(key)) {
      return null;
    }
    return (T) state.get(key);
  }

  /** @return all the state details */
  public Map<String, Object> getAll() {
    return state;
  }

  /**
   * populate the given req and value in the state.
   *
   * @param key input state key
   * @param value input state value
   */
  public <T> void addValue(String key, T value) {
    if (nonNull(key) && nonNull(value)) {
      state.put(key, value);
    }
  }

  /**
   * copy the details of all input state object
   *
   * @param inputState input state object
   */
  public void addAll(State inputState) {
    state.putAll(inputState.getAll());
  }

  public void addError(Throwable e, Integer taskIndex) {
    error = e;
    exceptionTaskIndex = taskIndex;
  }

  public Throwable getError() {
    return error;
  }

  public Integer getExceptionTaskIndex() {
    return exceptionTaskIndex;
  }
}

package com.intuit.async.execution;

import com.intuit.async.execution.request.State;

/** @author Nishant-Sehgal */
public interface Task {

  /**
   * by default all tasks will be fatal i.e if a fatal task fails chain next to the current chain
   * will not execute.<br>
   * But if u have a mix of fatal and non fatal task in a chain for example.
   * .next(FatalTaskA,NonFatalTaskA,FatalTaskB).chain(.,.,.) - next chain will not execute if there
   * is any error in FatalTaskA,FatalTaskB but if there is any error in NonFatalTaskA it will not
   * stop next chain from executing.<br>
   * Note:every task in a given chain will always execute, exception in one task in a given chain
   * will not effect other task in the same chain from executing, the chain that is currently
   * running will execute and complete all the tasks in the current chain, fatal changes will be
   * applied post execution of all task in a chain.
   *
   * @return
   */
  default boolean isFatal() {
    return true;
  }

  /**
   * execute the current task implementation.
   *
   * @param inputRequest {@link State} input state request
   * @return the final state after the current executing task
   */
  State execute(State inputRequest) throws Exception;

  /**
   * This method gets invoked in case an error occur in the chain.
   * This method can be used to execute rollback steps which will be executed in the reverse order.
   *
   * @param inputRequest {@link State} input state request
   * @return the final state after the current executing task
   */
  default State onError(State inputRequest) throws Exception {
    return inputRequest;
  }
}

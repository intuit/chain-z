package com.intuit.async.execution;

import com.intuit.async.execution.config.ExecutionChainConfiguration;
import com.intuit.async.execution.config.ExecutionChainConfiguration.ExecutionChainConfigBuilder;
import com.intuit.async.execution.request.State;

/**
 * @author Nishant-Sehgal
 *     <p>Execution chain client
 */
public interface ExecutionChainClient {

  /**
   * Creates a fluent builder to configure the Execution Chain client
   *
   * @return builder to apply configurations on
   */
  static ExecutionChainConfigBuilder create() {
    return ExecutionChainConfiguration.builder();
  }

  /**
   * get the Execution chain
   *
   * @param inputRequest input state request for Execution chain
   * @return Execution chain
   */
  Chain getExecutionChain(State inputRequest);

  /**
   * get the Execution chain
   *
   * @param inputRequest input state request for Execution chain
   * @param task chain of task
   * @return Execution chain
   */
  Chain getExecutionChain(State inputRequest, Task... task);

  /** clean up any resources */
  void clean();
}

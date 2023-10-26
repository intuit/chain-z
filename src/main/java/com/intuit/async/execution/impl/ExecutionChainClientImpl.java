package com.intuit.async.execution.impl;

import static com.intuit.async.execution.util.ChainHelper.getExecutor;

import com.intuit.async.execution.Chain;
import com.intuit.async.execution.ExecutionChainClient;
import com.intuit.async.execution.Task;
import com.intuit.async.execution.config.ExecutionChainConfiguration;
import com.intuit.async.execution.request.State;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/** @author Nishant-Sehgal */
public class ExecutionChainClientImpl implements ExecutionChainClient {

  private final ExecutionChainConfiguration executionChainConfig;

  @Override
  public Chain getExecutionChain(State inputRequest) {
    return new RxExecutionChain(inputRequest, executionChainConfig);
  }

  @Override
  public Chain getExecutionChain(State inputRequest, Task... task) {
    return new RxExecutionChain(inputRequest, executionChainConfig, task);
  }

  @Override
  public void clean() {
    Optional<ExecutorService> executorOpt = getExecutor(executionChainConfig);
    executorOpt.ifPresent(ExecutorService::shutdown);
  }

  public ExecutionChainConfiguration getExecutionChainConfig() {
    return executionChainConfig;
  }

  public ExecutionChainClientImpl(ExecutionChainConfiguration executionChainConfig) {
    this.executionChainConfig = executionChainConfig;
  }
}

package com.intuit.async.execution.config;

import com.intuit.async.execution.ExecutionChainClient;
import com.intuit.async.execution.impl.ExecutionChainClientImpl;

import java.util.concurrent.ExecutorService;

/**
 * @author Nishant-Sehgal
 *     <p>Configuration for execution chain.Can configure log time of tasks and custom thread pool
 *     executor.
 */
public class ExecutionChainConfiguration {

  private ExecutorService executor;
  private boolean logTime;

  public static ExecutionChainConfigBuilder builder() {
    return new ExecutionChainConfigBuilder();
  }

  public ExecutionChainConfiguration(ExecutionChainConfigBuilder builder) {
    this.executor = builder.executor;
    this.logTime = builder.logTime;
  }

  public static class ExecutionChainConfigBuilder {

    private ExecutorService executor;
    private boolean logTime;

    public ExecutionChainConfigBuilder executor(ExecutorService executor) {
      this.executor = executor;
      return this;
    }

    public ExecutionChainConfigBuilder logTime(boolean logTime) {
      this.logTime = logTime;
      return this;
    }

    public ExecutionChainClient build() {
      return new ExecutionChainClientImpl(new ExecutionChainConfiguration(this));
    }
  }

  public ExecutorService getExecutor() {
    return executor;
  }

  public boolean isLogTime() {
    return logTime;
  }
}

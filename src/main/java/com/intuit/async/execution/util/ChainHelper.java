package com.intuit.async.execution.util;

import static java.util.Objects.nonNull;

import com.intuit.async.execution.config.ExecutionChainConfiguration;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Nishant-Sehgal
 *     <p>Contains helper methods for execution chain
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChainHelper {

  /**
   * @param executionChainConfig input execution chain configuration
   * @return Optional of Executor implementation from input configuration
   */
  public static Optional<ExecutorService> getExecutor(
      ExecutionChainConfiguration executionChainConfig) {
    return Optional.ofNullable(executionChainConfig).map(executor -> executor.getExecutor());
  }
  /**
   * By default uses {@link Schedulers} else create Scheduler from the input configuration by
   * client
   *
   * @param executionChainConfig input execution chain configuration
   * @return the Scheduler to be used by chain for execution task
   */
  public static Scheduler getScheduler(ExecutionChainConfiguration executionChainConfig) {
    Optional<ExecutorService> executorOpt = getExecutor(executionChainConfig);
    if (executorOpt.isPresent()) {
      return Schedulers.from(executorOpt.get());
    }
    return Schedulers.io();
  }

  /**
   * @param executionChainConfig input execution chain configuration
   * @return true/false if logging of time is enabled for the tasks
   */
  public static boolean logTime(ExecutionChainConfiguration executionChainConfig) {
    return nonNull(executionChainConfig) && executionChainConfig.isLogTime();
  }
}

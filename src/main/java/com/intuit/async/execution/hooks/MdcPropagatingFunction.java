package com.intuit.async.execution.hooks;

import io.reactivex.functions.Function;

/**
 * @author Nishant-Sehgal
 *     <p>Propagates the MDC to child threads by calling {@link MdcPropagatingRunnable}
 */
public class MdcPropagatingFunction implements Function<Runnable, Runnable> {
  @Override
  public Runnable apply(Runnable runnable) {
    return new MdcPropagatingRunnable(runnable);
  }
}

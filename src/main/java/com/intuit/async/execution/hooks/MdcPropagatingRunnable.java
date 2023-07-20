package com.intuit.async.execution.hooks;

import java.util.Map;

import org.slf4j.MDC;

/**
 * Copies MDC from Parent thread to child thread
 *
 * @author Nishant-Sehgal
 */
public class MdcPropagatingRunnable implements Runnable {
  private final Runnable runnable;
  private final Map<String, String> context;

  /**
   * Decorates an {@link Runnable} so that it executes with the current {@link MDC} as its context.
   *
   * @param runnable the {@link Runnable} to decorate.
   */
  public MdcPropagatingRunnable(final Runnable runnable) {
    this.runnable = runnable;
    this.context = MDC.getCopyOfContextMap();
  }

  @Override
  public void run() {
    if (context != null) {
      MDC.setContextMap(context);
    }
    try {
      this.runnable.run();
    } finally {
      MDC.clear();
    }
  }
}

package com.intuit.async.execution.impl;

import com.intuit.async.execution.config.ExecutionChainConfiguration;
import com.intuit.async.execution.util.ChainHelper;

import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/** @author Nishant-Sehgal */
public class ChainHelperTest {

  private ExecutionChainConfiguration executionChainConfig =
          Mockito.mock(ExecutionChainConfiguration.class);

  @Test
  public void testlogTimeFalse() {
    Assert.assertFalse(ChainHelper.logTime(null));
  }

  @Test
  public void testlogTimeTrue() {
    Mockito.when(executionChainConfig.isLogTime()).thenReturn(true);
    Assert.assertTrue(ChainHelper.logTime(executionChainConfig));
  }

  @Test
  public void testgetExecutorNotSet() {
    Assert.assertFalse(ChainHelper.getExecutor(null).isPresent());
  }

  @Test
  public void testgetExecutor() {
    Mockito.when(executionChainConfig.getExecutor()).thenReturn(Executors.newFixedThreadPool(1));
    Assert.assertTrue(ChainHelper.getExecutor(executionChainConfig).isPresent());
  }

  @Test
  public void tesGettSchedulerDefault() {
    Assert.assertNotNull(ChainHelper.getScheduler(null));
  }

  @Test
  public void tesGettScheduler() {
    Mockito.when(executionChainConfig.getExecutor()).thenReturn(Executors.newFixedThreadPool(1));
    Assert.assertNotNull(ChainHelper.getScheduler(executionChainConfig));
  }

  @Test
  public void tesGettSchedulers() {
    Mockito.when(executionChainConfig.isLogTime()).thenReturn(false);
    Assert.assertFalse(ChainHelper.logTime(executionChainConfig));
  }
}


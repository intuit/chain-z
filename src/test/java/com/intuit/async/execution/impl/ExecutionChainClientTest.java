package com.intuit.async.execution.impl;

import com.intuit.async.execution.ExecutionChainClient;
import com.intuit.async.execution.Task;
import com.intuit.async.execution.hooks.MdcPropagatingFunction;
import com.intuit.async.execution.request.State;
import com.intuit.async.task.DummyTaskTest;
import com.intuit.async.task.DummyTaskWithExceptionTest;
import com.intuit.async.task.MDCTaskB;
import com.intuit.async.task.TaskA;
import com.intuit.async.task.TaskB;
import com.intuit.async.task.TaskC;
import com.intuit.async.task.TaskD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.reactivex.plugins.RxJavaPlugins;

/** @author Nishant-Sehgal */
public class ExecutionChainClientTest {

  private List<Integer> listA = new ArrayList<>();

  private List<Integer> listB = new ArrayList<>();


  @Before
  public void init() {
    listA.add(1);
    listA.add(2);
    listB.add(3);
    listB.add(4);
  }


  @Test
  public void testWithDefaultSchedulerAndLogTime() {
    ExecutionChainClient executionChainClient = ExecutionChainClient.create().logTime(true).build();

    State req = new State();
    Map<String, List<Integer>> map = new HashMap<>();

    map.put("A", listA);
    map.put("B", listB);

    List<Integer> listC = new ArrayList<>();
    listC.add(5);
    listC.add(6);

    map.put("C", listC);
    req.addValue(ChainConstantTest.DUMMYTASKREQ, map);

    List<Task> list = new ArrayList<>();

    map.forEach(
        (key, value) -> {
          list.add(new DummyTaskTest(key));
        });

    State response =
        executionChainClient
            .getExecutionChain(req)
            .next(list.toArray(new Task[0]))
            .execute();
    Assert.assertEquals("DONE-A", response.getValue("RESPONSE-A"));
    Assert.assertEquals("DONE-B", response.getValue("RESPONSE-B"));
    Assert.assertEquals("DONE-C", response.getValue("RESPONSE-C"));
  }

  @Test(expected = ArithmeticException.class)
  public void testWithDefaultSchedulerAndLogTimeWithException() {
    ExecutionChainClient executionChainClient = ExecutionChainClient.create().logTime(true).build();
    State req = new State();
    Map<String, List<Integer>> map = new HashMap<>();

    map.put("A", listA);
    map.put("B", listB);

    List<Integer> listC = new ArrayList<>();
    listC.add(5);
    listC.add(6);

    map.put("C", listC);
    req.addValue(ChainConstantTest.DUMMYTASKREQ, map);

    List<Task> list = new ArrayList<>();

    map.forEach(
        (key, value) -> {
          list.add(new DummyTaskWithExceptionTest(key));
        });

    executionChainClient
        .getExecutionChain(req)
        .next(list.toArray(new Task[0]))
        .next(new DummyTaskWithExceptionTest("A"))
        .execute();
  }

  @Test(expected = ArithmeticException.class)
  public void testWithDefaultSchedulerAndLogTimeWithExceptionAndNoNext() {
    ExecutionChainClient executionChainClient = ExecutionChainClient.create().logTime(true).build();
    State req = new State();
    Map<String, List<Integer>> map = new HashMap<>();

    map.put("A", listA);
    map.put("B", listB);

    List<Integer> listC = new ArrayList<>();
    listC.add(5);
    listC.add(6);

    map.put("C", listC);
    req.addValue(ChainConstantTest.DUMMYTASKREQ, map);

    List<Task> list = new ArrayList<>();

    map.forEach(
        (key, value) -> {
          list.add(new DummyTaskWithExceptionTest(key));
        });

    executionChainClient.getExecutionChain(req).next(list.toArray(new Task[0])).execute();
  }

  @Test
  public void testWithCustomExecutor() {
    ExecutionChainClient executionChainClient =
        ExecutionChainClient.create()
            .logTime(true)
            .executor(Executors.newFixedThreadPool(2))
            .build();
    State req = new State();
    req.addValue("A", 1);
    req.addValue("B", 2);
    req.addValue("C", 3);
    req.addValue("D", 4);

    TaskA taska = new TaskA("A", "A-RESPONSE", "DONE-A");
    TaskB taskb = new TaskB("B", "B-RESPONSE", "DONE-B");
    TaskC taskc = new TaskC("C", "C-RESPONSE", "DONE-C");
    TaskD taskd = new TaskD("D", "D-RESPONSE", "DONE-D");

    State response =
        executionChainClient.getExecutionChain(req, taska, taskb, taskc).next(taskd).execute();
    executionChainClient.clean();
    Assert.assertEquals("DONE-A", response.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", response.getValue("B-RESPONSE"));
    Assert.assertEquals("DONE-C", response.getValue("C-RESPONSE"));
    Assert.assertEquals("DONE-D", response.getValue("D-RESPONSE"));
  }

  @Test
  public void testWithCustomExecutorAsync() {
    ExecutionChainClient executionChainClient =
        ExecutionChainClient.create()
            .logTime(true)
            .executor(Executors.newFixedThreadPool(2))
            .build();
    State req = new State();
    req.addValue("A", 1);
    req.addValue("B", 2);
    req.addValue("C", 3);
    req.addValue("D", 4);

    TaskA taska = new TaskA("A", "A-RESPONSE", "DONE-A");
    TaskB taskb = new TaskB("B", "B-RESPONSE", "DONE-B");
    TaskC taskc = new TaskC("C", "C-RESPONSE", "DONE-C");
    TaskD taskd = new TaskD("D", "D-RESPONSE", "DONE-D");

    try {
      executionChainClient.getExecutionChain(req, taska, taskb, taskc).next(taskd).executeAsync();
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  public void testThreadReuse() {
    // to copy MDC to child threads in rx java context
    RxJavaPlugins.setScheduleHandler(new MdcPropagatingFunction());

    ExecutionChainClient executionChainClient =
        ExecutionChainClient.create()
            .logTime(true)
            .executor(Executors.newFixedThreadPool(1))
            .build();

    String mdcKey = "MDC_KEY1";
    String mdcVal = "MDC_VAL1";

    State responseA =
        executionChainClient.getExecutionChain(new State(), new MDCTaskB(mdcKey, mdcVal)).execute();

    String mdcKey1 = "MDC_KEY2";
    String mdcVal1 = "MDC_VAL2";

    State responseB =
        executionChainClient
            .getExecutionChain(new State(), new MDCTaskB(mdcKey1, mdcVal1))
            .execute();

    Assert.assertNotNull(responseA.getValue(mdcKey));

    // mdc values getting cleaned
    Assert.assertNull(responseB.getValue(mdcKey));
    Assert.assertNotNull(responseB.getValue(mdcKey1));
  }

  @Test
  public void testThreadReuseNotSettingMDCPropogation() {

    ExecutionChainClient executionChainClient =
        ExecutionChainClient.create()
            .logTime(true)
            .executor(Executors.newFixedThreadPool(1))
            .build();

    String mdcKey = "MDC_KEY1";
    String mdcVal = "MDC_VAL1";

    State responseA =
        executionChainClient.getExecutionChain(new State(), new MDCTaskB(mdcKey, mdcVal)).execute();

    String mdcKey1 = "MDC_KEY2";
    String mdcVal1 = "MDC_VAL2";

    State responseB =
        executionChainClient
            .getExecutionChain(new State(), new MDCTaskB(mdcKey1, mdcVal1))
            .execute();

    Assert.assertNotNull(responseA.getValue(mdcKey));

    // mdc values getting retained
    Assert.assertNotNull(responseB.getValue(mdcKey));
    Assert.assertNotNull(responseB.getValue(mdcKey1));
  }
}

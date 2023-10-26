package com.intuit.async.execution.impl;

import com.intuit.async.execution.Task;
import com.intuit.async.execution.hooks.MdcPropagatingFunction;
import com.intuit.async.execution.request.State;
import com.intuit.async.execution.util.PredicateEvaluator;
import com.intuit.async.task.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

import io.reactivex.observers.TestObserver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import io.reactivex.plugins.RxJavaPlugins;

/** @author Nishant-Sehgal */
public class RxExecutionChainTest {

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
  public void test() {
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

    State resp = new RxExecutionChain(req, list.toArray(new Task[0])).execute();

    Assert.assertEquals("DONE-A", resp.getValue("RESPONSE-A"));
    Assert.assertEquals("DONE-B", resp.getValue("RESPONSE-B"));
    Assert.assertEquals("DONE-C", resp.getValue("RESPONSE-C"));
  }

  @Test
  public void testEmptyTask() {
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


    State resp = new RxExecutionChain(req, list.toArray(new Task[0])).execute();

    Assert.assertNull(resp.getValue("RESPONSE-A"));
  }

  @Test
  public void testExecuteAsyncWithObv() {

    State states = new State();
    states.addValue("A", 1);
    states.addValue("B", 2);
    states.addValue("C", 3);
    states.addValue("D", 4);
    states.addValue("E", 5);

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    TestTask task2 = new TestTask("C", "C-RESPONSE", "DONE-C");

    TestObserver<State> testObserver = new TestObserver<>();

    RxExecutionChain rxExecutionChain = new RxExecutionChain(states, task, task1, task2);
    rxExecutionChain.executeAsyncWithObv().subscribe(testObserver);
    testObserver.assertNoErrors();
  }

  @Test(expected = ArithmeticException.class)
  public void testTaskWithException() {
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

    new RxExecutionChain(req, list.toArray(new Task[list.size()]))
        .next(new DummyTaskWithExceptionTest("A"))
        .execute();
  }

  @Test
  public void testTaskWithExceptionErrorNonFatal() {
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
          list.add(new DummyTaskWithNonFatalExceptionTest(key));
        });

    State resp = new RxExecutionChain(req, list.toArray(new Task[list.size()])).execute();

    // task A and C have failed but since it is non fatal it will not stop the chain

    // should contains success tasks result
    Assert.assertEquals("DONE-B", resp.getValue("RESPONSE-B"));
  }

  @Test
  public void testNormalTaskForMultithread() {

    for (int i = 0; i < 10; i++) {
      State req = new State();
      req.addValue("A", 1);
      req.addValue("B", 2);
      req.addValue("C", 3);
      req.addValue("D", 4);
      req.addValue("E", 5);

      TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
      TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
      TestTask task2 = new TestTask("C", "C-RESPONSE", "DONE-C");
      TestTask task3 = new TestTask("D", "D-RESPONSE", "DONE-D");
      TestTask task4 = new TestTask("E", "E-RESPONSE", "DONE-E");

      State resp = new RxExecutionChain(req, task, task1, task2).next(task3, task4).execute();

      Assert.assertEquals("DONE-A", resp.getValue("A-RESPONSE"));
      Assert.assertEquals("DONE-B", resp.getValue("B-RESPONSE"));
      Assert.assertEquals("DONE-C", resp.getValue("C-RESPONSE"));
      Assert.assertEquals("DONE-D", resp.getValue("D-RESPONSE"));
      Assert.assertEquals("DONE-E", resp.getValue("E-RESPONSE"));
    }
  }

  @Test
  public void testNormalTaskAsync() {

    State req = new State();
    req.addValue("A", 1);
    req.addValue("B", 2);
    req.addValue("C", 3);
    req.addValue("D", 4);

    TaskA task = new TaskA("A", "A-RESPONSE", "DONE-A");
    TaskB taska1 = new TaskB("B", "B-RESPONSE", "DONE-B");
    TaskC task1 = new TaskC("C", "C-RESPONSE", "DONE-C");
    TaskD task2 = new TaskD("D", "D-RESPONSE", "DONE-D");

    try {
      new RxExecutionChain(req, task, taska1).next(task1).next(task2).executeAsync();
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  public void testNormalTaskAsyncOneTask() {

    State req = new State();
    req.addValue("A", 1);

    TaskA task = new TaskA("A", "A-RESPONSE", "DONE-A");

    try {
      new RxExecutionChain(req, task).executeAsync();
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  public void testNormalTaskAsyncNoTask() {

    State req = new State();
    req.addValue("A", 1);

    try {
      new RxExecutionChain(req).executeAsync();
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  public void testNormalTaskOneTask() {

    State req = new State();
    req.addValue("A", 1);

    TaskA task = new TaskA("A", "A-RESPONSE", "DONE-A");

    State resp = new RxExecutionChain(req, task).execute();

    Assert.assertEquals("DONE-A", resp.getValue("A-RESPONSE"));
    Assert.assertEquals(2, resp.getAll().size());
  }

  @Test
  public void testNormalTaskNoTask() {

    State req = new State();
    req.addValue("A", 1);

    State resp = new RxExecutionChain(req).execute();
    Assert.assertEquals(1, resp.getAll().size());
  }

  @Test
  public void testMDCValInTasks() {
    State req = new State();
    req.addValue("A", 1);
    String mdcKey = "MDC_KEY1";
    String mdcVal = "MDC_VAL2";
    MDC.put(mdcKey, mdcVal);

    // to copy MDC to child threads in rx java context
    RxJavaPlugins.setScheduleHandler(new MdcPropagatingFunction());

    State resp = new RxExecutionChain(req).next(new MDCTaskA(mdcKey)).execute();
    Assert.assertNotNull(resp.getValue(mdcKey));
    Assert.assertEquals(mdcVal, resp.getValue(mdcKey));
  }

  @Test
  public void testTaskWithExceptionRollback() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    state.addValue("C", 3);
    state.addValue("D", 4);
    state.addValue("E", 5);

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    TestTask task2 = new TestTask("C", "C-RESPONSE", "DONE-C");
    TestTask task3 = new TestTask("D", "D-RESPONSE", "DONE-D");
    TestTask task4 = new TestTask("E", "E-RESPONSE", "DONE-E");

    try {
      state = new RxExecutionChain(state, task, task1, task2)
              .next(task3, task4)
              .next(new DummyTaskWithExceptionTest("F"))
              .executeWithRollBack();

    } catch (Exception e) {}

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));
    Assert.assertEquals("DONE-C", state.getValue("C-RESPONSE"));
    Assert.assertEquals("DONE-D", state.getValue("D-RESPONSE"));
    Assert.assertEquals("DONE-E", state.getValue("E-RESPONSE"));

    Assert.assertEquals("Rollback", state.getValue("A-ERROR"));
    Assert.assertEquals("Rollback", state.getValue("B-ERROR"));
    Assert.assertEquals("Rollback", state.getValue("C-ERROR"));
    Assert.assertEquals("Rollback", state.getValue("D-ERROR"));
    Assert.assertEquals("Rollback", state.getValue("E-ERROR"));
    Assert.assertEquals("Rollback", state.getValue("F-ERROR"));

    Assert.assertEquals(3, (int) state.getExceptionTaskIndex());
    Assert.assertNotNull(state.getError());
  }

  @Test
  public void testTaskWithExceptionRollbackSequence() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    state.addValue("D", 3);

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    TestTask task2 = new TestTask("D", "D-RESPONSE", "DONE-D");

    try {
      state = new RxExecutionChain(state, task)
              .next(task1)
              .next(new DummyTaskWithExceptionTest("C"))
              .next(task2)
              .executeWithRollBack();

    } catch (Exception e) {}

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));
    Assert.assertNull(state.getValue("D-RESPONSE"));

    Assert.assertEquals("Rollback", state.getValue("A-ERROR"));
    Assert.assertEquals("Rollback", state.getValue("B-ERROR"));
    Assert.assertEquals("Rollback", state.getValue("C-ERROR"));
    Assert.assertNull(state.getValue("D-ERROR"));

    Assert.assertEquals(3, (int) state.getExceptionTaskIndex());
    Assert.assertNotNull(state.getError());

    List<String> rollbackSequence = new ArrayList<>();
    rollbackSequence.add("C");
    rollbackSequence.add("B");
    rollbackSequence.add("A");
    Assert.assertEquals(rollbackSequence, state.getValue("rollbackSequence"));
  }

  @Test
  public void testTaskWithParallelTaskExceptionRollback() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    state.addValue("D", 3);

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    TestTask task2 = new TestTask("D", "D-RESPONSE", "DONE-D");

    try {
      state = new RxExecutionChain(state, task)
              .next(new DummyTaskWithExceptionTest("C"), task1)
              .next(task2)
              .executeWithRollBack();
    } catch (Exception e) {}

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));
    Assert.assertNull(state.getValue("D-RESPONSE"));

    Assert.assertEquals("Rollback", state.getValue("A-ERROR"));
    Assert.assertEquals("Rollback", state.getValue("B-ERROR"));
    Assert.assertEquals("Rollback", state.getValue("C-ERROR"));
    Assert.assertNull(state.getValue("D-ERROR"));

    Assert.assertEquals(2, (int) state.getExceptionTaskIndex());
    Assert.assertNotNull(state.getError());
  }

  @Test
  public void testTaskExceptionInRollback() {
    //
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    state.addValue("D", 3);

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTaskErrorRollback task1 = new TestTaskErrorRollback("B", "B-RESPONSE", "DONE-B");
    TestTask task2 = new TestTask("D", "D-RESPONSE", "DONE-D");

    try {
      state = new RxExecutionChain(state, task)
              .next(new DummyTaskWithExceptionTest("C"), task1)
              .next(task2)
              .executeWithRollBack();
    } catch (Exception e) {}

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));
    Assert.assertNull(state.getValue("D-RESPONSE"));

    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertEquals("Rollback", state.getValue("C-ERROR"));
    Assert.assertNull(state.getValue("D-ERROR"));

    Assert.assertEquals(2, (int) state.getExceptionTaskIndex());
    Assert.assertNotNull(state.getError());
  }

  @Test
  public void testTaskRollbackSkipForNonFatal() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");

    try {
      state = new RxExecutionChain(state, task)
              .next(new DummyTaskWithNonFatalExceptionTest("C"))
              .next(task1)
              .executeWithRollBack();
    } catch (Exception e) {}

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));

    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertNull(state.getValue("C-ERROR"));

    Assert.assertNull(state.getExceptionTaskIndex());
    Assert.assertNull(state.getError());
  }

  @Test
  public void testWithListPredicate() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    List<String> value = new ArrayList<>();
    value.add("test String"); // Evaluating Size Predicate

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    PredicateEvaluator<List> predicateEvaluator = new PredicateEvaluator<>((s -> value.size() > 0), value);

    List<Pair<Task, PredicateEvaluator<List>>> values = new ArrayList<>();
    values.add(Pair.of(task1, predicateEvaluator));

    try {
      state = new RxExecutionChain(state, task)
              .next(values)
              .execute();
    } catch (Exception e) {
    }

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));

    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertNull(state.getValue("C-ERROR"));

    Assert.assertNull(state.getExceptionTaskIndex());
    Assert.assertNull(state.getError());
  }

  @Test
  public void testWithListPredicateFalse() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    List<String> value = new ArrayList<>();
    value.add("test String"); // Evaluating Size Predicate

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    PredicateEvaluator<List> predicateEvaluator = new PredicateEvaluator<>((s -> value.size() > 10), value);

    List<Pair<Task, PredicateEvaluator<List>>> values = new ArrayList<>();
    values.add(Pair.of(task1, predicateEvaluator));

    try {
      state = new RxExecutionChain(state, task)
              .next(values)
              .execute();
    } catch (Exception e) {
    }

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertNull(state.getValue("B-RESPONSE")); // Predicate didn't match

    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertNull(state.getValue("C-ERROR"));

    Assert.assertNull(state.getExceptionTaskIndex());
    Assert.assertNull(state.getError());
  }

  @Test
  public void testWithStringSizePredicate() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    String value = "test String"; // Evaluating Size Predicate

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    PredicateEvaluator<String> predicateEvaluator = new PredicateEvaluator<>((s -> value.length() > 5), value);

    List<Pair<Task, PredicateEvaluator<String>>> values = new ArrayList<>();
    values.add(Pair.of(task1, predicateEvaluator));

    try {
      state = new RxExecutionChain(state, task)
              .next(values)
              .execute();
    } catch (Exception e) {
    }

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));

    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertNull(state.getValue("C-ERROR"));

    Assert.assertNull(state.getExceptionTaskIndex());
    Assert.assertNull(state.getError());
  }

  @Test
  public void testWithStringSizePredicateMultipleAllPassing() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    String value = "test String"; // Evaluating Size Predicate

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    PredicateEvaluator<String> predicateEvaluator = new PredicateEvaluator<>((s -> value.length() > 5), value);
    PredicateEvaluator<String> predicateEvaluator2 = new PredicateEvaluator<>((s -> value.contains("test")), value);

    List<Pair<Task, PredicateEvaluator<String>>> values = new ArrayList<>();
    values.add(Pair.of(task, predicateEvaluator));
    values.add(Pair.of(task1, predicateEvaluator2));

    try {
      state = new RxExecutionChain(state, task)
              .next(values)
              .execute();
    } catch (Exception e) {
    }

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));

    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertNull(state.getValue("C-ERROR"));

    Assert.assertNull(state.getExceptionTaskIndex());
    Assert.assertNull(state.getError());
  }

  @Test
  public void testWithIntegerPredicate() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    Integer value = 10; // Evaluating Size Predicate
    System.out.println(value);

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    PredicateEvaluator<Integer> predicateEvaluator = new PredicateEvaluator<>((s -> value > 5), value);

    List<Pair<Task, PredicateEvaluator<Integer>>> values = new ArrayList<>();
    values.add(Pair.of(task1, predicateEvaluator));

    try {
      state = new RxExecutionChain(state, task)
              .next(values)
              .execute();
    } catch (Exception e) {
    }

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));

    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertNull(state.getValue("C-ERROR"));

    Assert.assertNull(state.getExceptionTaskIndex());
    Assert.assertNull(state.getError());
  }

  @Test
  public void testWithLongPredicate() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    Long value = 10L; // Evaluating Size Predicate

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    PredicateEvaluator<Long> predicateEvaluator = new PredicateEvaluator<>((s -> value > 5), value);

    List<Pair<Task, PredicateEvaluator<Long>>> values = new ArrayList<>();
    values.add(Pair.of(task1, predicateEvaluator));

    try {
      state = new RxExecutionChain(state, task)
              .next(values)
              .execute();
    } catch (Exception e) {
    }

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));

    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertNull(state.getValue("C-ERROR"));

    Assert.assertNull(state.getExceptionTaskIndex());
    Assert.assertNull(state.getError());
  }

  @Test // Failure Case
  public void testWithLongPredicateFailure() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    Long value = 2L; // Evaluating Size Predicate

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    PredicateEvaluator<Long> predicateEvaluator = new PredicateEvaluator<>((s -> value > 5), value);

    List<Pair<Task, PredicateEvaluator<Long>>> values = new ArrayList<>();
    values.add(Pair.of(task1, predicateEvaluator));

    try {
      state = new RxExecutionChain(state, task)
              .next(values)
              .execute();
    } catch (Exception e) {
    }

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertNull(state.getValue("B-RESPONSE")); // Predicate didn't match

    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertNull(state.getValue("C-ERROR"));

    Assert.assertNull(state.getExceptionTaskIndex());
    Assert.assertNull(state.getError());
  }

  @Test // Success Case
  public void testWithBooleanPredicate() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    boolean value = true; // Evaluating Boolean Predicate

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    PredicateEvaluator<Boolean> predicateEvaluator = new PredicateEvaluator<>((s -> s.equals(true)), value);

    List<Pair<Task, PredicateEvaluator<Boolean>>> values = new ArrayList<>();
    values.add(Pair.of(task1, predicateEvaluator));

    try {
      state = new RxExecutionChain(state, task)
              .next(values)
              .execute();
    } catch (Exception e) {
    }

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));

    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertNull(state.getValue("C-ERROR"));

    Assert.assertNull(state.getExceptionTaskIndex());
    Assert.assertNull(state.getError());
  }

  @Test // Failure case
  public void testWithBooleanPredicateFalse() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    boolean value = false; // Evaluating Boolean Predicate

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    PredicateEvaluator<Boolean> predicateEvaluator = new PredicateEvaluator<>((s -> s.equals(true)), value);

    List<Pair<Task, PredicateEvaluator<Boolean>>> values = new ArrayList<>();
    values.add(Pair.of(task1, predicateEvaluator));

    try {
      state = new RxExecutionChain(state, task)
              .next(values)
              .execute();
    } catch (Exception e) {
    }

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertNull(state.getValue("B-RESPONSE")); // Predicate didn't match
    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertNull(state.getValue("C-ERROR"));
    Assert.assertNull(state.getExceptionTaskIndex());
    Assert.assertNull(state.getError());
  }

  @Test // Success case using RxExecutionChain constructor
  public void testWithBooleanPredicateTrueRxExecutionConstructor() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    boolean value = true; // Evaluating Boolean Predicate

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    PredicateEvaluator<Boolean> predicateEvaluator = new PredicateEvaluator<>((s -> s.equals(true)), value);

    List<Pair<Task, PredicateEvaluator<Boolean>>> values = new ArrayList<>();
    values.add(Pair.of(task, predicateEvaluator));

    try {
      state = new RxExecutionChain(state, values)
              .next(task1)
              .execute();
    } catch (Exception e) {
    }

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));

    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertNull(state.getValue("C-ERROR"));
    Assert.assertNull(state.getExceptionTaskIndex());
    Assert.assertNull(state.getError());
  }

  @Test // Success case using RxExecutionChain constructor
  public void testWithBooleanPredicateTrueRxExecutionConstructorWithRollback() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    boolean value = true; // Evaluating Boolean Predicate

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    PredicateEvaluator<Boolean> predicateEvaluator = new PredicateEvaluator<>((s -> s.equals(true)), value);

    List<Pair<Task, PredicateEvaluator<Boolean>>> values = new ArrayList<>();
    values.add(Pair.of(task, predicateEvaluator));

    try {
      state = new RxExecutionChain(state, values)
              .next(task1)
              .executeWithRollBack();
    } catch (Exception e) {
    }

    Assert.assertEquals("DONE-A", state.getValue("A-RESPONSE"));
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));

    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertNull(state.getValue("C-ERROR"));
    Assert.assertNull(state.getExceptionTaskIndex());
    Assert.assertNull(state.getError());
  }

  @Test // Failure case using RxExecutionChain constructor
  public void testWithBooleanPredicateFalseRxExecutionConstructor() {
    State state = new State();
    state.addValue("A", 1);
    state.addValue("B", 2);
    boolean value = false; // Evaluating Boolean Predicate

    TestTask task = new TestTask("A", "A-RESPONSE", "DONE-A");
    TestTask task1 = new TestTask("B", "B-RESPONSE", "DONE-B");
    PredicateEvaluator<Boolean> predicateEvaluator = new PredicateEvaluator<>((s -> s.equals(true)), value);

    List<Pair<Task, PredicateEvaluator<Boolean>>> values = new ArrayList<>();
    values.add(Pair.of(task, predicateEvaluator));

    try {
      state = new RxExecutionChain(state, values)
              .next(task1)
              .execute();
    } catch (Exception e) {
    }

    Assert.assertNull(state.getValue("A-RESPONSE")); // Predicate didn't match
    Assert.assertEquals("DONE-B", state.getValue("B-RESPONSE"));
    Assert.assertNull(state.getValue("A-ERROR"));
    Assert.assertNull(state.getValue("B-ERROR"));
    Assert.assertNull(state.getValue("C-ERROR"));
    Assert.assertNull(state.getExceptionTaskIndex());
    Assert.assertNull(state.getError());
  }

}

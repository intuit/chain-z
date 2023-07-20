package com.intuit.async.execution.impl;

import static com.intuit.async.execution.util.ChainHelper.getScheduler;
import static com.intuit.async.execution.util.ChainHelper.logTime;
import static java.util.Objects.isNull;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.intuit.async.execution.Chain;
import com.intuit.async.execution.Task;
import com.intuit.async.execution.config.ExecutionChainConfiguration;
import com.intuit.async.execution.request.State;
import com.intuit.async.execution.util.RxExecutionChainAction;

import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

/**
 * It helps in executing async tasks.It also provides chaining in tasks that too in async manner.
 *
 * @author Nishant-Sehgal
 */
@Slf4j
public class RxExecutionChain implements Chain {

  private State chainState;

  /** linked hash map to maintain order of tasks in chain */
  private Map<Integer, Observable<State>> inputObv = new LinkedHashMap<>();

  /** linked hash map to maintain order of rollback tasks in chain */
  private Map<Integer, Observable<State>> rollbackObv = new LinkedHashMap<>();

  private ExecutionChainConfiguration executionChainConfig;

  public RxExecutionChain(State inputReq, Task... tasks) {
    this.chainState = inputReq;
    populateTasks(tasks);
  }

  public RxExecutionChain(State inputReq) {
    this.chainState = inputReq;
  }

  public RxExecutionChain(State inputReq, ExecutionChainConfiguration executionChainConfig) {
    this.chainState = inputReq;
    this.executionChainConfig = executionChainConfig;
  }

  public RxExecutionChain(
      State inputReq, ExecutionChainConfiguration executionChainConfig, Task... tasks) {
    this.chainState = inputReq;
    this.executionChainConfig = executionChainConfig;
    populateTasks(tasks);
  }

  public RxExecutionChain next(Task... tasks) {
    populateTasks(tasks);
    return this;
  }

  /**
   * executes the tasks submitted to the execution chain.
   *
   * @return the merge output State that contains all the response inside it.
   */
  public State execute() {

    if (inputObv.isEmpty()) {
      return chainState;
    }

    getMergedObv().blockingSubscribe();

    return chainState;
  }

  /**
   * executes the tasks submitted to the execution chain in SYNC.
   * and rollback in case of error.
   * @return the merge output State that contains all the response inside it.
   */
  public State executeWithRollBack() {
    try {
      return execute();
    } catch (Exception e) {
      executeRollback();
      throw e;
    }
  }

  /**
   * Executes the rollback chain.
   */
  private void executeRollback() {
    getMergedRollbackObv().blockingSubscribe();
  }

  /** execute the task of chain submitted in async.It is fire and forget */
  public void executeAsync() {
    getMergedObv().subscribe();
  }

  /** returns the merged observable */
  public Observable<State> executeAsyncWithObv() {
    return getMergedObv();
  }

  /**
   * populates the Map of tasks by creating Observable around the tasks and merge the set of
   * Observable tasks in a single Observable task.
   *
   * @param tasks input task to be executed
   */
  private void populateTasks(Task... tasks) {
    if (isNull(tasks) || tasks.length == 0) {
      return;
    }

    Integer key = inputObv.size() + 1;

    List<Observable<State>> list = new ArrayList<>();
    List<Observable<State>> rollbackList = new ArrayList<>();

    for (Task task : tasks) {
      // if task is non fatal always setting on-resume next since any exception encountered will
      // result in moving to next chain
      list.add(getObvWithErrorResume(task.isFatal(), mapTaskToObservable(task, key, false)));
      rollbackList.add(getObvWithErrorResume(task.isFatal(), mapTaskToObservable(task, key, true)));
    }

    /**
     * merge the given set of Task and creating the merged Observable.If any of the merged
     * Observable Sources notify of an error via onError, mergeDelayError will refrain from
     * propagating that error notification until all of the merged ObservableSources have finished
     * emitting items
     */
    Observable<State> mergedTaskObv = Observable.mergeDelayError(list);
    Observable<State> mergedRollbackObv = Observable.mergeDelayError(rollbackList);

    // populates chain in the map
    inputObv.put(key, mergedTaskObv);
    rollbackObv.put(key, mergedRollbackObv);
  }

  /**
   * maps the given input task to Observable by subscribing it on new thread.
   *
   * <p>scheduling it on io thread that creates a new thread or use the idle thread.
   *
   * @param task input task
   * @param index index of task in the execution chain.
   * @param errorChain flag if this is to create error chain or the execution chain.
   * @return Observable task
   */
  private Observable<State> mapTaskToObservable(Task task, Integer index, boolean errorChain) {
    Observable<Task> taskObv = Observable.just(task);

    Observable<State> stateObv = null;

    stateObv =
        taskObv.map(
            inputTask -> {
              // passing copy of shared state instead of actual state
              State stateCopy = new State();
              stateCopy.addAll(chainState);
              Instant start = Instant.now();
              try {
                if (errorChain) {
                  return inputTask.onError(stateCopy);
                }
                return inputTask.execute(stateCopy);
              } finally {
                // if time is to be logged for the tasks
                if (logTime(executionChainConfig)) {
                  log.info(
                      "message=Time taken,taskName={},ttl={}ms",
                      inputTask.getClass().getSimpleName(),
                      Duration.between(start, Instant.now()).toMillis());
                }
              }
            });

    return stateObv
        .subscribeOn(getScheduler(executionChainConfig))
        .map(
            inputTaskResponse -> {
              chainState.addAll(inputTaskResponse);
              return inputTaskResponse;
            })
        .doOnError(
            error -> {
              // This is to add exception to the state object (Only if the task is marked fatal)
              if (task.isFatal() && !errorChain) {
                chainState.addError(error, index);
              }
              if (errorChain) {
                log.error("message=Error occurred while rolling back task: " + task.getClass().getSimpleName(), error);
              } else {
                log.error("message=Error occurred while executing task: " + task.getClass().getSimpleName(), error);
              }
            });
  }

  /**
   * create chain of observable for execution.
   *
   * @return the merge Observable for all the chain
   */
  private Observable<State> getMergedObv() {
    return getMergedObv(inputObv);
  }

  /**
   * @param fatal if fatal return the Observable as it is else adds onErrorResumeNext handling
   * @param inputObv
   * @return Observable with on error-resume handling if applicable
   */
  private Observable<State> getObvWithErrorResume(boolean fatal, Observable<State> inputObv) {
    return fatal ? inputObv : inputObv.onErrorResumeNext(Observable.empty());
  }

  /**
   * create chain of observable by setting next executing observable on on-complete of current
   * observable and create the chain in this form.
   *
   * @return the merge Observable for all the chain
   */
  private Observable<State> getMergedObv(Map<Integer, Observable<State>> input) {
    // return an empty Observable if no chain observable found.
    if (null == input || input.isEmpty()) {
      return Observable.empty();
    }

    // if there is only one chain directly return that
    if (input.size() == 1) {
      return input.entrySet().stream().findFirst().get().getValue();
    }

    @SuppressWarnings("unchecked")
	Entry<Integer, Observable<State>>[] inputObvEntry = input.entrySet().toArray(new Entry[0]);

    Observable<State> firstObservableReference = null;

    for (int i = inputObvEntry.length - 1; i > 0; i--) {
      Entry<Integer, Observable<State>> entry = inputObvEntry[i - 1];
      Entry<Integer, Observable<State>> entryNext = inputObvEntry[i];

      firstObservableReference =
              entry
                      .getValue()
                      // setting the next chain of observable onCompletion of current
                      .doOnComplete(new RxExecutionChainAction(firstObservableReference, entryNext));
    }

    return firstObservableReference;
  }

  /**
   * create chain of observable for rollback in reverse order.
   *
   * @return the merge Observable for all the chain
   */
  private Observable<State> getMergedRollbackObv() {
    // Removing the tasks later in the chain, as only the task which are executed till the exception occured,
    // are required to be rolled back.
    IntStream.range(chainState.getExceptionTaskIndex() + 1, rollbackObv.size() + 1)
            .forEach(key -> rollbackObv.remove(key));
    // Reversing the order as rollback is required in the reverse order.
    return getMergedObv(reverseMap(rollbackObv));
  }

  /**
   * Reverses the state map.
   * @return
   */
  private Map<Integer, Observable<State>> reverseMap(Map<Integer, Observable<State>> map) {

    Map<Integer, Observable<State>> reversedMap = new LinkedHashMap<>();

    List<Integer> orderedKeys = map.keySet().stream().collect(Collectors.toList());

    // Reversing the list of keys.
    Collections.reverse(orderedKeys);

    orderedKeys.forEach((key)->reversedMap.put(key,map.get(key)));
    return reversedMap;
  }
}

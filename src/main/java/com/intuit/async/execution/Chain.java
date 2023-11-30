package com.intuit.async.execution;

import com.intuit.async.execution.request.State;

import io.reactivex.Observable;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Predicate;


/** @author Nishant-Sehgal */
public interface Chain {

  /**
   * execute the tasks in chain and merge the response in the state and return the final merged
   * state.
   *
   * @return {@link State}
   */
  State execute();

  /** it submits tasks in async and don't wait for the response.It is fire and forget tasks */
  void executeAsync();

  /**
   * @param tasks input tasks that will be executed in a given chain
   * @return current {@link Chain} reference
   */
  Chain next(Task... tasks);
  /**
   * @param taskPredicate list of task with respective predicates
   * @param <T>
   * @return current {@link Chain} reference
   */
  <T> Chain next(final Pair<Task, Pair<Predicate<T>, T>>... taskPredicate);

  /**
   * Depends on the caller to handle async or sync and can add call backs as well.
   *
   * @return the Final Merged Observable.
   */
  Observable<State> executeAsyncWithObv();
}

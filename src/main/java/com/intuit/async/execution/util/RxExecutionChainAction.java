package com.intuit.async.execution.util;

import com.intuit.async.execution.request.State;

import java.util.Map.Entry;

import io.reactivex.Observable;
import io.reactivex.functions.Action;

/**
 * @author Nishant-Sehgal
 * <p>Build the Chaining of actions while creating merged observable
 */
public class RxExecutionChainAction implements Action {

    private Observable<State> inputObv = null;
    private Entry<Integer, Observable<State>> entry = null;

    public RxExecutionChainAction(Observable<State> inputObv, Entry<Integer, Observable<State>> entry) {
        this.inputObv = inputObv;
        this.entry = entry;
    }

    @Override
    public void run() throws Exception {
        if (inputObv == null) {
            entry.getValue().blockingSubscribe();
        } else {
            inputObv.blockingSubscribe();
        }
    }
}

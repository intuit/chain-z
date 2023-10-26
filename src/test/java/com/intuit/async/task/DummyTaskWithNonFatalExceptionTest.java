package com.intuit.async.task;

import com.intuit.async.execution.Task;
import com.intuit.async.execution.impl.ChainConstantTest;
import com.intuit.async.execution.request.State;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nishant-Sehgal
 */
public class DummyTaskWithNonFatalExceptionTest implements Task {

    private static final Logger log = LoggerFactory.getLogger("DummyTaskWithNonFatalExceptionTest");

    private String key;

    public DummyTaskWithNonFatalExceptionTest(String key) {
        this.key = key;
    }

    @Override
    public boolean isFatal() {
        return false;
    }

    @Override
    public State execute(State inputRequest) {
        Map<String, List<Integer>> req = inputRequest.getValue(ChainConstantTest.DUMMYTASKREQ);
        if ("A".equals(key) || "C".equals(key)) {
            int val = 1 / 0;
            log.debug(String.valueOf(val));
        }

        log.info(
                "Executed Task for request :: "
                        + req.get(key)
                        + " Thread "
                        + Thread.currentThread().getName());
        State state = new State();
        state.addValue("RESPONSE" + "-" + key, "DONE" + "-" + key);
        return state;
    }
}

package com.intuit.async.execution.request;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNull;

/** @author Nishant-Sehgal */
public class StateTest {

  @Test
  public void testgetValueNullKey() {
    State state = new State();
    assertNull(state.getValue(null));
  }

  @Test
  public void testgetValue() {
    State state = new State();
    state.addValue("A", "B");
    Assert.assertNotNull(state.getValue("A"));
    Assert.assertEquals("B", state.getValue("A"));
  }

  @Test
  public void testlogTimeFalse() {
    State state = new State();
    try {
      state.addValue(null, null);
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  public void testAddValueWithNullValue() {
    State state = new State();
    state.addValue("A", null);
    assertNull(state.getValue("A"));
  }

}

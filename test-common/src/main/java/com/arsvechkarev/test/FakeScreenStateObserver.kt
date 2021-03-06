package com.arsvechkarev.test

import androidx.lifecycle.Observer
import core.BaseScreenState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class FakeScreenStateObserver : Observer<BaseScreenState> {
  
  @PublishedApi
  internal val states: List<BaseScreenState> = ArrayList()
  
  @PublishedApi
  internal var _currentState: BaseScreenState? = null
  
  override fun onChanged(state: BaseScreenState) {
    (states as ArrayList).add(state)
    _currentState = state
  }
}


fun FakeScreenStateObserver.hasStatesCount(size: Int) {
  assertEquals(size, states.size)
}

inline fun <reified T : BaseScreenState> FakeScreenStateObserver.hasStateAtPosition(position: Int): T {
  assertTrue(states[position] is T)
  return states[position] as T
}

inline fun <reified T : BaseScreenState> FakeScreenStateObserver.hasCurrentState() {
  assertTrue(states.last() is T)
}

fun <T : BaseScreenState> FakeScreenStateObserver.currentState(): T {
  @Suppress("UNCHECKED_CAST")
  return _currentState as T
}

fun <T : BaseScreenState> FakeScreenStateObserver.state(position: Int): T {
  @Suppress("UNCHECKED_CAST")
  return states[position] as T
}
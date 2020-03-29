package core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import core.extenstions.updateSelf
import kotlin.reflect.KClass

/**
 * Base class for dealing with multiple states in view model
 */
class StateHandle<S : Any> {
  
  internal var newState: S? = null
  
  private val states = HashMap<KClass<out S>, S>()
  
  fun <T : S> addOrUpdate(state: T) {
    newState = state
    states[state::class] = state
  }
  
  fun <T : S> setOnly(state: T) {
    clear()
    newState = state
    states[state::class] = state
  }
  
  @Suppress("UNCHECKED_CAST")
  fun <T : S> get(stateClass: KClass<T>): T {
    return (states[stateClass] as T)
  }
  
  @Suppress("UNCHECKED_CAST")
  fun <T : S> doIfContains(stateClass: KClass<T>, action: T.() -> Unit) {
    if (states.containsKey(stateClass)) action(states[stateClass] as T)
  }
  
  fun <T : S> remove(stateClass: KClass<T>) = states.remove(stateClass)
  
  fun <T : S> contains(stateClass: KClass<T>) = states.containsKey(stateClass)
  
  fun clear() {
    newState = null
    states.clear()
  }
  
  /**
   * Iterates over all states applying given action or applies action to a new state (if
   * such state is not null)
   *
   * @see updateAll
   */
  fun forAll(action: (S) -> Unit) {
    if (newState != null) {
      action(newState!!)
    } else {
      states.values.forEach(action)
    }
  }
}

/**
 * Adds value to states map (or updates if the value already exists). Also can optionally remove state by
 * specified [remove]
 */
fun <T : S, R : S, S : Any> MutableLiveData<StateHandle<S>>.addOrUpdate(state: T, remove: KClass<R>? = null) {
  value!!.addOrUpdate(state)
  if (remove != null) {
    value!!.remove(remove)
  }
  updateSelf()
}

fun <T : S, S : Any> LiveData<StateHandle<S>>.contains(stateClass: KClass<T>): Boolean {
  return value!!.contains(stateClass)
}

fun <T : S, S : Any> LiveData<StateHandle<S>>.remove(stateClass: KClass<T>) {
  value!!.remove(stateClass)
}

fun <T : S, S : Any> MutableLiveData<StateHandle<S>>.doIfContains(
  stateClass: KClass<T>,
  action: T.() -> Unit
) {
  value!!.doIfContains(stateClass, action)
  updateSelf()
}

fun <S : Any> MutableLiveData<StateHandle<S>>.updateAll() {
  value!!.newState = null
  updateSelf()
}
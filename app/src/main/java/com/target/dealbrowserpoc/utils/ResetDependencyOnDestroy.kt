package com.target.dealbrowserpoc.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Operator overload provideDelegate to set resource to null when destroy lifecycle event
 * is observed on the property host object (LifecycleOwner).
 */
class ResetDependencyOnDestroy<T : Any> {
  private var dependencyValue: T? = null

  operator fun provideDelegate(thisRef: LifecycleOwner, property: KProperty<*>):
    ReadWriteProperty<LifecycleOwner, T> {
    thisRef.lifecycle.addObserver(object : LifecycleObserver {
      @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
      fun destroyDependency() {
        if (thisRef.lifecycle.currentState.isAtLeast(Lifecycle.State.DESTROYED)) {
          dependencyValue = null
        }
      }
    })

    return object : ReadWriteProperty<LifecycleOwner, T> {
      // Throw error if dependency injection failed to set dependency value
      override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): T {
        return dependencyValue ?: throw UninitializedPropertyAccessException(property.name)
      }

      override fun setValue(thisRef: LifecycleOwner, property: KProperty<*>, value: T) {
        dependencyValue = value
      }
    }
  }
}
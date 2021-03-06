package com.target.dealbrowserpoc.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Operator overload provideDelegate to initialize [disposables] property when ON_START
 * lifecycle event is observed on the property host object (LifecycleOwner).
 * Dispose and set [disposables] to null when ON_STOP lifecycle event is observed
 */
class DisposableOnLifecycleChange {
  private var disposables: CompositeDisposable? = null

  operator fun provideDelegate(thisRef: LifecycleOwner, property: KProperty<*>):
    ReadOnlyProperty<LifecycleOwner, CompositeDisposable> {
    thisRef.lifecycle.addObserver(object : LifecycleObserver {
      @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
      fun initializeDisposable() {
        if (thisRef.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
          disposables = CompositeDisposable()
        }
      }

      @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
      fun clearDisposables() {
        disposables?.clear()
      }

      @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
      fun disposeDisposables() {
        disposables?.dispose()
        disposables = null
      }
    })

    return object : ReadOnlyProperty<LifecycleOwner, CompositeDisposable> {
      override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): CompositeDisposable {
        return disposables ?: throw UninitializedPropertyAccessException(property.name)
      }
    }
  }
}
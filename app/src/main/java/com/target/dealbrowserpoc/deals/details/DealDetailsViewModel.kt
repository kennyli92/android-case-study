package com.target.dealbrowserpoc.deals.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.target.dealbrowserpoc.dealbrowser.R
import com.target.dealbrowserpoc.deals.data.DealsRepository
import com.target.dealbrowserpoc.deals.data.DealsResponse
import com.target.dealbrowserpoc.dialog.SnackbarViewModel
import com.target.dealbrowserpoc.log.Logging
import com.target.dealbrowserpoc.utils.StateEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class DealDetailsViewModel(
  private val handle: SavedStateHandle,
  private val dealsRepository: DealsRepository
) : ViewModel() {
  companion object {
    private const val STATE_KEY = "state"
  }

  @Volatile
  private var state: DealDetailsState = DealDetailsState.Noop

  /**
   * Should be called onStop() to save state upon backgrounding
   */
  fun saveState() {
    handle[STATE_KEY] = state
  }

  /**
   * should be called onStart() to recover state upon foregrounding
   * from backgrounding or a process death
   */
  fun restoreState() {
    state = handle[STATE_KEY] ?: DealDetailsState.Noop
  }

  private val stateEventObs =
    PublishSubject.create<StateEvent<DealDetailsState, DealDetailsEvent>>().toSerialized()

  /**
   * emit State that should persist on screen upon foreground from backgrounding or process death
   */
  fun stateObs(): Observable<DealDetailsState> {
    return stateEventObs.map {
      it.state
    }.distinctUntilChanged().observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Emit single Event that should happen once and not persist on the screen such as dialogs,
   * snackbars, navigations
   */
  fun eventObs(): Observable<DealDetailsEvent> {
    return stateEventObs.map {
      it.event
    }.observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Observe actions to be handled created from click streams
   */
  fun actionHandler(actionSignal: Observable<DealDetailsAction>): Disposable {
    return actionSignal
      .observeOn(Schedulers.computation())
      .flatMap { action ->
        when (action) {
          is DealDetailsAction.Load -> onLoad(action = action)
          is DealDetailsAction.Back -> onBackClick()
        }
      }.subscribe({
        this.state = it.state
        stateEventObs.onNext(it)
      }, Logging.logErrorAndThrow())
  }

  /**
   * We can do state recovery onLoad() should there be a need
   */
  private fun onLoad(
    action: DealDetailsAction.Load
  ): Observable<StateEvent<DealDetailsState, DealDetailsEvent>> {
    return dealsRepository.getDeals()
      .observeOn(Schedulers.computation())
      .flatMapObservable { dealsResponse ->
        when (dealsResponse) {
          is DealsResponse.Deals -> {
            val selectedDeal = dealsResponse.deals.find { deal ->
              action.dealId == deal.id
            }
            return@flatMapObservable if (selectedDeal != null) {
              Observable.just(StateEvent(
                DealDetailsState.Detail(
                  imageUrl = selectedDeal.image,
                  salePrice = selectedDeal.salePrice ?: selectedDeal.price,
                  regularPrice = selectedDeal.price,
                  title = selectedDeal.title,
                  description = selectedDeal.description
                ),
                DealDetailsEvent.Noop
              ))
            } else {
              Observable.just(
                StateEvent(
                  DealDetailsState.Noop,
                  DealDetailsEvent.Snackbar(
                    vm = SnackbarViewModel(
                      messageResId = R.string.deal_details_get_unknown_error,
                      duration = LENGTH_LONG
                    )
                  )
                ))
            }
          }
          is DealsResponse.NotFound -> Observable.just(
            StateEvent(
              DealDetailsState.Noop,
              DealDetailsEvent.Snackbar(
                vm = SnackbarViewModel(
                  messageResId = R.string.deal_details_get_not_found,
                  duration = LENGTH_LONG
                )
              )
            ))
          is DealsResponse.UnknownError -> Observable.just(
            StateEvent(
              DealDetailsState.Noop,
              DealDetailsEvent.Snackbar(
                vm = SnackbarViewModel(
                  messageResId = R.string.deal_details_get_unknown_error,
                  duration = LENGTH_LONG
                )
              )
            ))
        }
      }
  }

  /**
   * Observes back click action and emits back navigation event
   */
  private fun onBackClick(): Observable<StateEvent<DealDetailsState, DealDetailsEvent>> {
    return Observable.just(
      StateEvent(
        state = DealDetailsState.Noop,
        event = DealDetailsEvent.Back
      )
    )
  }
}
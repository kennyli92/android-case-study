package com.target.dealbrowserpoc.deals.list

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.target.dealbrowserpoc.deals.data.DealsRepository

class DealsListViewModelFactory(
  private val dealsRepository: DealsRepository,
  owner: SavedStateRegistryOwner,
  defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(
      key: String,
      modelClass: Class<T>,
      handle: SavedStateHandle
    ): T {
        return DealsListViewModel(handle = handle, dealsRepository = dealsRepository) as T
    }
}
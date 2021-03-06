package com.target.dealbrowserpoc.navigation

import androidx.appcompat.app.AppCompatActivity

interface Navigator {
  /**
   * Initializes the activity to use fragment manager
   */
  fun initializeContext(activity: AppCompatActivity)

  /**
   * releases the activity context to prevent memory leak
   */
  fun releaseContext()

  fun back()

  fun navigateDealsList()

  fun navigateDealsDetails(id: String)
}
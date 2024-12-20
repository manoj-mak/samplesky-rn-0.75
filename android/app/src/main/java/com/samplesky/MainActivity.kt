package com.samplesky

import androidx.activity.result.ActivityResultLauncher
import androidx.health.connect.client.PermissionController
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate
import com.getvisitapp.google_fit.healthConnect.OnActivityResultImplementation
import timber.log.Timber

class MainActivity : ReactActivity() {

    private val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()

    var onActivityResultImplementation: OnActivityResultImplementation<Set<String>, Set<String>?>? =
        null

    val requestPermissions: ActivityResultLauncher<Set<String>> = registerForActivityResult(
        requestPermissionActivityContract
    ) { granted: Set<String>? ->
        Timber.d("requestPermissions registerForActivityResult: %s", granted)
        if (onActivityResultImplementation != null) {
            onActivityResultImplementation!!.execute(granted)
        }
    }

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  override fun getMainComponentName(): String = "SampleSky"

  /**
   * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
   * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
   */
  override fun createReactActivityDelegate(): ReactActivityDelegate =
      DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)
}

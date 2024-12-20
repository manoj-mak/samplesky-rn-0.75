package com.samplesky.modules

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.Keep
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.getvisitapp.google_fit.HealthConnectListener
import com.getvisitapp.google_fit.data.VisitStepSyncHelper
import com.getvisitapp.google_fit.healthConnect.OnActivityResultImplementation
import com.getvisitapp.google_fit.healthConnect.activity.HealthConnectUtil
import com.getvisitapp.google_fit.healthConnect.contants.Contants.previouslyRevoked
import com.getvisitapp.google_fit.healthConnect.enums.HealthConnectConnectionState
import com.getvisitapp.google_fit.healthConnect.enums.HealthConnectConnectionState.CONNECTED
import com.getvisitapp.google_fit.healthConnect.enums.HealthConnectConnectionState.INSTALLED
import com.getvisitapp.google_fit.healthConnect.enums.HealthConnectConnectionState.NONE
import com.getvisitapp.google_fit.healthConnect.enums.HealthConnectConnectionState.NOT_INSTALLED
import com.getvisitapp.google_fit.healthConnect.enums.HealthConnectConnectionState.NOT_SUPPORTED
import com.samplesky.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Keep
class VisitFitnessModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), HealthConnectListener {

  private val TAG = "mytag"
  private val reactContext: ReactContext = reactContext

  private var promise: Promise? = null

  private var healthConnectStatusPromise: Promise? = null
  private var dataRetrivalPromise: Promise? = null
  private var isLoggingEnabled = false

  private var visitStepSyncHelper: VisitStepSyncHelper? = null
  private var healthConnectUtil: HealthConnectUtil? = null
  private var syncDataWithServer = false
  var mainActivity: MainActivity? = null


  init {
    Log.d(TAG, "GoogleFitPermissionModule: inside constructor")
  }

  @ReactMethod
  fun initiateSDK(isLoggingEnabled: Boolean) {
    Timber.d("mytag: initiateSDK %b", isLoggingEnabled)

    this.isLoggingEnabled = isLoggingEnabled
    mainActivity = reactContext.currentActivity as? MainActivity

    //don't initialise the native module if the mainActivity instance is null.
    if (mainActivity == null) {
      return
    }
    visitStepSyncHelper = VisitStepSyncHelper(mainActivity!!)
    healthConnectUtil = HealthConnectUtil(mainActivity!!, this)
    healthConnectUtil!!.initialize()

    mainActivity!!.onActivityResultImplementation =
      object : OnActivityResultImplementation<Set<String>, Set<String>?> {


        override fun execute(granted: Set<String>?): Set<String> {
          Timber.d("onActivityResultImplementation execute: result: %s", granted)

          granted?.let {
            if (granted.containsAll(healthConnectUtil!!.PERMISSIONS)) {
              previouslyRevoked = false


              Timber.d("Permissions successfully granted")
              healthConnectUtil!!.checkPermissionsAndRunForStar(true)
            } else {
              Timber.d("Lack of required permissions")
              healthConnectUtil!!.checkPermissionsAndRunForStar(true)
            }

          }
          return granted!!
        }
      }

    Timber.d("mytag: initiateSDK() called")
  }

  @ReactMethod
  fun getHealthConnectStatus(healthConnectStatusPromise: Promise) {
    this.healthConnectStatusPromise = healthConnectStatusPromise

    Timber.d("mytag: getHealthConnectStatus called")

    healthConnectUtil?.scope?.launch {
      val status: HealthConnectConnectionState = healthConnectUtil!!.checkAvailability()

      withContext(Dispatchers.Main) {
        when (status) {
          NOT_SUPPORTED -> {
            healthConnectStatusPromise?.resolve("NOT_SUPPORTED")
          }

          NOT_INSTALLED -> {
            healthConnectStatusPromise?.resolve("NOT_INSTALLED")
          }

          INSTALLED -> {
            healthConnectStatusPromise?.resolve("INSTALLED")
          }

          CONNECTED -> {
            healthConnectStatusPromise.resolve("CONNECTED")
          }

          NONE -> {

          }
        }
      }
    }
  }

  @ReactMethod
  fun askForFitnessPermission(promise: Promise) {
    this.promise = promise
    healthConnectUtil?.let {
      if (healthConnectUtil!!.healthConnectConnectionState == CONNECTED) {
        Timber.d("askForFitnessPermission: already granted")
        promise.resolve("GRANTED")
      } else {
        Timber.d("askForFitnessPermission: request permission")
        healthConnectUtil!!.requestPermission()
      }
    }

  }

  override fun userAcceptedHealthConnectPermission() {
    Timber.d("userAcceptedHealthConnectPermission")
    promise!!.resolve("GRANTED")
  }

  override fun userDeniedHealthConnectPermission() {
    Timber.d("userDeniedHealthConnectPermission")
    promise!!.resolve("CANCELLED")
  }

  @ReactMethod
  fun requestDailyFitnessData(promise: Promise?) {
    this.dataRetrivalPromise = promise

    if (healthConnectUtil!!.healthConnectConnectionState == CONNECTED) {
      healthConnectUtil!!.getVisitDashboardGraph()
    }
  }

  @ReactMethod
  fun requestActivityDataFromHealthConnect(
    type: String?,
    frequency: String?,
    timestamp: Double,
    promise: Promise?
  ) {
    this.dataRetrivalPromise = promise
    Timber.d("mytag: requestActivityData() called.")
    healthConnectUtil?.let {
      if (healthConnectUtil!!.healthConnectConnectionState == HealthConnectConnectionState.CONNECTED) {
        //Health Connect Implementation
        healthConnectUtil!!.getActivityData(type, frequency, Math.round(timestamp))
      } else {
        Timber.d("mytag: permission not available healthConnectConnectionState: ${healthConnectUtil!!.healthConnectConnectionState}")
        healthConnectUtil!!.requestPermission()
      }
    }

  }

  @ReactMethod
  fun updateApiBaseUrl(
    apiBaseUrl: String,
    authToken: String,
    googleFitLastSync: Double,
    gfHourlyLastSync: Double,
    promise: Promise?
  ) {
    if (isLoggingEnabled) {
      Log.d(
        "mytag",
        "GoogleFitPermissionModule syncDataWithServer(): baseUrl: " + apiBaseUrl + " authToken: " + authToken +
          " googleFitLastSync: " + googleFitLastSync + "  gfHourlyLastSync:" + gfHourlyLastSync
      )
    }

    if (!syncDataWithServer && healthConnectUtil != null) {
      Timber.d("mytag: syncDataWithServer() called")
      visitStepSyncHelper?.sendDataToVisitServer(
        healthConnectUtil!!, Math.round(googleFitLastSync), Math.round(gfHourlyLastSync),
        "$apiBaseUrl/", authToken
      )
      syncDataWithServer = true
      promise?.resolve("Health Data Sync Started")
    } else {
      promise?.resolve("Health Data Already Synced")
    }


  }

  @ReactMethod
  fun openHraLink(link: String?) {
    try {
      val i = Intent(Intent.ACTION_VIEW)
      i.setData(Uri.parse(link))
      mainActivity!!.startActivity(i)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  @ReactMethod
  fun fetchDailyFitnessData(timestamp: Double, promise: Promise?) {
    this.promise = promise
    // TODO: to be implemented in the future.
  }

  @ReactMethod
  fun fetchHourlyFitnessData(timestamp: Double, promise: Promise?) {
    this.promise = promise
    // TODO: to be implemented in the future.
  }

  @ReactMethod
  fun openHealthConnectApp(promise: Promise?) {
    this.promise = promise
    healthConnectUtil?.openHealthConnectApp();
  }

  override fun getName(): String {
    return "VisitFitnessModule"
  }

  override fun loadVisitWebViewGraphData(webString: String) {
    Handler(Looper.getMainLooper()).post {
      if (isLoggingEnabled) {
        Log.d("mytag", "loadVisitWebViewGraphData: $webString")
      }
      dataRetrivalPromise!!.resolve(webString)
    }
  }


  override fun requestPermission() {
    Timber.d("requestPermission called 218")
    mainActivity?.requestPermissions?.launch(healthConnectUtil!!.PERMISSIONS)
  }

  override fun updateHealthConnectConnectionStatus(
    healthConnectConnectionState: HealthConnectConnectionState,
    s: String
  ) {
    Timber.d("updateHealthConnectConnectionStatus: %s", healthConnectConnectionState)

    when (healthConnectConnectionState) {
      CONNECTED -> {}
      NOT_SUPPORTED -> {}
      NOT_INSTALLED -> {}
      INSTALLED -> {}
      NONE -> {}
    }
  }

  override fun logHealthConnectError(throwable: Throwable) {
    //pass it via a callback.
    throwable.message?.let {
      val formattedMessage = "${throwable.javaClass}: ${throwable.message}"
      Timber.d("mytag: logHealthConnectError= $formattedMessage")

      sendMessageToReactNative(formattedMessage)
    }
  }

  private fun sendMessageToReactNative(message: String) {
    Timber.d("mytag: sendMessageToReactNative called()")
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit("onMessage", message) // "onMessage" is the event name
  }
}

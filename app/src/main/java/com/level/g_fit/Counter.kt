package com.level.g_fit

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.fitness.result.DataSourcesResult
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATION")
class Counter : AppCompatActivity(), OnDataPointListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    val REQUEST_OAUTH = 1
    val AUTH_PENDING: String = ""
    var authInProgress: Boolean = false
    lateinit var mApiClient: GoogleApiClient
    var fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counter)

        if (savedInstanceState != null) {
            Log.i("Connection", "savedInstance not null")
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING)
        }
        mApiClient = GoogleApiClient.Builder(this)
            .addApi(Fitness.SENSORS_API)
            .addApi(Fitness.RECORDING_API)
            .addApi(Fitness.HISTORY_API)
            .addScope(Fitness.SCOPE_ACTIVITY_READ_WRITE)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()
    }


    override fun onStart() {
        super.onStart()
        mApiClient.connect()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(AUTH_PENDING, authInProgress)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i("Connection", "onActiityResult")
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false
            if (resultCode == RESULT_OK) {
                Log.i("Connection", "Result code ok")
                if (!mApiClient.isConnecting && !mApiClient.isConnected) {
                    mApiClient.connect()
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.i("Connection", "Request Cancelled")
            }
        } else {
            Log.i("Connection", "Not request Oauth")
        }
    }

    override fun onDataPoint(p0: DataPoint) {
        Log.i("Connection", "onDataPoint")
        var t = "0"
        for (field: Field in p0.dataType.fields) {
            val value: Value = p0.getValue(field)
            runOnUiThread {
                Fitness.getHistoryClient(
                    this,
                    GoogleSignIn.getAccountForExtension(this, fitnessOptions)
                )
                    .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener { result ->
                        val totalSteps =
                            result.dataPoints.firstOrNull()?.getValue(Field.FIELD_STEPS)?.asInt()
                                ?: 0
                        findViewById<TextView>(R.id.tv_stepsTaken).text = totalSteps.toString()
                    }
                    .addOnFailureListener { e ->
                        Log.i("Connection", "There was a problem getting steps.", e)
                        Toast.makeText(this, "Can't Load Steps", Toast.LENGTH_LONG).show()
                    }
                Toast.makeText(
                    applicationContext,
                    "Field: " + field.name + " Value: " + value,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        Fitness.SensorsApi.remove(mApiClient, this)
            .setResultCallback {

                if (it.isSuccess) {
                    Log.i("Connection", "Disconnecting")
                    mApiClient.disconnect()
                }
            }
    }

    private fun registerFitnessDataListener(dataSource: DataSource?, dataType: DataType) {
        val request: SensorRequest = SensorRequest.Builder()
            .setDataSource(dataSource!!)
            .setDataType(dataType)
            .setSamplingRate(1, TimeUnit.SECONDS)
            .build()

        Fitness.SensorsApi.add(mApiClient, request, this)
            .setResultCallback {
                if (it.isSuccess) {
                    Log.i("Connection", "SensorApi Added")
                }
            }
    }

    override fun onConnected(p0: Bundle?) {
        //recordingData()
        //Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
        val rec : Recording_Subscribe = Recording_Subscribe()
        rec.start(mApiClient)
        Log.i("Connection", "onConnected")
        val dataSourceRequest = DataSourcesRequest.Builder()
            .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
            .setDataSourceTypes(DataSource.TYPE_RAW)
            .build()


        val dataSourcesResultCallback: ResultCallback<DataSourcesResult> =
            ResultCallback<DataSourcesResult>() {
                Log.i("Connection", "onResult")
                for (dataSource in it.dataSources) {
                    Log.i(
                        "Connection",
                        dataSource.dataType.toString() + " " + DataType.TYPE_STEP_COUNT_CUMULATIVE
                    )
                    if (DataType.TYPE_STEP_COUNT_CUMULATIVE == dataSource.dataType) {
                        Log.i("Connection", "Data Source Exist")
                        registerFitnessDataListener(
                            dataSource,
                            DataType.TYPE_STEP_COUNT_CUMULATIVE
                        )

                    }
                }
            }

        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest)
            .setResultCallback(dataSourcesResultCallback)
    }


    private fun recordingData() {
        Fitness.RecordingApi
            .subscribe(mApiClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
        Fitness.RecordingApi.subscribe(mApiClient,DataType.AGGREGATE_STEP_COUNT_DELTA)
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show()
        if (!authInProgress) {
            try {
                authInProgress = true
                p0.startResolutionForResult(this, REQUEST_OAUTH)
            } catch (e: IntentSender.SendIntentException) {
                Log.i("Connection", e.toString())
            }
        } else {
            Log.i("Connection", "authOnProgress")
        }
    }


}
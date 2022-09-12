@file:Suppress("DEPRECATION")

package com.level.g_fit

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.result.DataReadResult
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class Archive : AppCompatActivity(), OnDataPointListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private var WEEK_IN_MS = 1000 * 60 * 60 * 24 * 7
    var now: Date = Date()
    var endTIme = now.time
    var startTime = endTIme - (WEEK_IN_MS)

    val REQUEST_OAUTH = 1
    val AUTH_PENDING: String = ""
    var authInProgress: Boolean = false

    var items = ArrayList<String>()
    private lateinit var mApiClient: GoogleApiClient
    private val mAdapter: History_Adapter = History_Adapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive)


        setTime()
        fetchData()

        val recyclerView = findViewById<RecyclerView>(R.id.recycclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING)
        }

        mApiClient = GoogleApiClient.Builder(this)
            .addApi(Fitness.SENSORS_API)
            .addApi(Fitness.RECORDING_API)
            .addApi(Fitness.HISTORY_API)
            .addScope(Fitness.SCOPE_LOCATION_READ)
            .addScope(Fitness.SCOPE_ACTIVITY_READ_WRITE)
            .addScope(Fitness.SCOPE_BODY_READ)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()
    }


    private fun setTime() {
        val rightNow: Calendar = Calendar.getInstance()
        val offset: Long = (rightNow.get(Calendar.ZONE_OFFSET) +
                rightNow.get(Calendar.DST_OFFSET)).toLong()

        val sinceMidnight: Long = (rightNow.getTimeInMillis() + offset) %
                (24 * 60 * 60 * 1000)

        startTime += (24 * 60 * 60 * 1000 - sinceMidnight)
    }

    private fun fetchData() {
        val list = ArrayList<String>()
        list.add("Date : Steps")

        mAdapter.updateSteps(list)
    }

    override fun onStart() {
        super.onStart()
        mApiClient.connect()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(AUTH_PENDING, authInProgress)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false
            if (resultCode == RESULT_OK) {
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

    override fun onConnected(p0: Bundle?) {
        retrievingData()
    }

    private fun retrievingData() {
        val readReq: DataReadRequest = DataReadRequest.Builder()
            .aggregate(
                DataType.TYPE_STEP_COUNT_DELTA,
                DataType.AGGREGATE_STEP_COUNT_DELTA
            )
            .aggregate(
                DataType.TYPE_DISTANCE_DELTA,
                DataType.AGGREGATE_DISTANCE_DELTA
            )
            .aggregate(DataType.TYPE_MOVE_MINUTES)
            .aggregate(
                DataType.TYPE_CALORIES_EXPENDED,
                DataType.AGGREGATE_CALORIES_EXPENDED
            )
            .aggregate(
                DataType.TYPE_SPEED,
                DataType.AGGREGATE_SPEED_SUMMARY
            )
            .aggregate(DataType.TYPE_HEART_POINTS, DataType.AGGREGATE_HEART_POINTS)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTIme, TimeUnit.MILLISECONDS)
            .build()

        val pendingResult: PendingResult<DataReadResult> =
            Fitness.HistoryApi.readData(mApiClient, readReq)

        pendingResult.setResultCallback {
            if (it.buckets.size > 0) {
                for (bucket in it.buckets) {
                    val dataSets: List<DataSet> = bucket.dataSets
                    for (dataSet in dataSets) {
                        processData(dataSet)
                    }
                }
            }
        }
    }

    private fun processData(dataSet: DataSet) {
        for (dp in dataSet.dataPoints) {
            val dpStart = dp.getStartTime(TimeUnit.NANOSECONDS) / 1000000 + 100000
            val simpleDateFormat = SimpleDateFormat("EEEE")
            for (field in dp.dataType.fields) {
                Log.i(
                    "DATAS", dp.dataType.fields.size.toString() + " " + simpleDateFormat.format(
                        dpStart
                    ) + " $field = " + dp.getValue(field) + "\n"
                )
                    items.add(
                        0,
                        simpleDateFormat.format(
                            dpStart
                        ).toString() + " : " +
                                field.name + " : " + dp.getValue(field).toString()
                    )
            }

        }
        mAdapter.updateSteps(items)
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i("Connection", "Connection Suspended")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
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

    override fun onDataPoint(p0: DataPoint) {
        TODO("Not yet implemented")
    }
}
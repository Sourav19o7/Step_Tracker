package com.level.g_fit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.level.g_fit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    val FINE_LOCATION = 101
    val ACTIVITY_RECO = 102

    var str_permissions = arrayOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        request()
    }

    override fun onStart() {
        super.onStart()
        binding.run.setOnClickListener {
            val intent: Intent = Intent(this, Counter::class.java)
            startActivity(intent)
        }

        binding.history.setOnClickListener {
            val intent = Intent(this, Archive::class.java)
            startActivity(intent)
        }
    }

    fun request() {
        checkForPermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            "location",
            FINE_LOCATION
        )
        checkForPermission(
            android.Manifest.permission.ACTIVITY_RECOGNITION,
            "activity",
            ACTIVITY_RECO
        )
    }

    private fun checkForPermission(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(applicationContext, permission)
                        == PackageManager.PERMISSION_GRANTED -> {

                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(
                    permission,
                    name,
                    requestCode
                )
                else -> ActivityCompat.requestPermissions(this, str_permissions, requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fun innerCheck(name: String) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                for (permission in permissions) {
                    checkForPermission(permission, name, requestCode)
                }
                Log.i("Connection", "$name permission granted")
            } else {
                Log.i("Connection", "$name permission refused")
            }
        }

        when (requestCode) {
            FINE_LOCATION -> innerCheck("location")
            ACTIVITY_RECO -> innerCheck("activity")
        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Permission to access your $name is required to use the app")
            setTitle("Permission Required")
            setPositiveButton("Allow") { dialog, which ->
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(permission),
                    requestCode
                )
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

}


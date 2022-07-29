package com.life360.bluetoothpermissionssampleapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.life360.bluetoothpermissionssampleapp.databinding.ActivityMainBinding


class ManActivity  : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonTurnOnBluetooth.setOnClickListener {
            enableBluetooth()
        }

        binding.buttonPermissionRequest.setOnClickListener {
            requestPermission()
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result: Map<String, Boolean> ->
            result.entries.forEach {
                Log.d("BT_Sample", "${it.key} = ${it.value}")
            }
            updateSettingsState()
        }

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        updateSettingsState()
    }

    private fun isAndroid12(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    private fun getInfo(): String {
        return "device API version: API-${Build.VERSION.SDK_INT} (Android-${Build.VERSION.RELEASE_OR_CODENAME})" +
                "\nApplication targetSDKVersion: API-${applicationContext.applicationInfo.targetSdkVersion}"
    }

    private fun updateSettingsState() {
        binding.permissionState.text = getPermissionStateMessage()
    }

    private fun isBtScanGrantedPermission(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    }

    private fun isBtConnectGrantedPermission(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    private fun isBtEnabled(): Boolean {
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter.isEnabled
    }

    private fun getHtmlTextForResult(value: Boolean): String {
        return if (value) "<font color='green'>TRUE</font>" else "<font color='red'>FALSE</font>"
    }

    private fun getPermissionStateMessage(): Spanned {
        val html = if (isAndroid12()) {
            "Bluetooth Adapter Enabled? - <b>${getHtmlTextForResult(isBtEnabled())}</b>\n" +
            "BLUETOOTH_SCAN Granted? - <b>${getHtmlTextForResult(isBtScanGrantedPermission())}</b>\n" +
                    "BLUETOOTH_CONNECT Granted? - <b>${getHtmlTextForResult(isBtConnectGrantedPermission())}</b>"
        } else {
            "Bluetooth check required for Android-12 and above devices only"
        }
        return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    }

    private fun requestPermission() {
        if (isAndroid12()) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            Toast.makeText(this, "Available for Android 12 and above", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableBluetooth() {
        try {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        } catch (error: SecurityException) {
            Toast.makeText(this, "Please request BLUETOOTH_CONNECT permission first", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.info.text = getInfo()
        updateSettingsState()
    }
}

package com.example.robotspotifyapp

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.UUID

// MUST MATCH YOUR ESP32 UUIDS EXACTLY
val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
val CHARACTERISTIC_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")

class BleHelper(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private var bluetoothGatt: BluetoothGatt? = null

    @SuppressLint("MissingPermission")
    fun connectAndSend(emotionId: String) {
        Log.d("BLE", "=== STARTING BLE CONNECTION ===")
        Log.d("BLE", "Trying to send emotion ID: $emotionId")
        
        // Safety checks
        if (bluetoothAdapter == null) {
            Log.e("BLE", "❌ Bluetooth adapter is NULL")
            GlobalDebug.lastError += " | BLE: No Adapter"
            return
        }
        
        if (!bluetoothAdapter.isEnabled) {
            Log.e("BLE", "❌ Bluetooth is DISABLED")
            GlobalDebug.lastError += " | BLE: Bluetooth OFF"
            return
        }

        val scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null) {
            Log.e("BLE", "❌ BLE Scanner is NULL")
            GlobalDebug.lastError += " | BLE: Scanner NULL"
            return
        }

        // Stop previous connection if exists to avoid "ghost" connections
        bluetoothGatt?.close()
        bluetoothGatt = null

        Log.d("BLE", "✅ Starting scan for MOCHI_ROBOT...")
        GlobalDebug.lastError += " | BLE: Scanning..."

        // 1. SCAN FOR ROBOT
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                val device = result?.device
                val deviceName = device?.name
                
                Log.d("BLE", "Found device: $deviceName")

                // Check if this is our robot
                if (deviceName == "MOCHI_ROBOT") {
                    Log.d("BLE", "🎉 Found MOCHI_ROBOT! Connecting...")
                    GlobalDebug.lastError += " | BLE: Found Robot!"
                    scanner.stopScan(this) // Stop scanning immediately
                    connectToDevice(device!!, emotionId)
                } else {
                    Log.d("BLE", "Ignoring device: $deviceName")
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BLE", "❌ Scan failed with error: $errorCode")
                GlobalDebug.lastError += " | BLE: Scan Failed ($errorCode)"
            }
        }

        scanner.startScan(scanCallback)

        // Stop scanning after 10 seconds if not found (increased timeout)
        Handler(Looper.getMainLooper()).postDelayed({
            Log.w("BLE", "⏰ Scan timeout - stopping scan")
            scanner.stopScan(scanCallback)
            GlobalDebug.lastError += " | BLE: Timeout"
        }, 10000)
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice, dataToSend: String) {
        Log.d("BLE", "Connecting to device: ${device.name}")
        
        // 2. CONNECT
        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d("BLE", "✅ Connected! Discovering services...")
                        GlobalDebug.lastError += " | BLE: Connected!"
                        gatt?.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d("BLE", "❌ Disconnected")
                        GlobalDebug.lastError += " | BLE: Disconnected"
                    }
                    else -> {
                        Log.d("BLE", "Connection state: $newState, status: $status")
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLE", "✅ Services discovered!")
                    val service = gatt?.getService(SERVICE_UUID)
                    
                    if (service == null) {
                        Log.e("BLE", "❌ Service not found!")
                        GlobalDebug.lastError += " | BLE: Service Not Found"
                        return
                    }
                    
                    val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)

                    if (characteristic != null) {
                        Log.d("BLE", "✅ Characteristic found! Sending data...")
                        // 3. WRITE DATA
                        characteristic.value = dataToSend.toByteArray()
                        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        val success = gatt.writeCharacteristic(characteristic)
                        
                        if (success) {
                            Log.d("BLE", "🚀 Successfully sent emotion ID: $dataToSend")
                            GlobalDebug.lastError += " | BLE: Sent $dataToSend ✅"
                        } else {
                            Log.e("BLE", "❌ Failed to write characteristic")
                            GlobalDebug.lastError += " | BLE: Write Failed"
                        }
                    } else {
                        Log.e("BLE", "❌ Characteristic not found!")
                        GlobalDebug.lastError += " | BLE: Characteristic Not Found"
                    }
                } else {
                    Log.e("BLE", "❌ Service discovery failed: $status")
                    GlobalDebug.lastError += " | BLE: Discovery Failed"
                }
            }
        })
    }
}
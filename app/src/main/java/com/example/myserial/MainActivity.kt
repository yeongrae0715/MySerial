package com.example.myserial

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface


class MainActivity : AppCompatActivity() {

//    Declares
    lateinit var editText: EditText
    lateinit var sendButton: Button
    lateinit var stopSendButton: Button
    lateinit var connectButton: Button
    lateinit var disconnectButton: Button
    lateinit var resultView: TextView
    lateinit var threadView: TextView
    var data:Int? = null
    var toExit:Boolean = false

    lateinit var usbManager: UsbManager
    var device:UsbDevice? = null
    var serial:UsbSerialDevice? = null
    var connection:UsbDeviceConnection? = null

    val ACTION_USB_PERMISSION = "permission"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        Init:
        editText = findViewById(R.id.edittext)
        sendButton = findViewById(R.id.sendbutton)
        stopSendButton = findViewById(R.id.stopsendbutton)
        connectButton = findViewById(R.id.connectButton)
        disconnectButton = findViewById(R.id.disconnectButton)
        resultView = findViewById(R.id.resultview)
        threadView = findViewById(R.id.threadview)
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

//        Connect to usb device
        connectButton.setOnClickListener { startUsbConnection() }
        disconnectButton.setOnClickListener { disconnect() }
        sendButton.setOnClickListener { toExit = true }
        stopSendButton.setOnClickListener { toExit = false }


//        Filter
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
//        registerReceiver(broadcastReceiver, filter)

//        Run the thread : send data
        val thread = sendDataAsync()
        thread.start()

    }

//    Send data to connected device
    private fun sendData(input: String) {
        serial?.write(input.toByteArray())
        Log.i("serial", "sending data: " + input.toByteArray())
    }

//    Disconnect
    private fun disconnect() {
        serial?.close()
    }

//    Connect to attached device(android-powered)
    private fun startUsbConnection() {
        try {
            val usbDevices: HashMap<String, UsbDevice>? = usbManager.deviceList
            resultView.setText(usbDevices.toString())
            if (!usbDevices?.isEmpty()!!) {
                var keep = true
                usbDevices.forEach { entry ->
                    device = entry.value
                    val deviceVendorId: Int? = device?.vendorId
                    Log.i("serial", "vendorId: " + deviceVendorId)
                    if (deviceVendorId == 9025) {
                        val intent: PendingIntent = PendingIntent.getBroadcast(
                            this, 0, Intent(
                                ACTION_USB_PERMISSION
                            ), 0
                        )
                        usbManager.requestPermission(device, intent)
                        keep = false
                        Log.i("serial", "connection successful")
                        resultView.setText("connection successful")
                    } else {
                        connection = null
                        device = null
                        Log.i("serial", "unable to connect")
                        resultView.setText("unable to connect.\ndevice vendor id: " + deviceVendorId)
                    }
                    if (!keep) {
                        return
                    }
                }
            } else {
                Toast.makeText(applicationContext, "no usb device connected", Toast.LENGTH_SHORT).show()
                Log.i("serial", "no usb device connected")
            }
        } catch (e: Exception){
            Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }


//    private val broadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if (intent?.action!! == ACTION_USB_PERMISSION) {
//                val granted: Boolean = intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
//                if (granted) {
//                    connection = usbManager.openDevice(device)
//                    serial = UsbSerialDevice.createUsbSerialDevice(device, connection)
//                    if (serial != null) {
//                        if (serial!!.open()) {
//                            serial!!.setBaudRate(9600)
//                            serial!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
//                            serial!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
//                            serial!!.setParity(UsbSerialInterface.PARITY_NONE)
//                            serial!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
//                        } else {
//                            Log.i("Serial", "port not open")
//                            Toast.makeText(applicationContext, "port not open", Toast.LENGTH_SHORT).show()
//                        }
//                    } else {
//                        Log.i("Serial", "port is null")
//                        Toast.makeText(applicationContext, "port is null", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Log.i("Serial", "permission not granted")
//                    Toast.makeText(applicationContext, "permission not granted", Toast.LENGTH_SHORT).show()
//                }
//            } else if (intent.action == UsbManager.ACTION_USB_ACCESSORY_ATTACHED) {
//                startUsbConnection()
//            } else if (intent.action == UsbManager.ACTION_USB_ACCESSORY_DETACHED) {
//                disconnect()
//            }
//        }
//    }

    inner class sendDataAsync:Thread() {
        override fun run() {
            var num = 0
            try {
                while (true){
                    if (toExit){
                        num++
                        threadView.setText(num.toString())
                        sendData(editText.text.toString())
                        Thread.sleep(500)
                    }

                }
            }
            catch (e:java.lang.Exception) {
                Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
            }

        }
    }

}
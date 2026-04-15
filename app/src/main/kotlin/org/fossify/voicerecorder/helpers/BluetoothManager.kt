package org.fossify.voicerecorder.helpers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log

class BluetoothManagerHelper(private val context: Context) {

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        manager?.adapter
    }

    @SuppressLint("MissingPermission")
    fun isBluetoothAvailable(): Boolean {
        val result = bluetoothAdapter?.isEnabled == true
        Log.d(TAG, "isBluetoothAvailable: $result (adapter=${bluetoothAdapter != null})")
        return result
    }

    @SuppressLint("MissingPermission")
    fun hasBluetoothHeadset(): Boolean {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            Log.d(TAG, "hasBluetoothHeadset: no adapter")
            return false
        }
        if (!adapter.isEnabled) {
            Log.d(TAG, "hasBluetoothHeadset: adapter not enabled")
            return false
        }

        return try {
            val bondedDevices = adapter.bondedDevices
            val result = bondedDevices.any { device ->
                val type = device.type
                type == BluetoothDevice.DEVICE_TYPE_CLASSIC || type == BluetoothDevice.DEVICE_TYPE_DUAL
            }
            Log.d(TAG, "hasBluetoothHeadset: $result (bondedDevices=${bondedDevices.size})")
            bondedDevices.forEach { device ->
                Log.d(TAG, "  bonded device: name=${device.name}, type=${device.type}, bondState=${device.bondState}")
            }
            result
        } catch (e: SecurityException) {
            Log.w(TAG, "hasBluetoothHeadset: permission denied", e)
            false
        } catch (e: Exception) {
            Log.w(TAG, "hasBluetoothHeadset: exception", e)
            false
        }
    }

    fun getAvailableAudioInputDevices(): List<AudioDeviceInfo> {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        Log.d(TAG, "getAvailableAudioInputDevices: ${devices.size} total input devices")
        devices.forEach { device ->
            Log.d(TAG, "  input device: type=${device.type} (${audioDeviceTypeToString(device.type)}), productName=${device.productName}, address=${device.address}")
        }
        return devices.toList()
    }

    fun getBluetoothAudioInputDevices(): List<AudioDeviceInfo> {
        val allDevices = getAvailableAudioInputDevices()
        val btDevices = allDevices.filter { device ->
            device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
            device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            device.type == 26 /* TYPE_BLE_HEADSET on API 31+ */
        }
        Log.d(TAG, "getBluetoothAudioInputDevices: ${btDevices.size} bluetooth input devices found")
        btDevices.forEach { device ->
            Log.d(TAG, "  bt input: type=${device.type}, productName=${device.productName}")
        }
        return btDevices
    }

    fun hasBluetoothAudioInputDevice(): Boolean {
        val result = getBluetoothAudioInputDevices().isNotEmpty()
        Log.d(TAG, "hasBluetoothAudioInputDevice: $result")
        return result
    }

    fun setCommunicationDevice(device: AudioDeviceInfo): Boolean {
        val result = audioManager.setCommunicationDevice(device)
        Log.d(TAG, "setCommunicationDevice: $result (type=${device.type}, name=${device.productName})")
        return result
    }

    fun clearCommunicationDevice() {
        Log.d(TAG, "clearCommunicationDevice")
        audioManager.clearCommunicationDevice()
    }

    fun getCurrentCommunicationDevice(): AudioDeviceInfo? {
        return audioManager.communicationDevice
    }

    private fun audioDeviceTypeToString(type: Int): String = when (type) {
        AudioDeviceInfo.TYPE_BUILTIN_MIC -> "BUILTIN_MIC"
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "BLUETOOTH_SCO"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "BLUETOOTH_A2DP"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "WIRED_HEADSET"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "WIRED_HEADPHONES"
        AudioDeviceInfo.TYPE_USB_DEVICE -> "USB_DEVICE"
        AudioDeviceInfo.TYPE_USB_ACCESSORY -> "USB_ACCESSORY"
        AudioDeviceInfo.TYPE_TELEPHONY -> "TELEPHONY"
        AudioDeviceInfo.TYPE_FM_TUNER -> "FM_TUNER"
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "BUILTIN_SPEAKER"
        AudioDeviceInfo.TYPE_REMOTE_SUBMIX -> "REMOTE_SUBMIX"
        AudioDeviceInfo.TYPE_BLE_HEADSET -> "BLE_HEADSET"
        AudioDeviceInfo.TYPE_HDMI -> "HDMI"
        AudioDeviceInfo.TYPE_HDMI_ARC -> "HDMI_ARC"
        AudioDeviceInfo.TYPE_BLE_SPEAKER -> "BLE_SPEAKER"
        else -> "UNKNOWN($type)"
    }

    companion object {
        private const val TAG = "BluetoothManager"
        fun needsBluetoothConnectPermission(): Boolean {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
        }
    }
}

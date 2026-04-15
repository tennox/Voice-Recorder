package org.fossify.voicerecorder.helpers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager

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
        return bluetoothAdapter?.isEnabled == true
    }

    @SuppressLint("MissingPermission")
    fun hasBluetoothHeadset(): Boolean {
        val adapter = bluetoothAdapter ?: return false
        if (!adapter.isEnabled) return false

        return try {
            val bondedDevices = adapter.bondedDevices
            bondedDevices.any { device ->
                val type = device.type
                type == BluetoothDevice.DEVICE_TYPE_CLASSIC || type == BluetoothDevice.DEVICE_TYPE_DUAL
            }
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun getConnectedBluetoothDevices(): List<BluetoothDevice> {
        val adapter = bluetoothAdapter ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()

        return try {
            adapter.bondedDevices.filter { device ->
                try {
                    device.connectionState == BluetoothDevice.CONNECTION_STATE_CONNECTED
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAvailableAudioInputDevices(): List<AudioDeviceInfo> {
        return audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
    }

    fun getBluetoothAudioInputDevices(): List<AudioDeviceInfo> {
        return getAvailableAudioInputDevices().filter { device ->
            device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
            device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            device.type == AudioDeviceInfo.TYPE_BLUETOOTH_LE_AUDIO
        }
    }

    fun hasBluetoothAudioInputDevice(): Boolean {
        return getBluetoothAudioInputDevices().isNotEmpty()
    }

    fun setCommunicationDevice(device: AudioDeviceInfo): Boolean {
        return audioManager.setCommunicationDevice(device)
    }

    fun clearCommunicationDevice() {
        audioManager.clearCommunicationDevice()
    }

    fun getCurrentCommunicationDevice(): AudioDeviceInfo? {
        return audioManager.communicationDevice
    }

    companion object {
        fun needsBluetoothConnectPermission(): Boolean {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
        }
    }
}

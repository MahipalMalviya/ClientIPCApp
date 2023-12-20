package com.mahipal.ipc.clientapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import com.mahipal.ipc.aidl.IMyAidlInterface

class ClientActivity : AppCompatActivity() {

    private var aidlInterface : IMyAidlInterface? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            Log.d("ClientActivity", "Service connected")
            try {
                aidlInterface = IMyAidlInterface.Stub.asInterface(service)

                val data = aidlInterface?.response
                Log.d("ClientActivity", "received response from server : $data")

                findViewById<TextView>(R.id.tvClient).text = "Server Response: $data"
            } catch(ex: Exception) {
                ex.printStackTrace()
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.d("ClientActivity", "Service disconnected")
            aidlInterface = null
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)
        bindServer()
    }

    private fun bindServer() {
        try {
            val implictIntent = Intent("interface.aidl")
            val explictIntent = convertImplictToExplictIntent(implictIntent)
            explictIntent?.let { intent ->
                bindService(explictIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun convertImplictToExplictIntent(implictIntent: Intent): Intent? {
        val resolveInfos = this.packageManager?.queryIntentServices(implictIntent, 0)
        if (resolveInfos == null || resolveInfos.size == 0) {
            return null
        }
        val resolveInfo = resolveInfos[0]
        val componentName = ComponentName(resolveInfo.serviceInfo.packageName,resolveInfo.serviceInfo.name)
        val explictIntent = Intent(implictIntent)
        explictIntent.component = componentName
        return explictIntent
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}
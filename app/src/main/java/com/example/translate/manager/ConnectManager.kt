package com.example.translate.manager

import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.util.Base64
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.translate.info.ConnectInfo
import com.example.translate.info.CountryPerInfo
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.utils.StartService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.*

class ConnectManager(
    private val activity: AppCompatActivity, private val listener: OnConnectListener
) : ShadowsocksConnection.Callback {

    companion object {
        const val TAG = "ConnectManager"
        var mConnectState = BaseService.State.Idle
        var mCurrentProfile: Profile? = null
        var mConnectInfo: ConnectInfo? = null
    }

    private var mState = -1

    private val mConnection = ShadowsocksConnection(true)

    private val mHandler = Handler(Looper.getMainLooper())

    private var mConnectLifecycle = ConnectLifecycle(activity) {
        listener.onObtainPermissionFail()
    }

    init {
        activity.lifecycle.addObserver(mConnectLifecycle)
    }

    fun startConnect() {
        if (mState == -1) {
            mState = 0
            mConnection.connect(activity, this)
        }
    }

    /**
     * 切换链接
     */
    fun switchConnect() {
        if (mConnectState.canStop) {
            Core.stopService()
        } else {
            GlobalScope.launch {
                mCurrentProfile = getConnectProfile()
                if (mCurrentProfile == null) {
                    withContext(Dispatchers.Main) {
                        onStateChange(BaseService.State.Connecting)
                        onStateChange(BaseService.State.Idle)
                    }
                    return@launch
                }
                Core.currentProfile = ProfileManager.expand(mCurrentProfile!!)
                Logger.d({ TAG }, { "profile: $mCurrentProfile" })
                withContext(Dispatchers.Main) {
                    Logger.d({ TAG }, { "start connect!!!" })
                    mConnectLifecycle.launch()
                }
            }
        }
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        Logger.d({ TAG }, { "stateChanged: state: $state profileName: $profileName" })
        onStateChange(state)
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        mState = 1
        mConnectState = try {
            BaseService.State.values()[service.state]
        } catch (_: RemoteException) {
            BaseService.State.Idle
        }
        if (mConnectState == BaseService.State.Connected) {
            mCurrentProfile = getCurrentProfile(service.profileName)
        }
        listener.onObtainStateComplete()
        setUserDz()
    }

    override fun onServiceDisconnected() {
        super.onServiceDisconnected()
        mState = -1

    }

    override fun onBinderDied() {
        mConnection.disconnect(activity)
        mState = -1
        startConnect()
    }

    /**
     * 链接状态改变
     */
    private fun onStateChange(state: BaseService.State) {
        mConnectState = state
        setUserDz()
        Logger.d({ TAG }, { "connect state change: $state" })
        if (state == BaseService.State.Connected) {
            mHandler.postDelayed({
                listener.onConnectStateChange(state)
            }, 800)
        } else {
            listener.onConnectStateChange(state)
        }
    }

    private fun setUserDz() {
//        if (state == BaseService.State.Connected) {
//            Core.currentProfile
//            val host = mCurrentProfile?.host
//            Logger.d({ TAG }, { "host: $host" })
//            if (host.isNullOrEmpty()) {
//                FirebaseAnalyticsHelper.instance.setUserDz()
//            } else {
//                FirebaseAnalyticsHelper.instance.setUserDz(host)
//            }
//        } else if (state == BaseService.State.Idle || state == BaseService.State.Stopped) {
//            FirebaseAnalyticsHelper.instance.setUserDz()
//        }
    }

    interface OnConnectListener {
        fun onObtainPermissionFail()
        fun onObtainStateComplete()
        fun onConnectStateChange(state: BaseService.State)
    }

    private inner class ConnectLifecycle(
        private val activity: AppCompatActivity, private val permissionDenied: () -> Unit
    ) : DefaultLifecycleObserver {

        private var mConnectResult: ActivityResultLauncher<Void?>? = null

        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            mConnectResult =
                activity.activityResultRegistry.register("start_service", StartService()) {
                    if (it) permissionDenied()
                }
        }

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            mConnection.bandwidthTimeout = 500
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            mConnection.bandwidthTimeout = 0
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            mConnectResult?.unregister()
            mConnection.disconnect(activity)
        }

        fun launch() = mConnectResult?.launch(null)
    }

    private fun getCurrentProfile(name: String): Profile? {
        if (mConnectInfo == null) {
            mConnectInfo = parseConnectInfo()
        }
        val list = mConnectInfo?.profileList
        if (list.isNullOrEmpty()) return null
        for (profile in list) {
            if (profile.name == name) {
                return profile
            }
        }
        return null
    }

    private suspend fun getConnectProfile(): Profile? {
        val connectInfo = parseConnectInfo()
        Logger.d({ TAG }, { "profileList: $connectInfo" })
        if (connectInfo == null || connectInfo.profileList.isEmpty()) return null
        mConnectInfo = connectInfo
        val profile: Profile?
        if (connectInfo.countryPerList.isEmpty()) {
            val num = Random().nextInt(connectInfo.profileList.size)
            profile = connectInfo.profileList[num]
            return profile
        }
        var num = 0
        connectInfo.countryPerList.forEach {
            num += it.per
        }
        val n = RandomManager.random(num)
        Logger.d({ TAG }, { "n: $n" })
        num = 0
        var code = ""
        for (it in connectInfo.countryPerList) {
            num += it.per
            if (n <= num) {
                code = it.country
                break
            }
        }
        Logger.d({ TAG }, { "code: $code" })
        val list = mutableListOf<Profile>()
        if (code.isNotEmpty()) {
            connectInfo.profileList.forEach {
                if (it.code == code) {
                    list.add(it)
                }
            }
            if (list.size == 1) {
                profile = list[0]
                return profile
            }
        }
        if (list.isEmpty()) {
            list.addAll(connectInfo.profileList)
        }
        val position = Random().nextInt(list.size)
        profile = list[position]
        return profile
    }

    private fun parseConnectInfo(): ConnectInfo? {
        var connectInfo: ConnectInfo? = null
        try {
            val services = ConfigManager.instance.getConnects()
            Logger.d({ TAG }, { "services: $services" })
            if (services.isEmpty()) {
                return null
            }
            val profileList = mutableListOf<Profile>()
            val json = JSONObject(
                String(
                    Base64.decode(
                        services, Base64.NO_WRAP
                    )
                )
            )
            val serviceArray = json.getJSONArray("s")
            for (i in 0 until serviceArray.length()) {
                val serviceJson = serviceArray.getJSONObject(i)
                profileList.add(Profile().apply {
                    code = serviceJson.getString("c").lowercase()
                    name = serviceJson.getString("n")
                    host = serviceJson.getString("a")
                    remotePort = serviceJson.getInt("p")
                    password = serviceJson.getString("ps")
                })
            }

            val perArray = json.getJSONArray("prs")
            if (perArray.length() <= 0) return null
            val countryPerInfoList = mutableListOf<CountryPerInfo>()
            for (i in 0 until perArray.length()) {
                val perJson = perArray.getJSONObject(i)
                countryPerInfoList.add(
                    CountryPerInfo(
                        perJson.getString("c").lowercase(), perJson.getInt("pr")
                    )
                )
            }
            connectInfo = ConnectInfo(profileList, countryPerInfoList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return connectInfo
    }
}
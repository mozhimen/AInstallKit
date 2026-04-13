package com.mozhimen.installk.builder.basic.bases

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.mozhimen.installk.builder.basic.commons.IInstallKBuilder
import com.mozhimen.installk.builder.basic.commons.IInstallKStateListener
import com.mozhimen.installk.builder.basic.cons.EInstallKMessage
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @ClassName BaseInstallKBuilder
 * @Description TODO
 * @Author mozhimen
 * @Date 2026/4/2
 * @Version 1.0
 */
abstract class BaseInstallKBuilder : IInstallKBuilder {
    protected var _iInstallStateChangeListener: IInstallKStateListener? = null

    fun setInstallStateChangeListener(listener: IInstallKStateListener?): BaseInstallKBuilder {
        _iInstallStateChangeListener = listener
        return this
    }

    /**
     * 安装
     * @param strPathNameApk String
     */
    override suspend fun install_suspend(strPathNameApk: String) {
        withContext(Dispatchers.Main) {
            install(strPathNameApk)
        }
    }

    override fun install(strPathNameApk: String) {
        try {
            sendMessage(EInstallKMessage.MSG_INSTALL_START)
            tryInstall(strPathNameApk)
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "install: ${e.message}")
            sendMessage(EInstallKMessage.MSG_INSTALL_FAIL(e.message?:""))
        } finally {
            sendMessage(EInstallKMessage.MSG_INSTALL_FINISH)
        }
    }

    protected val _handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                EInstallKMessage.Message.MSG_INSTALL_START -> _iInstallStateChangeListener?.onInstallStart()
                EInstallKMessage.Message.MSG_INSTALL_FINISH -> _iInstallStateChangeListener?.onInstallFinish()
                EInstallKMessage.Message.MSG_INSTALL_FAIL -> _iInstallStateChangeListener?.onInstallFail(msg.obj as String)
                EInstallKMessage.Message.MSG_REQUIRE_PERMISSION -> _iInstallStateChangeListener?.onRequirePermissions(msg.obj as Array<String>)
            }
        }
    }

    protected fun sendMessage(message: EInstallKMessage) {
        _handler.sendMessage(Message().apply {
            what = message.message
            obj = message.obj
        })
    }
}
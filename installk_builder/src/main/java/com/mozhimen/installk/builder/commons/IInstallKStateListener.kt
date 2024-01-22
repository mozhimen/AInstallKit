package com.mozhimen.installk.builder.commons

import com.mozhimen.installk.builder.cons.EInstallKPermissionType

/**
 * @ClassName IOnInstallStateChangedListener
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2023/1/7 0:26
 * @Version 1.0
 */
interface IInstallKStateListener {
    fun onDownloadStart() {}
    fun onInstallStart()
    fun onInstallFinish()
    fun onInstallFail(msg: String?)
    fun onNeedPermissions(type: EInstallKPermissionType)
}
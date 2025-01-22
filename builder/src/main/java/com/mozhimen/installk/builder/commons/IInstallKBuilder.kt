package com.mozhimen.installk.builder.commons

import com.mozhimen.installk.builder.cons.EInstallKMode

/**
 * @ClassName IInstallK
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/5/17 18:21
 * @Version 1.0
 */
interface IInstallKBuilder {
    fun setInstallMode(mode: EInstallKMode): IInstallKBuilder
    fun setInstallSilenceReceiver(receiverClazz: Class<*>): IInstallKBuilder
    fun setInstallSmartService(serviceClazz: Class<*>): IInstallKBuilder
    fun setInstallStateChangeListener(listener: IInstallKStateListener): IInstallKBuilder
}
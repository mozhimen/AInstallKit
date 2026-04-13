package com.mozhimen.installk.builder.basic.commons

import com.mozhimen.kotlin.utilk.commons.IUtilK

/**
 * @ClassName IInstallKBuilder
 * @Description TODO
 * @Author mozhimen
 * @Date 2026/4/2
 * @Version 1.0
 */
interface IInstallKBuilder : IUtilK {
    suspend fun install_suspend(strPathNameApk: String)
    fun install(strPathNameApk: String)
    fun tryInstall(strPathNameApk: String): Boolean
}
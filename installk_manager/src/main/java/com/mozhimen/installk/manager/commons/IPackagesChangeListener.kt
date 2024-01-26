package com.mozhimen.installk.manager.commons

import android.content.pm.PackageInfo

/**
 * @ClassName IPackagesChangeListener
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/1/26
 * @Version 1.0
 */
interface IPackagesChangeListener {
    fun onPackageAdd(packageInfo: PackageInfo) {}
    fun onPackageRemove(packageInfo: PackageInfo) {}
}
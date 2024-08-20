package com.mozhimen.installk.manager.commons

/**
 * @ClassName IPackagesChangeListener
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/1/26
 * @Version 1.0
 */
interface IPackagesChangeListener {
    fun onPackageAdd(packageName: String, versionCode: Int) {}
    fun onPackageRemove(packageName: String) {}
}
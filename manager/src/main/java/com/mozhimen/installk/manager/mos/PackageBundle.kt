package com.mozhimen.installk.manager.mos

import android.content.pm.PackageInfo

/**
 * @ClassName PackageBundle
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/19
 * @Version 1.0
 */
data class PackageBundle constructor(
    val packageName: String,
    var versionCode: Int
)
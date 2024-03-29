package com.mozhimen.installk.builder.test

import android.view.View
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVDB
import com.mozhimen.basick.utilk.android.content.startContext
import com.mozhimen.installk.builder.test.databinding.ActivityMainBinding

class MainActivity : BaseActivityVDB<ActivityMainBinding>() {
    fun goInstallK(view: View) {
        startContext<InstallKBuilderActivity>()
    }
}
package com.mozhimen.installk.builder.test

import android.view.View
import com.mozhimen.uik.databinding.bases.activity.databinding.BaseActivityVDB
import com.mozhimen.kotlin.utilk.android.content.startContext
import com.mozhimen.installk.builder.test.databinding.ActivityMainBinding

class MainActivity : BaseActivityVDB<ActivityMainBinding>() {
    fun goInstallK(view: View) {
        startContext<InstallKBuilderActivity>()
    }
}
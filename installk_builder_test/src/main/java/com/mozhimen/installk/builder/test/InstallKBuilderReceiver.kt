package com.mozhimen.installk.builder.test

import com.mozhimen.basick.elemk.android.content.bases.BasePackageBroadcastReceiver
import com.mozhimen.basick.lintk.optin.OptInApiTarget_AtV_25_71_N1

/**
 * @ClassName InstallReceiver
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/1/13 17:32
 * @Version 1.0
 */
@OptIn(OptInApiTarget_AtV_25_71_N1::class)
class InstallKBuilderReceiver : BasePackageBroadcastReceiver()
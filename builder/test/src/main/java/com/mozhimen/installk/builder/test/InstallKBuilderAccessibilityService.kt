package com.mozhimen.installk.builder.test

import com.mozhimen.installk.builder.auto.InstallKBuilderSmartAccessibilityService
import com.mozhimen.kotlin.lintk.optins.manifest.application.service.OService_ACCESSIBILITY


/**
 * @ClassName InstallKService
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/1/13 16:49
 * @Version 1.0
 */
@OptIn(OService_ACCESSIBILITY::class)
class InstallKBuilderAccessibilityService : com.mozhimen.installk.builder.auto.InstallKBuilderSmartAccessibilityService()
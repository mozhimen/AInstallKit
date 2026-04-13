package com.mozhimen.installk.builder.basic.cons

/**
 * @ClassName EInstallMode
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2023/1/7 0:05
 * @Version 1.0
 */
enum class EInstallKType {
    ROOT,//root安装
    SILENCE,//静默安装//可能需要自己退出再点开
    AUTO,//无障碍
    HAND,//手动安装
    SMART
}
package com.mozhimen.installk.builder.basic.cons

import java.security.Permissions

/**
 * @ClassName EInstallKMessage
 * @Description TODO
 * @Author mozhimen
 * @Date 2026/4/2
 * @Version 1.0
 */
sealed class EInstallKMessage(val message: Int, val obj: Any) {
    object MSG_INSTALL_START : EInstallKMessage(Message.MSG_INSTALL_START, "")
    object MSG_INSTALL_FINISH : EInstallKMessage(Message.MSG_INSTALL_FINISH, "")
    data class MSG_INSTALL_FAIL(val error: String) : EInstallKMessage(Message.MSG_INSTALL_FAIL, error)
    data class MSG_REQUIRE_PERMISSION(val permissions: Array<String>) : EInstallKMessage(Message.MSG_REQUIRE_PERMISSION, permissions)

    object Message {
        const val MSG_INSTALL_START = 1
        const val MSG_INSTALL_FINISH = 2
        const val MSG_INSTALL_FAIL = -2
        const val MSG_REQUIRE_PERMISSION = -1
    }
}
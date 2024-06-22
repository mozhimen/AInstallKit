package com.mozhimen.installk.splits.ackpine.cons

import ru.solrudev.ackpine.session.Failure
import java.util.UUID

/**
 * @ClassName SInstallState
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
sealed class SInstallState {
    data class Success(val id: UUID) : SInstallState()
    data class Fail(val id: UUID?, val failure: Failure?) : SInstallState()
    data class Progress(val id: UUID, val progress: Int, val max: Int) : SInstallState()
}
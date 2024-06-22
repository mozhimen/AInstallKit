package com.mozhimen.installk.splits.ackpine.utils

import android.net.Uri
import ru.solrudev.ackpine.exceptions.SplitPackageException
import ru.solrudev.ackpine.splits.Apk

/**
 * @ClassName InstallSplitsAckpineUtil
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
fun Sequence<Apk>.toUrisList(): List<Uri> =
    InstallSplitsAckpineUtil.toUrisList(this)

//////////////////////////////////////////////////////////////////

object InstallSplitsAckpineUtil {
    @JvmStatic
    fun toUrisList(apks: Sequence<Apk>): List<Uri> =
        try {
            apks.map { it.uri }.toList()
            /*} catch (exception: SplitPackageException) {
    //            val errorString = when (exception) {
    //                is NoBaseApkException -> NotificationString.resource(R.string.error_no_base_apk)
    //                is ConflictingBaseApkException -> NotificationString.resource(R.string.error_conflicting_base_apk)
    //                is ConflictingSplitNameException -> NotificationString.resource(
    //                    R.string.error_conflicting_split_name,
    //                    exception.name
    //                )
    //
    //                is ConflictingPackageNameException -> NotificationString.resource(
    //                    R.string.error_conflicting_package_name,
    //                    exception.expected, exception.actual, exception.name
    //                )
    //
    //                is ConflictingVersionCodeException -> NotificationString.resource(
    //                    R.string.error_conflicting_version_code,
    //                    exception.expected, exception.actual, exception.name
    //                )
    //            }
    //            error.value = errorString
                return emptyList()*/
        } catch (exception: Exception) {
//            error.value = NotificationString.raw(exception.message.orEmpty())
            emptyList()
        }
}
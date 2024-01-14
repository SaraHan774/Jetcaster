package com.august.jetcaster.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Find the closest Activity in a given Context.
 *
 * reference : https://github.com/google/accompanist/blob/a9506584939ed9c79890adaaeb58de01ed0bb823/permissions/src/main/java/com/google/accompanist/permissions/PermissionsUtil.kt#L132
 */
internal fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("$context is not an activity!")
}
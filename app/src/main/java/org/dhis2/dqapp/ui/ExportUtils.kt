package org.dhis2.dqapp.ui

import android.content.Context
import android.net.Uri

fun exportCsvToUri(
    context: Context,
    csvContent: String,
    outputUri: Uri,
    onSuccess: (String) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    try {
        val stream = context.contentResolver.openOutputStream(outputUri, "w")
            ?: run {
                onError("Cannot open selected file for Excel export.")
                return
            }
        stream.use {
            it.write(csvContent.toByteArray(Charsets.UTF_8))
            it.flush()
        }
        onSuccess("Excel-compatible CSV exported successfully.")
    } catch (ex: Exception) {
        onError(ex.message ?: "Excel export failed.")
    }
}

package org.dhis2.dqapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.dhis2.dqapp.AuthMode
import java.io.IOException

class Dhis2Client(
    baseUrl: String,
    private val authMode: AuthMode,
    private val username: String,
    private val password: String,
    private val sessionCookie: String?
) {
    private val base = baseUrl.trimEnd('/')
    private val client = OkHttpClient.Builder().build()

    private fun makeUrl(path: String): String {
        return if (path.startsWith("http")) path else "${base}${path}"
    }

    private fun authHeader(): Pair<String, String>? {
        return if (authMode != AuthMode.BASIC) null else {
            val credential = okhttp3.Credentials.basic(username, password)
            "Authorization" to credential
        }
    }

    private fun cookieHeader(): Pair<String, String>? {
        return if (authMode != AuthMode.SESSION) null else {
            if (sessionCookie.isNullOrBlank()) null else "Cookie" to sessionCookie
        }
    }

    private fun Request.Builder.applyAuth(): Request.Builder {
        authHeader()?.let { header(it.first, it.second) }
        cookieHeader()?.let { header(it.first, it.second) }
        return this
    }

    private fun looksLikeHtml(contentType: String?, body: String): Boolean {
        val ct = contentType.orEmpty().lowercase()
        if (ct.contains("text/html")) return true
        val trimmed = body.trimStart()
        return trimmed.startsWith("<!DOCTYPE html", ignoreCase = true) ||
            trimmed.startsWith("<html", ignoreCase = true)
    }

    suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url(makeUrl(path))
            .get()
            .applyAuth()
            .build()

        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw HttpException(res.code, "GET $path failed: ${res.code}")
            val body = res.body?.string() ?: ""
            if (looksLikeHtml(res.header("Content-Type"), body)) {
                throw HttpException(401, "Authentication failed.")
            }
            body
        }
    }

    suspend fun getMaybe(path: String): String? = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url(makeUrl(path))
            .get()
            .applyAuth()
            .build()

        client.newCall(req).execute().use { res ->
            if (res.code == 404) return@withContext null
            if (!res.isSuccessful) throw HttpException(res.code, "GET $path failed: ${res.code}")
            val body = res.body?.string()
            if (body != null && looksLikeHtml(res.header("Content-Type"), body)) {
                throw HttpException(401, "Authentication failed.")
            }
            body
        }
    }

    suspend fun put(path: String, jsonBody: String): Boolean = withContext(Dispatchers.IO) {
        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url(makeUrl(path))
            .put(body)
            .applyAuth()
            .build()

        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) {
                val text = res.body?.string() ?: ""
                throw HttpException(res.code, "PUT $path failed: ${res.code} $text")
            }
            true
        }
    }

    suspend fun post(path: String, jsonBody: String): Boolean = withContext(Dispatchers.IO) {
        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url(makeUrl(path))
            .post(body)
            .applyAuth()
            .build()

        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) {
                val text = res.body?.string() ?: ""
                throw HttpException(res.code, "POST $path failed: ${res.code} $text")
            }
            true
        }
    }
}

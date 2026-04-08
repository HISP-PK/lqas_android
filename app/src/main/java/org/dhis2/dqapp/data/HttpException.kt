package org.dhis2.dqapp.data

import java.io.IOException

class HttpException(val code: Int, message: String) : IOException(message)

package com.example.ps.payu.clients.interceptors

import com.example.ps.payu.common.HEADER_NAME_CORRELATION_ID
import com.example.ps.payu.common.LogContext
import mu.KLogging
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.IOException

class OutboundRequestInterceptor(
    private val logContext: LogContext
) : ClientHttpRequestInterceptor {

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        logContext.correlationId?.let {
            request.headers.add(HEADER_NAME_CORRELATION_ID, it)
        }
        traceRequest(request, body)
        return execution.execute(request, body)
    }

    @Throws(IOException::class)
    private fun traceRequest(request: HttpRequest, body: ByteArray) {
        logger.debug("===========================request begin================================================" +
                "URI         : ${request.uri} " +
                "Method      : ${request.method} " +
                "Headers     : ${request.headers} " +
                "Request body: ${String(body)} " +
                "==========================request end================================================")
    }

    companion object : KLogging()
}

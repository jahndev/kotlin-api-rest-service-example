package com.example.ps.payu.web.error.handlers

import com.newrelic.api.agent.NewRelic
import com.example.example.payment.hateoas.api.v2_1.Action
import com.example.example.payment.hateoas.api.v2_1.ActionResult
import com.example.example.payment.hateoas.api.v2_1.ActionStatus
import com.example.example.payment.hateoas.api.v2_1.ActionType
import com.example.example.payment.hateoas.api.v2_1.HttpMethod
import com.example.example.payment.hateoas.api.v2_1.UserMessage
import com.example.example.payment.hateoas.api.v2_1.UserOperation
import com.example.ps.payu.services.exceptions.NotSupportedPaymentException
import com.example.ps.payu.services.exceptions.PayuCommunicationException
import com.example.ps.payu.services.helpers.UrlProvider
import com.example.ps.payu.web.responses.ErrorResponse
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException

@ControllerAdvice
class GlobalErrorHandler(private val urlProvider: UrlProvider) {

    @ExceptionHandler(HttpClientErrorException.NotFound::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    fun resourceNotFoundException(resourceNotFoundException: HttpClientErrorException.NotFound): ErrorResponse {
        logger.error(
            "Handle NotFoundException and responding with ${HttpStatus.NOT_FOUND}.",
            resourceNotFoundException
        )
        NewRelic.noticeError(resourceNotFoundException)
        return ErrorResponse(resourceNotFoundException.message)
    }

    @ExceptionHandler(
        HttpClientErrorException::class,
        HttpMessageConversionException::class,
        IllegalArgumentException::class
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun clientErrorException(httpClientErrorException: Exception): ErrorResponse {
        logger.error(
            "Handle httpClientException and responding with ${HttpStatus.BAD_REQUEST}.",
            httpClientErrorException
        )
        NewRelic.noticeError(httpClientErrorException)
        return ErrorResponse(httpClientErrorException.message)
    }

    @ExceptionHandler(HttpServerErrorException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun serverErrorException(httpServerErrorException: HttpServerErrorException): ErrorResponse {
        logger.error(
            "Handle httpServerErrorException and responding with ${HttpStatus.INTERNAL_SERVER_ERROR}.",
            httpServerErrorException
        )
        NewRelic.noticeError(httpServerErrorException)
        return ErrorResponse(httpServerErrorException.message)
    }

    @ExceptionHandler(PayuCommunicationException::class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun payuCommunicationException(exception: PayuCommunicationException): ActionResult {
        logger.error(
            "Handle payuCommunicationException and responding with a redirection to payment selection page.",
            exception
        )

        NewRelic.noticeError(exception)

        val uri = urlProvider.createPaymentSelectionPageUrl(exception.payment, UserMessage.TRY_OTHER_METHOD)
        val action = Action(ActionType.REDIRECT, HttpMethod.GET, null, uri)
        return ActionResult(
            status = ActionStatus.ERROR,
            message = UserMessage.TRY_OTHER_METHOD,
            actions = mapOf(UserOperation.SUBMIT to action),
            options = emptyList()
        )
    }

    @ExceptionHandler(NotSupportedPaymentException::class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun notSupportedPaymentException(exception: NotSupportedPaymentException): ActionResult {
        logger.error(exception.message, exception)

        NewRelic.noticeError(exception)

        val uri = urlProvider.createPaymentSelectionPageUrl(exception.payment, UserMessage.TRY_OTHER_METHOD)
        val action = Action(ActionType.REDIRECT, HttpMethod.GET, null, uri)
        return ActionResult(
            status = ActionStatus.ERROR,
            message = UserMessage.TRY_OTHER_METHOD,
            actions = mapOf(UserOperation.SUBMIT to action),
            options = emptyList()
        )
    }

    @ExceptionHandler(RuntimeException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun runtimeException(exception: RuntimeException): ErrorResponse {
        logger.error(
            "Handle runtimeException globally. Responding with ${HttpStatus.INTERNAL_SERVER_ERROR}.",
            exception
        )
        NewRelic.noticeError(exception)
        return ErrorResponse(exception.message)
    }

    companion object : KLogging()
}

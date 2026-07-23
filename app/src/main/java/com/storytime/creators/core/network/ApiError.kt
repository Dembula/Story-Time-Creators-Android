package com.storytime.creators.core.network

sealed class ApiException(message: String) : Exception(message) {
    object InvalidUrl : ApiException("Invalid request URL.")
    object Unauthorized : ApiException("Please sign in again.")
    object Forbidden : ApiException("You do not have access to this resource.")
    class Http(val code: Int, val serverMessage: String?) :
        ApiException(serverMessage?.takeIf { it.isNotEmpty() } ?: "Request failed ($code).")
    class Decoding(detail: String) : ApiException("Could not read server response. $detail")
    class Network(detail: String) : ApiException(detail)
    object EmptyResponse : ApiException("Empty response from server.")
}

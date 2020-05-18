package com.example.ps.payu.common

import java.util.Base64

fun String.base64Encode() = String(Base64.getUrlEncoder().encode(this.toByteArray()))

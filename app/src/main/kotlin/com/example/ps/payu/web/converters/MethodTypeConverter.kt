package com.example.ps.payu.web.converters

import com.example.ps.payu.model.MethodType
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class MethodTypeConverter : Converter<String, MethodType> {

    override fun convert(source: String): MethodType = MethodType.fromCode(source)
}

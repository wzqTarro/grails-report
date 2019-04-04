package com.zcareze.report.interceptor

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class StyleInterceptorSpec extends Specification implements InterceptorUnitTest<StyleInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test style interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"style")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}

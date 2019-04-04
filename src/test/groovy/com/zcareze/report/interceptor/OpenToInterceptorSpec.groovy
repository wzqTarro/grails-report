package com.zcareze.report.interceptor

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class OpenToInterceptorSpec extends Specification implements InterceptorUnitTest<OpenToInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test openTo interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"openTo")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}

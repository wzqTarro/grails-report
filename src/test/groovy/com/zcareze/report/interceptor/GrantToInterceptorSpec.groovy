package com.zcareze.report.interceptor

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class GrantToInterceptorSpec extends Specification implements InterceptorUnitTest<GrantToInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test grantTo interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"grantTo")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}

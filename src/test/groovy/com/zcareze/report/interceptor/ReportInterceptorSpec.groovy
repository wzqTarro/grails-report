package com.zcareze.report.interceptor

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class ReportInterceptorSpec extends Specification implements InterceptorUnitTest<ReportInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test report interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"report")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}

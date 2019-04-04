package com.zcareze.report.interceptor

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class ReportInputsInterceptorSpec extends Specification implements InterceptorUnitTest<ReportInputsInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test reportInputs interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"reportInputs")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}

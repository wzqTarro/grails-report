package com.zcareze.report.interceptor

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class TablesInterceptorSpec extends Specification implements InterceptorUnitTest<TablesInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test tables interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"tables")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}

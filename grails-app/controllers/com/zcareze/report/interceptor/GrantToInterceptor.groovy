package com.zcareze.report.interceptor


class GrantToInterceptor {
    GrantToInterceptor() {
        match(controller:"reportGrantTo", action: "addReportGrantTo")
    }
    boolean before() {

        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}

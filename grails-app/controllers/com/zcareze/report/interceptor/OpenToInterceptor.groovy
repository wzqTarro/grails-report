package com.zcareze.report.interceptor


class OpenToInterceptor {
    OpenToInterceptor() {
        match(controller: "reportOpenTo", action: "saveReportOpenTo")
    }

    boolean before() {
        params."rpt.id" = params."rptId"
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}

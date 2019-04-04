package com.zcareze.report.interceptor

/**
 * 报表拦截器
 */
class ReportInterceptor {
    ReportInterceptor() {
        match(controller: "report", action: "addReportList")
        match(controller: "report", action: "editReportList")
    }

    boolean before() {
        params."editorId" = "1"
        params."editorName" = "王"
        params."editTime" = new Date()
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}

package com.zcareze.report.interceptor

/**
 * 输入参数拦截器
 */
class ReportInputsInterceptor {
    ReportInputsInterceptor() {
        match(controller:"reportInputs", action: "addReportInput")
    }

    // 执行方法之前
    boolean before() {
        def rptId = params."rptId"
        params."rpt.id" = rptId
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}

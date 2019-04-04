package com.zcareze.report.interceptor

/**
 * 数据表拦截器
 */
class TablesInterceptor {
    TablesInterceptor() {
        match(controller: 'reportTables', action: 'addReportTable')
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

package com.zcareze.report.interceptor

import com.zcareze.report.ReportDatasource

/**
 * 数据表拦截器
 */
class TablesInterceptor {
    TablesInterceptor() {
        match(controller: 'reportTables', action: 'addReportTable')
    }

    boolean before() {
        params."rpt.id" = params."rptId"
        params."dataSource" = ReportDatasource.findByCode(params."dataSource")
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}

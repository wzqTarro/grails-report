package com.report.param

import com.report.dto.ReportParamValue
import grails.validation.Validateable

/**
 * 大屏报表数据请求参数
 */
class ScreenReportParam implements Serializable, Validateable{
    // 报表ID
    String reportId;
    // 数据表名
    List<String> tableNames;
    // 参数值
    List<ReportParamValue> paramValues;
}

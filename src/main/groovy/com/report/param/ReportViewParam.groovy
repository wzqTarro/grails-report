package com.report.param

import com.report.dto.ReportParamValue
import grails.validation.Validateable

/**
 * 获取报表数据请求参数
 */
class ReportViewParam implements Serializable, Validateable{
    String reportId
    List<ReportParamValue> paramValues
}

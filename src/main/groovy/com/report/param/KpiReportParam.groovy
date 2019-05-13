package com.report.param

import grails.validation.Validateable

/**
 * 指标关联报表查看请求参数
 */
class KpiReportParam implements Serializable, Validateable{
    /**
     * 指标Id
     */
    String kpiId;
    /**
     * 报表Id
     */
    String reportId;
    /**
     * 组织Id
     */
    String orgId;
    /**
     * 周期值
     */
    String cycValue;
}

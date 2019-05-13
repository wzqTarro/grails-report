package com.report.vo

import com.zcareze.report.Report
import com.zcareze.report.ReportGroups

/**
 * 有管理权限的报表(包括已经授予的权限)
 */
class ReportManageVO implements Serializable{
    // 报表ID
    String reportId
    // 报表名称
    String reportName
    // 分组名称
    String groupName
    // 组织机构ID
    String orgId
    // 组织机构名称
    String orgName
    // 已授权列表
    List<ReportGrantToVO> grantList

    void accessReport(Report report, String orgId, String orgName) {
        this.reportId = report.id
        this.reportName = report.name

        // 报表分组
        ReportGroups reportGroups = ReportGroups.findByCode(report.grpCode)
        if (reportGroups) {
            this.groupName = reportGroups.name
        }

        this.orgId = orgId
        this.orgName = orgName
    }
}

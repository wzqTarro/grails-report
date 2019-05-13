package com.zcareze.report

import com.report.dto.OrgDTO
import com.report.util.PolicyUtil

/**
 * 报表授权记录
 */
class ReportGrantTo implements Serializable{
    /** 组织ID **/
    String orgId
    /** 授予角色 **/
    String roles
    /** 管理权 **/
    Integer manage
    /** 授权者 **/
    String granter
    /** 授权时间 **/
    Date grantTime
    static belongsTo = [rpt: Report]
    static constraints = {
        orgId(unique: 'rpt')
        granter(nullable: true)
        grantTime(nullable: true)
        manage(inList: [0, 1])
    }

    static mapping = {
        table "report_grant_to"

        orgId column: "org_id", sqlType: "char", length: 32, unique: 'rpt'
        roles column: "roles", length: 30
        granter column: "granter", length: 30
        manage column: "manage", sqlType: "int", length: 1
    }

    def beforeValidate() {
        grantTime = new Date()
    }

    /**
     * 判断是否有报表的权限
     * @param reportId 报表ID
     * @param staffOrgList 职员所属的组织列表
     * @return
     */
    static boolean checkReadPolicy(List<ReportGrantTo> reportGrantToList, List<Map<String, Object>> staffOrgList) {
        if (reportGrantToList) {
            for(reportGrantTo in reportGrantToList) {
                String orgId = reportGrantTo.orgId
                // 授权角色
                String roles = reportGrantTo.roles

                // 角色是否有权限查看该报表
                for (org in staffOrgList) {
                    // 职员所属组织ID
                    String staffOrgId = org.get("orgId")
                    // 职员所属工作种类
                    String classes = org.get("classes")

                    if (orgId == staffOrgId) {
                        // 匹配工作种类
                        if (PolicyUtil.mateRoles(roles, classes)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
}

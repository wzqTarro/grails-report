package com.zcareze.report

import com.report.dto.OrgDTO
import com.report.dto.OrgTreeDTO
import com.report.dto.OrgTreeListDTO
import com.report.util.PolicyUtil

/**
 * 报表开放记录
 */
class ReportOpenTo implements Serializable{
    String id
    /** 组织树ID **/
    String orgTreeId
    /** 组织树层 **/
    Integer orgTreeLayer
    /** 角色 **/
    String roles
    /** 授权者 **/
    String granter
    /** 授权时间 **/
    Date grantTime
    static belongsTo = [rpt: Report]
    static constraints = {
        granter(nullable: true)
        orgTreeId(nullable: true, unique: ['rpt', 'orgTreeLayer'])
        orgTreeLayer(min: 0)
    }
    static mapping = {
        table "report_open_to"

        id generator: 'uuid', column: 'id', sqlType: 'char', length: 32
        orgTreeId column: "org_tree_id", sqlType: "char", length: 32
        roles column: "roles", length: 30
        granter column: "granter", length: 30
        dateCreated column: "grant_time"
    }

    def beforeValidate() {
        grantTime = new Date()
    }

    /**
     * 判断是否有报表的权限
     * @param reportOpenToList 开放记录
     * @param orgTreeDTOList 开放的职员所属的组织机构列表
     * @return
     */
    static boolean checkReadPolicy(List<ReportOpenTo> reportOpenToList, Map<String, List<Map<String, Object>>> orgTreeDTOMap) {
        if (reportOpenToList && orgTreeDTOMap) {

            // 遍历开放记录
            for( reportOpenTo in reportOpenToList ) {
                // 根组织ID
                String orgTreeId = reportOpenTo.orgTreeId
                // 组织树层级
                Integer orgTreeLayer = reportOpenTo.orgTreeLayer
                // 已授权工作种类
                String roles = reportOpenTo.roles

                // 开放的组织机构列表-已授权的组织列表
                String key = orgTreeId + "-" + orgTreeLayer
                List<Map<String, Object>> staffOrgList = null
                if (orgTreeDTOMap.containsKey(key)) {
                    staffOrgList = orgTreeDTOMap.get(key)
                }

                if (staffOrgList) {
                    for (orgTree in staffOrgList) {
                        // 职员所属工作种类
                        String classes = orgTree.get("classes")

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

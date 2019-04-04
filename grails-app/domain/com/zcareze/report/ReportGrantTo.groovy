package com.zcareze.report

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
        granter(blank: true)
        manage(inList: [0, 1])
    }

    static mapping = {
        table "report_grant_to"

        orgId column: "org_id", sqlType: "char", length: 32, unique: 'rpt'
        roles column: "roles", length: 30
        granter column: "granter", length: 30
        dateCreated column: "grant_time"
        manage column: "manage", sqlType: "int", length: 1
    }

    def beforeValidate() {
        grantTime = new Date()
    }
}

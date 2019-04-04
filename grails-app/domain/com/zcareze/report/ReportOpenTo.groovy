package com.zcareze.report

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
        orgTreeLayer(nullable: true, min: 0)
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
}

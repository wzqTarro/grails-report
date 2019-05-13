package com.zcareze.report

import grails.gorm.MultiTenant

/**
 * 个人常用报表
 */
class ReportUsually implements Serializable, MultiTenant<ReportUsually> {
    /** 职员ID **/
    String staffId
    /** 排列号 **/
    Integer seqNum
    /** 查看时间 **/
    Date readTime
    /** 多租户鉴别字段 **/
    String cloudId

    static belongsTo = [rpt: Report]

    static constraints = {
        staffId(nullable: true, unique: 'rpt')
        seqNum(nullable: true, min: 0)
    }

    static mapping = {
        table "report_usually"

        tenantId name: 'cloudId'
        staffId column: 'staff_id', sqlType: 'char', length: 32, unique: 'rpt'
        seqNum column: 'seq_num', sqlType: 'int', length: 5
        readTime column: "read_time"
        cloudId(nullable: true)
    }

    def beforeValidate() {
        readTime = new Date()
    }
}

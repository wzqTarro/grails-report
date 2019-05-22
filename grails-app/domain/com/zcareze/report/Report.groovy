package com.zcareze.report

import com.sun.media.jfxmedia.logging.Logger
import grails.gorm.MultiTenant
import grails.gorm.transactions.Transactional

/**
 * 报表目录
 */
class Report implements Serializable, MultiTenant<Report>{

    String id
    /** 编码 **/
    String code
    /** 名称 **/
    String name
    /** 分组码 **/
    String grpCode
    /** 说明 **/
    String comment
    /** 执行方式 **/
    Integer runway
    /** 多租户鉴别字段 **/
    String cloudId
    // 固化报表
    Integer isFixed
    // 管理状态
    Integer ctrlStatus

    static hasMany = [ctrlLogList: ReportCtrlLog, inputList: ReportInputs, tableList: ReportTables, styleList: ReportStyle, openToList: ReportOpenTo, grantToList: ReportGrantTo]

    static constraints = {
        code(unique: 'cloudId')
        name(unique: 'cloudId')
        grpCode(nullable: true)
        comment(nullable: true)
        runway(nullable: true, inList: [1, 2])
        cloudId(nullable: true)
        isFixed(nullable: true)
        ctrlStatus(nullable: true, inList: [0, 1, -1, 2, -2]) // 0-编制中草稿，1-提请审核，-1-审核退回，2-审核通过，-2-停止使用
    }

    static mapping = {
        table "report_list"

        tenantId name: 'cloudId'
        id generator: 'uuid', column: 'id', sqlType: 'char', length: 32
        code column: 'code', length: 5, unique: 'cloudId'
        name column: 'name', length: 30, unique: 'cloudId'
        grpCode column: 'grp_code', length: 2
        comment column: 'comment', length: 100
        isFixed column: 'is_fixed', sqlType: 'int', length: 1
        cloudId column: 'cloud_id', sqlType: 'char', length: 32
        ctrlStatus column: 'ctrl_status', sqlType: 'int', length: 1

        // 级联操作
        inputList cascade: 'all'
        tableList cascade: 'all'
        styleList cascade: 'all'
        openToList cascade: 'all'
        grantToList cascade: 'all'
        ctrlLogList cascade: 'all'
    }
}

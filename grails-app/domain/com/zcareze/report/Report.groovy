package com.zcareze.report

import com.sun.media.jfxmedia.logging.Logger
import grails.gorm.transactions.Transactional

/**
 * 报表目录
 */
class Report implements Serializable{

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
    /** 编辑时间 **/
    Date editTime
    /** 编辑人ID **/
    String editorId
    /** 编辑人姓名 **/
    String editorName

    static hasMany = [inputList: ReportInputs, tableList: ReportTables, styleList: ReportStyle, openToList: ReportOpenTo, grantToList: ReportGrantTo]

    static constraints = {
        code(unique: true)
        name(unique: true)
        grpCode(nullable: true)
        comment(nullable: true)
        runway(nullable: true, inList: [1, 2])
        editorId(blank: true)
        editorName(blank: true)
        editTime(nullable: true)
    }

    static mapping = {
        table "report_list"

        id generator: 'uuid', column: 'id', sqlType: 'char', length: 32
        code column: 'code', length: 5, unique: true
        name column: 'name', length: 30, unique: true
        grpCode column: 'grp_code', length: 2
        comment column: 'comment', length: 100
        editorId column: 'editor_id', sqlType: 'char', length: 32
        editorName column: 'editor_name', length: 50
        lastUpdated column: "editTime"

        // 级联操作
        inputList cascade: 'all'
        tableList cascade: 'all'
        styleList cascade: 'all'
        openToList cascade: 'all'
        grantToList cascade: 'all'
    }

    def abc() {
        name = "test"
        editTime = new Date()
    }

    def beforeInsert() {
        editTime = new Date()
    }

    def beforeUpdate() {

        editTime = new Date()
    }
}
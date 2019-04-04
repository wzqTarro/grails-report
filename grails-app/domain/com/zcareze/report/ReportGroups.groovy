package com.zcareze.report

/**
 * 报表分组
 */
class ReportGroups implements Serializable{
    /** 编码 **/
    String code
    /** 名称 **/
    String name
    /** 说明 **/
    String comment
    /** 颜色 **/
    String color

    static constraints = {
        code(unique: true)
        name(unique: true)
        comment(nullable: true)
        color(nullable: true)
    }

    static mapping = {
        table "report_groups"

        code column: 'code', sqlType: 'varchar', length: 2, unique: true
        comment column: 'comment', sqlType: 'varchar', length: 100
        color column: 'color', sqlType: 'char', length: 6

        // 唯一键
        name column: 'name', length: 10, unique: true
    }
}

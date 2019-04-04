package com.zcareze.report

/**
 * 报表数据表
 */
class ReportTables implements Serializable{
    /** 名称 **/
    String name
    /** 查询语句 **/
    String sqlText
    /** 排列号 **/
    Integer seqNum
    static belongsTo = [rpt: Report]
    static constraints = {
        name(unique: 'rpt')
        seqNum(nullable: true, min: 0)
    }
    static mapping = {
        table "report_tables"

        name column: 'name', length: 20, sqlType: 'varchar', unique: 'rpt'
        sqlText column: 'sql_text', sqlType: 'text'
        seqNum column: 'seq_num', sqlType: 'int', length: 3
    }
}

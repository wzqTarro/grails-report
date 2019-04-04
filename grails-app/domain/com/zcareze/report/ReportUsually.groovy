package com.zcareze.report

/**
 * 个人常用报表
 */
class ReportUsually implements Serializable{
    /** 职员ID **/
    String staffId
    /** 排列号 **/
    Integer seqNum
    /** 查看时间 **/
    Date readTime

    static belongsTo = [rpt: Report]

    static constraints = {
        staffId(nullable: true, unique: 'rpt')
        seqNum(nullable: true, min: 0)
    }

    static mapping = {
        table "report_usually"

        staffId column: 'staff_id', sqlType: 'char', length: 32, unique: 'rpt'
        seqNum column: 'seq_num', sqlType: 'int', length: 5
        readTime column: "read_time"
    }

    def beforValidate() {
        readTime = new Date()
    }
}

package com.zcareze.report

/**
 *  报表样式
 */
class ReportStyle implements Serializable{
    /** 场景 **/
    Integer scene
    /** 文件URL **/
    String fileUrl
    /** 图标设置 **/
    String chart
    /** 说明 **/
    String comment
    static belongsTo = [rpt: Report]
    static constraints = {
        scene(unique: 'rpt')
        scene(inList: [0,1,2,3])
        fileUrl(nullable: false)
        chart(nullable: true)
        comment(nullable: true)
    }
    static mapping = {
        table "report_style"

        scene column: 'scene', sqlType: 'int', length: 2, unique: 'rpt'
        fileUrl column: 'file_url', length: 200
        chart column: 'chart', sqlType: 'text'
        comment column: 'comment', length: 100
    }
}

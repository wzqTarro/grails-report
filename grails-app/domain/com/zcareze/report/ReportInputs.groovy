package com.zcareze.report

/**
 * 报表输入
 */
class ReportInputs implements Serializable{
    /** 参数名称 **/
    String name
    /** 参数标签 **/
    String caption
    /** 排列号 **/
    Integer seqNum
    /** 数据类型 **/
    String dataType
    /** 输入类型 **/
    Integer inputType
    /** 选项列表 **/
    String optionList
    /** 查询语句 **/
    String sqlText
    /** 默认值文本 **/
    String defValue
    /** 默认值类型 **/
    String defType
    static belongsTo = [rpt: Report, dataSource: ReportDatasource]
    static constraints = {
        name(unique: 'rpt')
        rpt(nullable: false)
        seqNum(nullable: true)
        dataType(inList: ['11', '12', '21', '22', '23', '31', '32', '33'])
        inputType(nullable: true, inList: [0 ,1, 2, 3])
        optionList(nullable: true)
        sqlText(nullable: true)
        defValue(nullable: true)
        defType(nullable: true)
        dataSource(nullable: true)
    }
    static mapping = {
        table "report_inputs"

        name column: 'name', length: 20, unique: 'rpt'
        caption column: 'caption', length: 20
        dataType column: 'data_type', sqlType: 'char', length: 2
        optionList column: 'option_list', length: 300
        sqlText column: 'sql_text', sqlType: 'text'
        defValue column: 'def_value', length: 30
        defType column: 'def_type', length: 10
        dataSource column: 'data_source', length: 2
    }
}

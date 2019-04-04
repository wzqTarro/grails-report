package com.report.vo

import com.zcareze.report.ReportInputs
import com.zcareze.report.ReportStyle
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

/**
 * 报表输入参数
 */
class ReportInputVO implements Serializable{
    // 报表ID
    String rptId
    // 参数名称
    String name
    // 参数标签
    String caption
    // 顺序号
    Integer seqNum
    // 数据类型 11-文本，12-数字；21-年度(YYYY)，22-月份(YYYYMM)，23-日期(YYYYMMDD)；31-机构，32-科室，33-医生团队，该类输入类型只能是动态查询列表
    String dataType
    // 输入类型 0-依赖外部调用程序直接传入；1-标准控件输入；2-固定列表选择；3-动态查询列表
    Integer inputType
    // 选项列表 固定列表选择时，以分号分隔的选项列表
    String optionList
    // 查询语句
    String sqlText
    // 默认值文本
    String defValue
    // 默认值类型
    String defType
    // 系统内置
    boolean system

    void accessInputsProperties(MappingContext grailsDomainClassMappingContext, ReportInputs reportInputs) {
        PersistentEntity entityClass = grailsDomainClassMappingContext.getPersistentEntity(ReportInputs.class.name)
        List<PersistentProperty> persistentPropertyList = entityClass.persistentProperties
        persistentPropertyList.each { property ->
            if (property.name != "rpt" && property.name != "version") {
                this[property.name] =  reportInputs[property.name]
            }
        }
    }
}

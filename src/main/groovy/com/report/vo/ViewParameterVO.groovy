package com.report.vo

import com.zcareze.report.ReportInputs
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

/**
 * 输入参数显示
 */
class ViewParameterVO implements Serializable{
    String id
    // 参数名
    String name
    // 参数显示名
    String caption
    // 数据类型 11-文本，12-数字；21-年度(YYYY)，22-月份(YYYYMM)，23-日期(YYYYMMDD)；31-机构，32-科室，33-医生团队
    String dataType
    // 输入类型  0-依赖外部调用程序直接传入；1-标准控件输入；2-固定列表选择；3-动态查询列表
    Integer inputType
    // 数据源 固定列表选择时为选项列表,用;隔开 动态查询列表时为数据字符串,其中包含col_value,col_title两列
    String optionList
    // 默认值
    String defValue
    // 默认值类型
    String defType
    // 顺序号
    Integer seqNum

    void accessInputsProperties(MappingContext grailsDomainClassMappingContext, ReportInputs reportInputs) {
        PersistentEntity entityClass = grailsDomainClassMappingContext.getPersistentEntity(ReportInputs.class.name)
        List<PersistentProperty> persistentPropertyList = entityClass.persistentProperties
        persistentPropertyList.each { property ->
            if (property.name != "rpt" && property.name != "sqlText" && property.name != "version") {
                this[property.name] =  reportInputs[property.name]
            }
        }
    }
}

package com.report.vo

import com.zcareze.report.ReportStyle
import com.zcareze.report.ReportTables
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

/**
 * 报表样式
 */
class ReportStyleVO implements Serializable{
    // 报表ID
    String rptId
    // 场景
    Integer scene
    // xslt 存放路径
    String fileUrl
    // 可访问的url地址
    String visitUrl
    // 说明该格式文件应用场景区分识别等描述
    String comment
    // 图表设置信息
    String chart

    /**
     * 数据绑定
     * @param grailsDomainClassMappingContext
     * @param reportStyle
     */
    void accessStyleProperties(MappingContext grailsDomainClassMappingContext, ReportStyle reportStyle) {
        PersistentEntity entityClass = grailsDomainClassMappingContext.getPersistentEntity(ReportStyle.class.name)
        List<PersistentProperty> persistentPropertyList = entityClass.persistentProperties
        persistentPropertyList.each { property ->
            if (property.name != "rpt" && property.name != "version") {
                this[property.name] =  reportStyle[property.name]
            }
        }
    }
}

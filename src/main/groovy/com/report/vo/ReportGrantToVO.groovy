package com.report.vo

import com.zcareze.report.ReportGrantTo
import com.zcareze.report.ReportInputs
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

/**
 * 报表授权
 */
class ReportGrantToVO implements Serializable{
    // 报表ID
    String rptId
    // 机构ID
    String orgId
    // 机构名称
    String orgName
    // 授予角色 01-健康助理;02-基层医生;03-专科医生;04-医学专家;05-专业技师;11-业务管理
    String roles
    // 授权包含下级
    Integer manage

    void accessGrantToProperties(MappingContext grailsDomainClassMappingContext, ReportGrantTo reportGrantTo) {
        PersistentEntity entityClass = grailsDomainClassMappingContext.getPersistentEntity(ReportGrantTo.class.name)
        List<PersistentProperty> persistentPropertyList = entityClass.persistentProperties
        persistentPropertyList.each { property ->
            if (property.name != "rpt" && property.name != "version" && property.name != "grantTime" && property.name != "granter") {
                this[property.name] =  reportGrantTo[property.name]
            }
        }
    }
}

package com.report.vo

import com.zcareze.report.ReportGrantTo
import com.zcareze.report.ReportOpenTo
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

class ReportOpenToVO implements Serializable{
    String id;

    /**
     * 报表ID
     */
    String rptId;
    /**
     * 组织树ID
     */
    String orgTreeId;

    /**
     * 组织树名称
     */
    String orgTreeName;

    /**
     * 组织树层
     */
    Integer orgTreeLayer;
    /**
     * 角色
     */
    String roles;
    /**
     * 授权者
     */
    String granter;
    /**
     * 授权时间
     */
    Date grantTime;

    void accessOpenToProperties(MappingContext grailsDomainClassMappingContext, ReportOpenTo reportOpenTo) {
        PersistentEntity entityClass = grailsDomainClassMappingContext.getPersistentEntity(ReportOpenTo.class.name)
        List<PersistentProperty> persistentPropertyList = entityClass.persistentProperties
        persistentPropertyList.each { property ->
            if (property.name != "rpt" && property.name != "version") {
                this[property.name] =  reportOpenTo[property.name]
            }
        }
    }
}

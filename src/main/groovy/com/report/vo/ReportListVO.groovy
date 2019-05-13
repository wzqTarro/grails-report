package com.report.vo

import com.zcareze.report.Report
import com.zcareze.report.ReportTables
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

class ReportListVO implements Serializable{
    String id
    /**
     * 编码
     */
    String code;
    /**
     * 名称
     */
    String name;
    /**
     * 分组编码
     */
    String grpCode;

    /**
     * 执行方式 1-先报表：首先按参数默认值计算报表显示，必要时用户重置参数查询；2-先参数：首先显示参数输入界面，确定后计算查看报表
     */
    Integer runway;
    /**
     * 修改时间
     */
    Date editTime;
    /**
     * 修改人
     */
    String editorId;
    /**
     * 修改人名称
     */
    String editorName;
    /**
     * 备注
     */
    String comment;

    void accessReportProperties(MappingContext grailsDomainClassMappingContext, Report report) {
        PersistentEntity entity = grailsDomainClassMappingContext.getPersistentEntity(Report.class.name)
        List<PersistentProperty> persistentPropertyList = entity.persistentProperties
        persistentPropertyList.each { property ->
            if (property.name != "version") {
                this[property.name] = report[property.name]
            }
        }
    }
}

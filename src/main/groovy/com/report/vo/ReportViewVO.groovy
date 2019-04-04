package com.report.vo

import com.zcareze.report.Report
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

/**
 * 报表信息-用于前端界面 包括报表名称,样式信息(xslt),需要输入的参数列表(参数名,类型)
 */
class ReportViewVO implements Serializable{
    String id
    // 编码
    String code
    // 报表名
    String name
    // 分组码
    String grpCode
    // xslt文件存储地址
    String fileUrl
    // 图标设置
    String chart
    // 最后修改时间（时间格式）
    Date editTime
    // 最后修改时间（时间戳）
    Long updateTime
    // 执行方式 1-先报表：首先按参数默认值计算报表显示，必要时用户重置参数查询； 2-先参数：首先显示参数输入界面，确定后计算查看报表。
    Integer runway
    // 输入参数
    List<ViewParameterVO> inputParams

    void access(String id, String code, String name, String grpCode, String fileUrl, String chart, Date editTime, Long updateTime, Integer runway) {
        this.id = id
        this.code = code
        this.name = name
        this.grpCode = grpCode
        this.fileUrl = fileUrl
        this.chart = chart
        this.editTime = editTime
        this.updateTime = updateTime
        this.runway = runway
    }

    /**
     * 赋值报表基础信息
     * @param grailsDomainClassMappingContext
     * @param report
     */
    void accessReportProperties(MappingContext grailsDomainClassMappingContext, Report report) {
        PersistentEntity entityClass = grailsDomainClassMappingContext.getPersistentEntity(Report.class.name)
        List<PersistentProperty> persistentPropertyList = entityClass.persistentProperties
        persistentPropertyList.each { property ->
            if (property.name != "inputList" && property.name != "tableList" && property.name != "styleList"
                    && property.name != "openToList" && property.name != "grantToList"
                    && property.name != "editorName" && property.name != "editorId"
                    && property.name != "comment" && property.name != "editorId"
                    && property.name != "version") {
                this[property.name] =  report[property.name]
            }
        }
    }
}

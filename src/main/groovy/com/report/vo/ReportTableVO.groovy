package com.report.vo

import com.zcareze.report.Report
import com.zcareze.report.ReportTables
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

/**
 * 报表数据表
 */
class ReportTableVO implements Serializable{
    // 报表Id
    String rptId
    // 数据表名称
    String name
    // 查询语句
    String sqlText
    // 顺序号
    Integer seqNum
    // 数据源
    String dataSource
}

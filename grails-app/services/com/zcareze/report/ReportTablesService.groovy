package com.zcareze.report

import grails.gorm.services.Service

@Service(ReportTables)
interface ReportTablesService {

    ReportTables get(Serializable id)

    List<ReportTables> list(Map args)

    Long count()

    void delete(Serializable id)

    ReportTables save(ReportTables reportTables)

}
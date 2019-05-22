package com.zcareze.report

import grails.gorm.services.Service

@Service(ReportDatasource)
interface ReportDatasourceService {

    ReportDatasource get(Serializable id)

    List<ReportDatasource> list(Map args)

    Long count()

    void delete(Serializable id)

    ReportDatasource save(ReportDatasource reportDatasource)

}
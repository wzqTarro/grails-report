package com.zcareze.report

import grails.gorm.services.Service

@Service(ReportUsually)
interface ReportUsuallyService {

    ReportUsually get(Serializable id)

    List<ReportUsually> list(Map args)

    Long count()

    void delete(Serializable id)

    ReportUsually save(ReportUsually reportUsually)

}
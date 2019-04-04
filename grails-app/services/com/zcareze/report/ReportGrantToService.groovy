package com.zcareze.report

import grails.gorm.services.Service

@Service(ReportGrantTo)
interface ReportGrantToService {

    ReportGrantTo get(Serializable id)

    List<ReportGrantTo> list(Map args)

    Long count()

    void delete(Serializable id)

    ReportGrantTo save(ReportGrantTo reportGrantTo)

}
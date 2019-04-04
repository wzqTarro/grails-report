package com.zcareze.report

import grails.gorm.services.Service

@Service(ReportOpenTo)
interface ReportOpenToService {

    ReportOpenTo get(Serializable id)

    List<ReportOpenTo> list(Map args)

    Long count()

    void delete(Serializable id)

    ReportOpenTo save(ReportOpenTo reportOpenTo)

}
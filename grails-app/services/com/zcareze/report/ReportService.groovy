package com.zcareze.report

import grails.gorm.services.Service

@Service(Report)
interface ReportService {

    Report get(Serializable id)

    List<Report> list(Map args)

    Long count()

    void delete(Serializable id)

    Report save(Report report)

}
package com.zcareze.report

import grails.gorm.services.Service

@Service(ReportStyle)
interface ReportStyleService {

    ReportStyle get(Serializable id)

    List<ReportStyle> list(Map args)

    Long count()

    void delete(Serializable id)

    ReportStyle save(ReportStyle reportStyle)

}
package com.zcareze.report

import grails.gorm.services.Service

@Service(ReportGroups)
interface ReportGroupsService {

    ReportGroups get(Serializable id)

    List<ReportGroups> list(Map args)

    Long count()

    void delete(Serializable id)

    ReportGroups save(ReportGroups reportGroups)

}
package com.zcareze.report

import grails.gorm.services.Service

@Service(ReportCtrlLog)
interface ReportCtrlLogService {

    ReportCtrlLog get(Serializable id)

    List<ReportCtrlLog> list(Map args)

    Long count()

    void delete(Serializable id)

    ReportCtrlLog save(ReportCtrlLog reportCtrlLog)

}
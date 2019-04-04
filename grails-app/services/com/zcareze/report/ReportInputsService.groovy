package com.zcareze.report

import grails.gorm.services.Service

@Service(ReportInputs)
interface ReportInputsService {

    ReportInputs get(Serializable id)

    List<ReportInputs> list(Map args)

    Long count()

    void delete(Serializable id)

    ReportInputs save(ReportInputs reportInputs)

}
package com.zcareze.report

import com.report.result.BaseResult
import com.report.result.Result
import com.report.vo.ReportGrantToVO
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class ReportGrantToController {

    ReportGrantToService reportGrantToService

    def grailsDomainClassMappingContext

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond reportGrantToService.list(params), model:[reportGrantToCount: reportGrantToService.count()]
    }

    def show(String reportId, String orgId) {
        def grantTo = ReportGrantTo.where {
            report {
                eq("id", reportId)
            }
            and{
                eq("orgId", orgId)
            }
        }
        respond grantTo.find()
    }

    def create() {
        respond new ReportGrantTo(params)
    }

    def save(ReportGrantTo reportGrantTo) {
        if (reportGrantTo == null) {
            notFound()
            return
        }

        try {
            reportGrantToService.save(reportGrantTo)
        } catch (ValidationException e) {
            respond reportGrantTo.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'reportGrantTo.label', default: 'ReportGrantTo'), reportGrantTo.id])
                redirect reportGrantTo
            }
            '*' { respond reportGrantTo, [status: CREATED] }
        }
    }

    def edit(String reportId, String orgId) {
        def grantTo = ReportGrantTo.where {
            report {
                eq("id", reportId)
            }
            and{
                eq("orgId", orgId)
            }
        }
        respond grantTo.find()
    }

    def update(ReportGrantTo reportGrantTo) {
        if (reportGrantTo == null) {
            notFound()
            return
        }

        try {
            reportGrantToService.save(reportGrantTo)
        } catch (ValidationException e) {
            respond reportGrantTo.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'reportGrantTo.label', default: 'ReportGrantTo'), reportGrantTo.id])
                redirect reportGrantTo
            }
            '*'{ respond reportGrantTo, [status: OK] }
        }
    }

    def delete(String reportId, String orgId) {
        if (reportId == null || reportId.equals("") || orgId == null || orgId.equals("")) {
            notFound()
            return
        }

        def grantTo = ReportGrantTo.where {
            report {
                eq("id", reportId)
            }
            and{
                eq("orgId", orgId)
            }
        }.find()
        grantTo.delete(flush:true)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'reportGrantTo.label', default: 'ReportGrantTo'), grantTo.orgId])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    /**
     * 新增报表授权
     * @param reportGrantTo
     */
    @Transactional
    def addReportGrantTo() {
        String rptId = params."rptId"
        if (!rptId) {
            render Result.error("报表标识不能为空") as JSON
            return
        }

        // 参数赋值
        ReportGrantTo grantTo = new ReportGrantTo()
        grantTo.properties = params
        if (!grantTo) {
            render Result.error("授权信息不能为空") as JSON
            return
        }

        // 报表标识列表
        String[] ids = rptId.split(";")
        if (ids) {
            for (int i = 0; i < ids.length; i++) {
                String id = ids[i]
                if (!id) {
                    continue
                }

                // 报表信息
                Report report = Report.get(id)
                if (!report) {
                    continue
                }
                grantTo.rpt = report

                // 授权人信息
                grantTo.granter = "wang"
                grantTo.grantTime = new Date()

                // 管理权默认为0
                if (!grantTo.manage) {
                    grantTo.manage = 0
                }

                if (grantTo.save(flush:true)) {
                    render Result.success() as JSON
                    return
                } else {
                    String errorMessage = hasError(grantTo)
                    render Result.error(errorMessage) as JSON
                    return
                }
            }
        }
        render Result.success() as JSON
    }

    /**
     * 删除报表授权
     * @param reportId
     * @param orgId
     */
    def deleteReportGrantTo(String reportId, String orgId) {
        if (reportId && orgId) {
            // 指定报表授权记录
            ReportGrantTo grantTo = ReportGrantTo.createCriteria().get {
                and {
                    rpt {
                        eq("id", reportId)
                    }
                    eq("orgId", orgId)
                }
            }
            if (grantTo) {
                grantTo.delete(flush: true)
                render Result.success() as JSON
                return
            }
            render Result.error("授权记录不存在") as JSON
            return
        }
        render Result.error("报表和组织机构标识不能为空") as JSON
    }

    /**
     * 获取报表已有授权列表
     * @param reportId
     */
    @Transactional(readOnly=true)
    def getReportGrantToByRptId(String reportId) {
        BaseResult<ReportGrantToVO> result = new BaseResult<>()
        if (reportId) {
            List<ReportGrantToVO> reportGrantToVOList = new LinkedList<>()
            ReportGrantTo.createCriteria().list {
                rpt {
                    eq("id", reportId)
                }
            }.each { grantTo ->
                ReportGrantToVO reportGrantToVO = new ReportGrantToVO()
                reportGrantToVO.accessGrantToProperties(grailsDomainClassMappingContext, grantTo)
                reportGrantToVO.rptId = grantTo.rpt.id
                reportGrantToVOList.add(reportGrantToVO)
            }
            result.list = reportGrantToVOList
        } else {
            result.error = "参数不能为空"
        }
        render result as JSON
    }
    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'reportGrantTo.label', default: 'ReportGrantTo'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

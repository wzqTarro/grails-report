package com.zcareze.report

import com.report.Staff
import com.report.result.BaseResult
import com.report.result.Result
import com.report.vo.ReportOpenToVO
import grails.converters.JSON
import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class ReportOpenToController {

    ReportOpenToService reportOpenToService

    def grailsDomainClassMappingContext

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond reportOpenToService.list(params), model:[reportOpenToCount: reportOpenToService.count()]
    }

    def show(Long id) {
        respond reportOpenToService.get(id)
    }

    def create() {
        respond new ReportOpenTo(params)
    }

    def save(ReportOpenTo reportOpenTo) {
        if (reportOpenTo == null) {
            notFound()
            return
        }

        try {
            reportOpenToService.save(reportOpenTo)
        } catch (ValidationException e) {
            respond reportOpenTo.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'reportOpenTo.label', default: 'ReportOpenTo'), reportOpenTo.id])
                redirect reportOpenTo
            }
            '*' { respond reportOpenTo, [status: CREATED] }
        }
    }

    def edit(Long id) {
        respond reportOpenToService.get(id)
    }

    def update(ReportOpenTo reportOpenTo) {
        if (reportOpenTo == null) {
            notFound()
            return
        }

        try {
            reportOpenToService.save(reportOpenTo)
        } catch (ValidationException e) {
            respond reportOpenTo.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'reportOpenTo.label', default: 'ReportOpenTo'), reportOpenTo.id])
                redirect reportOpenTo
            }
            '*'{ respond reportOpenTo, [status: OK] }
        }
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        reportOpenToService.delete(id)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'reportOpenTo.label', default: 'ReportOpenTo'), id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    /**
     * 保存开放记录
     * @param reportOpenTo
     */
    def saveReportOpenTo(ReportOpenTo reportOpenTo) {
        if (reportOpenTo) {
            // 参数校验
            if (reportOpenTo.hasErrors()) {
                String errorMessage = hasError(reportOpenTo)
                render Result.error(errorMessage) as JSON
                return
            }

            // 组织树
            if (!reportOpenTo.orgTreeId) {
                reportOpenTo.orgTreeId = null
            }

            // 外键校验
            if (!reportOpenTo.rpt) {
                render Result.error("报表不存在") as JSON
                return
            }
            if (!reportOpenTo.rpt.id) {
                render Result.error("报表不存在") as JSON
                return
            }

            Staff staff = new Staff()
            reportOpenTo.granter = staff.staffName

            reportOpenTo.save(flush: true)
            render Result.success() as JSON
            return
        }
        render Result.error("数据不能为空") as JSON
    }

    /**
     * 删除开放记录
     * @param id
     * @return
     */
    def deleteReportOpenTo(String id) {
        if (!id) {
            render Result.error("参数不能为空") as JSON
            return
        }
        ReportOpenTo openTo = ReportOpenTo.get(id)
        if (!openTo) {
            render Result.error("开放记录不存在") as JSON
            return
        }
        openTo.delete(flush: true)
        render Result.success() as JSON
    }

    /**
     * 获取指定报表的开放记录
     * @param rptId
     */
    def getReportOpenToList(String rptId) {
        BaseResult<ReportOpenToVO> result = new BaseResult<>()
        List<ReportOpenToVO> reportOpenToVOList = new ArrayList<>()
        ReportOpenTo.createCriteria().list {
            rpt {
                eq("id", rptId)
            }
        }.each { openTo ->
            ReportOpenToVO reportOpenToVO = new ReportOpenToVO()
            reportOpenToVO.accessOpenToProperties(grailsDomainClassMappingContext, openTo)

            reportOpenToVO.rptId = openTo.rpt.id
            reportOpenToVO.id = openTo.id

            /**
             * TODO 组织机构树形菜单
             */
            reportOpenToVO.orgTreeName = ""
            reportOpenToVOList.add(reportOpenToVO)
        }
        result.list = reportOpenToVOList
        render result as JSON
    }
    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'reportOpenTo.label', default: 'ReportOpenTo'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

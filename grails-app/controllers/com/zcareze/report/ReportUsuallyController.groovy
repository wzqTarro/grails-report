package com.zcareze.report

import com.report.Staff
import com.report.result.BaseResult
import com.report.result.Result
import com.report.vo.ReportUsuallyVO
import grails.converters.JSON
import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class ReportUsuallyController {

    ReportUsuallyService reportUsuallyService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond reportUsuallyService.list(params), model:[reportUsuallyCount: reportUsuallyService.count()]
    }

    def show(String staffId, String reportId) {
        def reportUsually = ReportUsually.where {
            report {
                eq("id", reportId)
            }
            and{
                eq("staffId", staffId)
            }
        }
        respond reportUsually.find()
    }

    def create() {
        respond new ReportUsually(params)
    }

    def save(ReportUsually reportUsually) {
        if (reportUsually == null) {
            notFound()
            return
        }

        try {
            reportUsuallyService.save(reportUsually)
        } catch (ValidationException e) {
            respond reportUsually.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'reportUsually.label', default: 'ReportUsually'), reportUsually.id])
                redirect reportUsually
            }
            '*' { respond reportUsually, [status: CREATED] }
        }
    }

    def edit(String staffId, String reportId) {
        def reportUsually = ReportUsually.where {
            report {
                eq("id", reportId)
            }
            and{
                eq("staffId", staffId)
            }
        }
        respond reportUsually.find()
    }

    def update(ReportUsually reportUsually) {
        if (reportUsually == null) {
            notFound()
            return
        }

        try {
            reportUsuallyService.save(reportUsually)
        } catch (ValidationException e) {
            respond reportUsually.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'reportUsually.label', default: 'ReportUsually'), reportUsually.id])
                redirect reportUsually
            }
            '*'{ respond reportUsually, [status: OK] }
        }
    }

    def delete(String staffId, String reportId) {
        if (staffId == null || staffId.equals("") || reportId == null || reportId.equals("")) {
            notFound()
            return
        }

        def reportUsually = ReportUsually.where {
            report {
                eq("id", reportId)
            }
            and{
                eq("staffId", staffId)
            }
        }.find()
        reportUsually.delete(flush:true)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'reportUsually.label', default: 'ReportUsually'), reportUsually.reportId])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    /**
     * 添加我的常用报表
     * @param reportId
     */
    def addMyUsuallyReport(String reportId) {
        if (!reportId) {
            render Result.error("参数不能为空") as JSON
            return
        }

        Report report = Report.get(reportId)
        if (!report) {
            render Result.error("报表不存在") as JSON
            return
        }

        ReportUsually reportUsually = new ReportUsually()
        reportUsually.readTime = new Date()
        /**
         * TODO 职员信息
         */
        reportUsually.staffId = "1"
        reportUsually.rpt = report

        // 当前最小排列号
        def seqNum = ReportUsually.createCriteria().get {
            /**
             * TODO 职员信息
             */
            eq("staffId", "1")
            projections {
                min "seqNum"
            }
        }
        println seqNum
        reportUsually.seqNum  = seqNum > -1 ? seqNum + 1 : 0
        if (reportUsually.save(flush: true)) {
            render Result.success() as JSON
            return
        } else {
            String errorMessage = hasError(reportUsually)
            render Result.error(errorMessage) as JSON
        }

    }

    /**
     * 取消我的常用报表
     * @param reportId
     */
    def cancelMyUsuallyReport(String reportId) {
        if (!reportId) {
            render Result.error("参数不能为空") as JSON
            return
        }

        /**
         * TODO 职员信息
         */
        String staffId = "1"
        ReportUsually reportUsually = ReportUsually.createCriteria().get {
            and {
                rpt {
                    eq("id", reportId)
                }
                eq("staffId", staffId)
            }
        }
        if (!reportUsually) {
            render Result.error("常用报表不存在") as JSON
        }
        reportUsually.delete(flush: true)
        render Result.success() as JSON
    }

    /**
     * 阅览我的常用报表
     * @param reportId
     * @return
     */
    def readMyUsuallyReport(String reportId) {
        if (!reportId) {
            render Result.error("参数不能为空") as JSON
            return
        }

        /**
         * TODO 职员信息
         */
        String staffId = "1"
        ReportUsually reportUsually = ReportUsually.createCriteria().get {
            and {
                rpt {
                    eq("id", reportId)
                }
                eq("staffId", staffId)
            }
        }
        if (!reportUsually) {
            render Result.error("常用报表不存在") as JSON
            return
        }
        reportUsually.readTime = new Date()
        reportUsually.save(flush: true)
        render Result.success() as JSON
    }
    /**
     * 获取我的常用报表
     * @param scene
     */
    def getMyUsuallyReports(Integer scene) {
        BaseResult<ReportUsuallyVO> result = new BaseResult<>()
        /**
         * TODO 职员信息
         */
        Staff staff = new Staff()
        String staffId = staff.staffId
        String orgId = staff.orgId
        String
        boolean isCloudManage = staff.isCloudManage


        // 关联表查询 还可以写HQL或者先查询主表再查询相应从表
        ReportUsually.createCriteria().list {
            // 此写法仅支持sql库
            sqlRestriction("rptId = (select rptId from report_style where scene = "+ scene +" )")
        }.each { ReportUsually usually ->
            // 如果是云管，查看所有我的常用报表，否则筛选
            if (!isCloudManage) {
                /**
                 * TODO 筛选报表
                 */

            }
        }
        render result as JSON
    }
    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'reportUsually.label', default: 'ReportUsually'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

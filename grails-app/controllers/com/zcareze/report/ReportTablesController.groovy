package com.zcareze.report

import com.report.result.BaseResult
import com.report.result.Result
import com.report.vo.ReportTableVO
import grails.converters.JSON
import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class ReportTablesController {

    ReportTablesService reportTablesService

    def grailsDomainClassMappingContext

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond reportTablesService.list(params), model:[reportTablesCount: reportTablesService.count()]
    }

    def show(String reportId, String name) {
        def reportTables = ReportTables.where {
            report {
                eq("id", reportId)
            }
            and {
                eq("name", name)
            }
        }
        respond reportTables.find()
    }

    def create() {
        respond new ReportTables(params)
    }

    def save(ReportTables reportTables) {
        if (reportTables == null) {
            notFound()
            return
        }

        try {
            reportTablesService.save(reportTables)
        } catch (ValidationException e) {
            respond reportTables.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'reportTables.label', default: 'ReportTables'), reportTables.id])
                redirect reportTables
            }
            '*' { respond reportTables, [status: CREATED] }
        }
    }

    def edit(String reportId, String name) {
        def reportTables = ReportTables.where {
            report {
                eq("id", reportId)
            }
            and {
                eq("name", name)
            }
        }
        respond reportTables.find()
    }

    def update(ReportTables reportTables) {
        if (reportTables == null) {
            notFound()
            return
        }

        try {
            reportTablesService.save(reportTables)
        } catch (ValidationException e) {
            respond reportTables.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'reportTables.label', default: 'ReportTables'), reportTables.id])
                redirect reportTables
            }
            '*'{ respond reportTables, [status: OK] }
        }
    }

    def delete(String reportId, String name) {
        if (reportId == null || reportId.equals("") || name == null || name.equals("")) {
            notFound()
            return
        }

        def reportTables = ReportTables.where {
            report {
                eq("id", reportId)
            }
            and {
                eq("name", name)
            }
        }.find()
        reportTables.delete(flush: true)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'reportTables.label', default: 'ReportTables'), reportTables.name])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    /**
     * 添加报表数据表
     * @param reportTables
     */
    def addReportTable(ReportTables reportTables) {
        if (reportTables) {
            // 参数验证
            if (reportTables.hasErrors()) {
                String errorMessage = hasError(reportTables)
                render Result.error(errorMessage) as JSON
                return
            }

            // 外键验证
            if (reportTables.rpt) {
                if (reportTables.rpt.id) {

                    /**
                     * TODO 更新报表编辑信息
                      */
                    reportTables.rpt.editorId = "1"
                    reportTables.rpt.editorName = "王"
                    reportTables.rpt.editTime = new Date()
                    reportTables.save(flush: true)
                    render Result.success() as JSON
                    return
                }
            }
            render Result.error("报表不能为空") as JSON
            return
        }
        render Result.error("数据表不能为空") as JSON
    }

    /**
     * 修改
     * @param reportTables
     */
    def editReportTable() {
        if (params."rptId" && params."name") {
            ReportTables reportTables = ReportTables.createCriteria().get {
                and {
                    rpt {
                        eq("id", params."rptId")
                    }
                    eq("name", params."name")
                }
            }
            if (reportTables) {
                reportTables.properties = params

                /**
                 * TODO 更新报表编辑信息
                 */
                reportTables.rpt.editorId = "1"
                reportTables.rpt.editorName = "王"
                reportTables.rpt.editTime = new Date()
                if (reportTables.save(flush: true)) {
                    render Result.success() as JSON
                    return
                } else {
                    def errorMessage = hasError(reportInputs)
                    render Result.error(errorMessage) as JSON
                    return
                }

            }
            render Result.error("数据表不能为空") as JSON
            return
        }
        render Result.error("报表标识和数据表名称不能为空")  as JSON
    }

    /**
     * 删除指定的报表数据表
     * @param reportId
     * @param name
     */
    def deleteReportTable(String reportId, String name) {
        if (reportId && name) {
            ReportTables reportTables = ReportTables.createCriteria().get {
                and {
                    rpt {
                        eq("id", reportId)
                    }
                    eq("name", name)
                }
            }
            if (reportTables) {
                /**
                 * TODO 更新报表编辑信息
                 */
                reportTables.rpt.editorId = "1"
                reportTables.rpt.editorName = "王"
                reportTables.rpt.editTime = new Date()
                reportTables.delete(flush:true)
                render Result.success() as JSON
                return
            }
            render Result.error("数据表不存在") as JSON
            return
        }
        render Result.error("报表标识和数据表名称不能为空") as JSON
    }

    /**
     * 获取指定报表的数据表
     * @param reportId
     */
    def getReportTableByRptId(String reportId) {
        BaseResult<ReportTables> result = new BaseResult<>()
        if (reportId) {
            // 前端展示样式
            List<ReportTableVO> reportTableVOList = new LinkedList<>()

            List<ReportTables> reportTablesList = ReportTables.createCriteria().list {
                rpt {
                    eq("id", reportId)
                }
                order("seqNum", "asc")
            }.each { tables ->
                // 属性赋值
                ReportTableVO vo = new ReportTableVO()
                vo.accessTableProperties(grailsDomainClassMappingContext, tables)
                vo.setRptId(tables.rpt.id)
                reportTableVOList.add(vo)
            }

            result.list = reportTableVOList
            render result as JSON
        }
        result.error = "报表标识不能为空"
        render result as JSON
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'reportTables.label', default: 'ReportTables'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

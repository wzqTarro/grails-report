package com.zcareze.report

import com.report.enst.ResultEnum
import com.report.result.BaseResult
import com.report.result.Result
import grails.converters.JSON
import grails.gorm.PagedResultList
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.grails.web.json.JSONObject

import static org.springframework.http.HttpStatus.*

class ReportGroupsController {

    ReportGroupsService reportGroupsService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    /**
     * 添加报表分组
     * @param reportGroups
     * @return
     */
    def addReportGroup(ReportGroups reportGroups) {
        if (!reportGroups.validate()) {
            def errorMessage = hasError(reportGroups)
            render Result.error(errorMessage) as JSON
            return
        }
        reportGroups.save(flush:true)
        render Result.success() as JSON
    }

    /**
     * 编辑报表分组
     * @return
     */
    def editReportGroup() {
        def code = params.get("code")
        if (!code) {
            render Result.error("编码不能为空") as JSON
            return
        }
        ReportGroups reportGroups = ReportGroups.findByCode(params.get("code"));
        if (reportGroups != null) {
            reportGroups.properties = params
            if (reportGroups.save(flush: true)) {
                render Result.success() as JSON
            } else {
                def errorMessage = hasError(reportGroups)
                render Result.error(errorMessage) as JSON
            }
        } else {
            render Result.error() as JSON
        }

    }

    /**
     * 删除报表分组
     * @param code
     * @return
     */
    def deleteReportGroup(String code) {
        if (code) {
            ReportGroups reportGroups = ReportGroups.findByCode(code)
            if (reportGroups) {
                reportGroups.delete(flush:true)
                render Result.success() as JSON
                return
            }
            render Result.error("报表分组不存在") as JSON
        } else {
            render Result.error("编码不能为空") as JSON
        }
    }
    /**
     * 获取指定编码的报表分组信息
     * @param code
     */
    def getReportGroupByCode(String code) {
        BaseResult result = new BaseResult<ReportGroups>()
        if (code) {
            ReportGroups reportGroups = ReportGroups.findByCode(code)
            if (!reportGroups) {
                result.setError("报表分组不存在")
                render result as JSON
                return
            }
            result.one = reportGroups
            render result as JSON
        } else {
            result.setError("编码不能为空")
            render result as JSON
        }
    }

    /**
     * 获取报表分组列表
     * @param pageNow
     * @param pageSize
     * @return
     */
    def getReportGroupList(Integer pageNow, Integer pageSize) {
        BaseResult<ReportGroups> result = new BaseResult<>()
        if (pageNow != null && pageSize != null) {
            Integer pageStart = pageNow * pageSize
            if (pageStart < 0) {
                pageStart = 0
            }
            def list = ReportGroups.createCriteria().list{
                ne("code", "99")
                firstResult pageStart
                maxResults pageSize
                order("code", "asc")
            }
            result.setList(list);
        }
        render result as JSON
        /*render(message:"", book: {
            id book.id
        })*/
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'reportGroups.label', default: 'ReportGroups'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

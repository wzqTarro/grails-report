package com.zcareze.report

import com.report.result.BaseResult
import com.report.result.Result
import com.report.vo.ReportStyleVO
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class ReportStyleController {

    ReportStyleService reportStyleService

    def grailsDomainClassMappingContext

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond reportStyleService.list(params), model:[reportStyleCount: reportStyleService.count()]
    }

    def show(String reportId, Integer scene) {
         def reportStyle = ReportStyle.where {
             report {
                 eq("id", reportId)
             }
             and{
                 scene == scene
            }
         }
         respond reportStyle.find()
    }

    def create() {
        respond new ReportStyle(params)
    }

    def save(ReportStyle reportStyle) {
        if (reportStyle == null) {
            notFound()
            return
        }

        try {
            reportStyleService.save(reportStyle)
        } catch (ValidationException e) {
            respond reportStyle.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'reportStyle.label', default: 'ReportStyle'), reportStyle.id])
                redirect reportStyle
            }
            '*' { respond reportStyle, [status: CREATED] }
        }
    }

    def edit(String reportId, Integer scene) {
        def reportStyle = ReportStyle.where {
            report {
                eq("id", reportId)
            }
            and{
                scene == scene
            }
        }
        respond reportStyle.find()
    }

    def update(ReportStyle reportStyle) {
        if (reportStyle == null) {
            notFound()
            return
        }

        try {
            reportStyleService.save(reportStyle)
        } catch (ValidationException e) {
            respond reportStyle.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'reportStyle.label', default: 'ReportStyle'), reportStyle.id])
                redirect reportStyle
            }
            '*'{ respond reportStyle, [status: OK] }
        }
    }

    def delete(String reportId, Integer scene) {
        if (reportId == null || reportId.equals("") || scene == null) {
            notFound()
            return
        }

        def reportStyle = ReportStyle.where {
            report {
                eq("id", reportId)
            }
            and{
                scene == scene
            }
        }.find()
        reportStyle.delete(flush: true)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'reportStyle.label', default: 'ReportStyle'), reportStyle.reportId])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    /**
     * 新增报表样式
     * @param reportStyle
     */
    @Transactional
    def addReportStyle() {
        // 报表标识
        String reportId = params."rptId"
        if (!reportId) {
            render Result.error("报表标识不能为空") as JSON
            return
        }

        // xslt文件内容
        String fileContent = params."fileContent"
        if (!fileContent) {
            render Result.error("xslt内容不能为空") as JSON
            return
        }

        // 样式基础信息
        ReportStyle reportStyle = new ReportStyle()
        reportStyle.properties = params
        if (reportStyle) {
            Report report = Report.get(reportId)
            if (!report) {
                render Result.error("报表不存在") as JSON
                return
            }

            reportStyle.rpt = report
            /**
             * TODO 更新报表编辑时间 ps:调用领域类中封装方法修改属性值失败，暂时改为直接赋值
             */
            reportStyle.rpt.editorId = "1"
            reportStyle.rpt.editorName = "王"
            reportStyle.rpt.editTime = new Date()

            if (reportStyle.save(flush:true)) {
                /**
                 * TODO 上传xslt
                 */
                render Result.success() as JSON
                return
            } else {
                String errorMessage = hasError(reportStyle)
                render Result.error(errorMessage) as JSON
                return
            }
        }
        render Result.error("样式不能为空") as JSON
    }

    /**
     * 修改样式
     */
    @Transactional
    def editReportStyle() {
        // 报表标识
        String reportId = params."rptId"
        if (!reportId) {
            render Result.error("报表标识不能为空") as JSON
            return
        }

        // 场景
        Integer scene = params.getInt("scene")
        if (scene < 0) {
            render Result.error("场景标识不能为空") as JSON
            return
        }

        // 样式查询
        ReportStyle reportStyle = ReportStyle.createCriteria().get {
            and {
                rpt{
                    eq("id", reportId)
                }
                eq("scene", scene)
            }
        }

        if (!reportStyle) {
            render Result.error("样式不存在") as JSON
            return
        }

        reportStyle.properties = params

        // xslt文件内容不为空需要更新文件
        String fileContent = params."fileContent"
        if (fileContent) {
            /**
             * TODO 上传xslt文件
             */
            reportStyle.fileUrl = "12"
        }

        /**
         * TODO 更新报表编辑时间 ps:调用领域类中封装方法修改属性值失败，暂时改为直接赋值
         */
        reportStyle.rpt.editorId = "1"
        reportStyle.rpt.editorName = "王"
        reportStyle.rpt.editTime = new Date()

        if (reportStyle.save(flush:true)) {
            render Result.success() as JSON
            return
        } else {
            /**
             * TODO 删除文件
             */
            String errorMessage = hasError(reportStyle)
            render Result.error(errorMessage) as JSON
            return
        }
    }

    /**
     * 删除报表的指定样式
     * @param reportId
     * @param scene
     */
    @Transactional
    def deleteReportStyle(String reportId, Integer scene) {
        if (reportId && scene > -1) {
            ReportStyle reportStyle = ReportStyle.createCriteria().get {
                and {
                    rpt {
                        eq("id", reportId)
                    }
                    eq("scene", scene)
                }
            }
            if (reportStyle) {
                /**
                 * TODO 更新报表编辑时间 ps:调用领域类中封装方法修改属性值失败，暂时改为直接赋值
                 */
                reportStyle.rpt.editorId = "1"
                reportStyle.rpt.editorName = "王"
                reportStyle.rpt.editTime = new Date()

                reportStyle.delete(flush: true)
                render Result.success() as JSON
                return
            }
            render Result.error("样式不存在") as JSON
            return
        }
        render Result.error("报表标识和场景标识不能为空") as JSON
    }

    /**
     * 获取指定报表的样式列表
     * @param reportId
     */
    @Transactional(readOnly = true)
    def getReportStyleByRptId(String reportId) {
        BaseResult<ReportStyleVO> result = new BaseResult<>()
        if (!reportId) {
            result.error = "报表标识不能为空"
            render result as JSON
            return
        }

        // 前端显示列表
        List<ReportStyleVO> reportStyleVOList = new LinkedList<>()
        // 查询样式列表
        ReportStyle.createCriteria().list {
            rpt {
                eq("id", reportId)
            }
            order("scene", "asc")
        }.each { style ->
            ReportStyleVO reportStyleVO = new ReportStyleVO()
            reportStyleVO.accessStyleProperties(grailsDomainClassMappingContext, style)
            reportStyleVO.rptId = style.rpt.id

            /**
             * TODO 文件授权访问路径
             */
            reportStyleVO.visitUrl = ""
            reportStyleVOList.add(reportStyleVO)
        }
        result.list = reportStyleVOList
        render result as JSON
    }
    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'reportStyle.label', default: 'ReportStyle'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

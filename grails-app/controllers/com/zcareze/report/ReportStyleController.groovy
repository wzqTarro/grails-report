package com.zcareze.report


import com.report.result.BaseResult
import com.report.result.Result
import com.report.service.IFileService
import com.report.vo.ReportStyleVO
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONObject

import static org.springframework.http.HttpStatus.*

class ReportStyleController {

    ReportStyleService reportStyleService

    IFileService fileService

    String staffId = "1"
    String staffName = "王"

    def grailsDomainClassMappingContext

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

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
            reportStyle.rpt.editorId = staffId
            reportStyle.rpt.editorName = staffName
            reportStyle.rpt.editTime = new Date()

            /**
             * TODO 上传xslt
             */
            JSONObject jsonObject = fileService.uploadReportFile("cloudId", reportId)
            if (!jsonObject) {
                render Result.error("上传xslt失败") as JSON
                return
            }
            def errCode = jsonObject.get("errcode")
            if (errCode == 0) {
                String fileUrl = jsonObject.get("strValue")

                /**
                 * TODO 文件路径:区域云/xslt/报表ID/uuid.xslt
                 */
                reportStyle.fileUrl = fileUrl
            } else {
                render Result.error("上传xslt失败") as JSON
                return
            }

            if (reportStyle.save(flush:true)) {

                render Result.success() as JSON
                return
            } else {
                /**
                 * TODO 删除文件
                 */
                fileService.deleteReportFile(reportStyle.fileUrl)

                String errorMessage = hasError(reportStyle)
                render Result.error(errorMessage) as JSON
                return
            }
        }
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
            JSONObject jsonObject = fileService.uploadReportFile("cloudId", reportId)
            if (!jsonObject) {
                render Result.error("上传xslt失败") as JSON
                return
            }
            def errCode = jsonObject.get("errcode")
            if (errCode == 0) {
                String fileUrl = jsonObject.get("strValue")

                /**
                 * TODO 文件路径:区域云/xslt/报表ID/uuid.xslt
                 */
                reportStyle.fileUrl = fileUrl
            } else {
                render Result.error("上传xslt失败") as JSON
                return
            }
        }

        /**
         * TODO 更新报表编辑时间 ps:调用领域类中封装方法修改属性值失败，暂时改为直接赋值
         */
        reportStyle.rpt.editorId = staffId
        reportStyle.rpt.editorName = staffName
        reportStyle.rpt.editTime = new Date()

        if (reportStyle.save(flush:true)) {
            render Result.success() as JSON
            return
        } else {
            /**
             * TODO 删除文件
             */
            fileService.deleteReportFile(reportStyle.fileUrl)
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
                reportStyle.rpt.editorId = staffId
                reportStyle.rpt.editorName = staffName
                reportStyle.rpt.editTime = new Date()

                /**
                 * TODO 删除文件
                 */
                JSONObject jsonObject = fileService.deleteReportFile(reportStyle.fileUrl)
                if (!jsonObject) {
                    render Result.error("删除失败") as JSON
                    return
                }
                def errCode = jsonObject.get("errcode")
                if (errCode != 0) {
                    render Result.error("删除失败") as JSON
                    return
                }
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
        List<ReportStyleVO> reportStyleVOList = new ArrayList<>()
        // 查询样式列表
        ReportStyle.createCriteria().list {
            rpt {
                eq("id", reportId)
            }
            order("scene", "asc")
        }.each { style ->
            ReportStyleVO reportStyleVO = new ReportStyleVO()
            bindData(reportStyleVO, style)
            reportStyleVO.rptId = style.rpt.id
            def visitUrl = ""
            /**
             * TODO 文件授权访问路径
             */
            JSONObject jsonObject = fileService.getReportFileVisitUrl(style.fileUrl, staffId)
            if (!jsonObject) {
                render Result.error("获取文件访问路径失败") as JSON
                return
            }
            def errCode = jsonObject.get("errcode")
            if (errCode == 0) {
                def visitUrlDTO = jsonObject.get("visitUrlDTO")
                if (visitUrlDTO) {
                    visitUrl = visitUrlDTO.ossUrl
                }
            }
            reportStyleVO.visitUrl = visitUrl
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

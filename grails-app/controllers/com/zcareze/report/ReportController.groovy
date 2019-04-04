package com.zcareze.report

import com.report.common.CommonValue
import com.report.enst.InputDataTypeEnum
import com.report.enst.ParamDataTypeEnum
import com.report.enst.ReportSystemParamEnum
import com.report.result.BaseResult
import com.report.result.Result
import com.report.util.CommonUtil
import com.report.vo.ReportInputVO
import com.report.vo.ReportViewVO
import com.report.vo.ViewParameterVO
import grails.converters.JSON
import grails.core.GrailsDomainClass
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.grails.core.DefaultGrailsDomainClass
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.boot.actuate.endpoint.AutoConfigurationReportEndpoint

import java.util.regex.Pattern

import static org.springframework.http.HttpStatus.*

class ReportController {

    ReportService reportService

    def grailsDomainClassMappingContext

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond reportService.list(params), model:[reportCount: reportService.count()]
    }

    def show(String id) {
//        Report report = reportService.get(id)
//        List<ReportInputs> reportInputsList = ReportInputs.findAllByReport(report)
//        reportInputsList.each {reportInput ->
//            report.InputList(reportInput)
//        }
        def report = Report.get(id)
        respond report
    }

    def create() {
        respond new Report(params)
    }

    def save(Report report) {
        if (report == null) {
            notFound()
            return
        }

        try {
            reportService.save(report)
        } catch (ValidationException e) {
            respond report.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'report.label', default: 'Report'), report.id])
                redirect report
            }
            '*' { respond report, [status: CREATED] }
        }
    }

    def edit(String id) {
        respond reportService.get(id)
    }

    def update(Report report) {
        if (report == null) {
            notFound()
            return
        }

        try {
            reportService.save(report)
        } catch (ValidationException e) {
            respond report.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'report.label', default: 'Report'), report.id])
                redirect report
            }
            '*'{ respond report, [status: OK] }
        }
    }

    def delete(String id) {
        if (id == null || id.equals("")) {
            notFound()
            return
        }

        reportService.delete(id)

        def reportUsually = ReportUsually.where {
            report {
                eq("id", id)
            }
        }.find()
        reportUsually.delete()

        def grantTo = ReportGrantTo.where {
            report {
                eq("id", id)
            }
        }.find()
        grantTo.delete()

        def input = ReportInputs.where {
            report {
                eq("id", id)
            }
        }.find()
        input.delete()

        def reportStyle = ReportStyle.where {
            report {
                eq("id", id)
            }
        }.find()
        reportStyle.delete()

        def reportTables = ReportTables.where {
            report {
                eq("id", id)
            }
        }.find()
        reportTables.delete()

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'report.label', default: 'Report'), id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    /**
     * 新增报表
     * @param report
     * @return
     */
    def addReportList(Report report) {
        if (report.hasErrors()) {
            def errorMessage = hasError(report)
            render Result.error(errorMessage) as JSON
            return
        }

        report.save(flush: true)
        render Result.success() as JSON
    }

    /**
     * 修改报表
     * @param report
     */
    def editReportList(Report report) {
        // 传递错误主键，report为null,不传主键或者正确主键report都不会为null
        if (report) {
            if (report.hasErrors()) {
                def errorMessage = hasError(report)
                render Result.error(errorMessage) as JSON
                return
            }
            report.save(flush: true)
            render Result.success() as JSON
            return
        }
        render Result.error("报表不存在") as JSON
        return
    }

    /**
     * 删除报表
     * @param id
     */
    @Transactional
    def deleteReportList(String id) {
        if (id) {
            // 删除个人常用报表
            def reportUsually = ReportUsually.where {
                report {
                    eq("id", id)
                }
            }.each {
                it.delete()
            }

            // 删除报表及其级联数据
            Report report = Report.get(id)
            if (report) {
                report.delete(flush: true)
            }
            render Result.success() as JSON
        } else {
            render Result.error("标识不能为空") as JSON
        }
    }

    /**
     * 获取指定报表信息
     * @param id
     */
    def getReportListById(String id) {
        BaseResult<Report> result = new BaseResult<>()
        if (id) {
            Report report = Report.get(id)
            result.one = report
            render result as JSON
        } else {
            result.setError("标识不能为空")
            render result as JSON
        }
    }
    /**
     * 获取报表清单
     * @param name
     * @param groupCode
     * @param pageNow
     * @param pageSize
     */
    def getReportList(String name, String groupCode, Integer pageNow, Integer pageSize) {
        BaseResult<Report> result = new BaseResult<>()
        Integer pageStart = null
        if (pageNow > -1 && pageSize) {
            pageStart = pageNow * pageSize
            if (pageStart < 0) {
                pageStart = 0
            }
        }
        result.list = Report.createCriteria().list {
            if (name || groupCode) {
                if (name == null && groupCode != null) {
                    eq("grpCode", groupCode)
                }
                if (name != null && groupCode == null) {
                    and {
                        ilike("name", "%" + name + "%")
                        ne("grpCode", "99")
                    }
                }
                if (name && groupCode) {
                    and {
                        ilike("name", "%" + name + "%")
                        eq("grpCode", groupCode)
                    }
                }
            } else {
                ne("grpCode", "99")
            }
            if (pageNow > -1 && pageSize) {
                firstResult pageStart
                maxResults pageSize
            }
            order "code", "asc"
        }
        render result as JSON
    }

    /**
     * 获取监控大屏报表清单
     * @param name
     */
    def getScreenReportList(String name) {
        BaseResult<Report> result = new BaseResult<>()
        result.list = Report.createCriteria().list {
            if (name) {
                and {
                    ilike("name", "%" + name + "%")
                    eq("grpCode", "99")
                }
            } else {
                eq("grpCode", "99")
            }
            order("code", "asc")
        }
        render result as JSON
    }

    /**
     * 获取指定报表最后修改时间
     * @param id
     */
    def getReportListUpdateTime(String id) {
        BaseResult<Date> result = new BaseResult<>()
        if (id) {
            Report report = Report.get(id)
            if (report) {
                Date updateTime = report.editTime
                result.one = updateTime
            }
        } else {
            result.setError("标识不能为空")
            render result as JSON
        }
    }

    /**
     * 获取xlst文件的实际访问地址 (用于获取oss访问地址)
     * @param fileUrl
     */
    def getVisitFileUrl(String fileUrl) {
        BaseResult<String> result = new BaseResult<>()
        /**
         * TODO 文件服务，获取oss真实访问路径
         */
        result.one = ""
        render result as JSON
    }

    /**
     * 获取报表信息 主要用于前端获取报表基本信息使用
     * @param id
     * @param scene
     */
    @Transactional(readOnly = true)
    def getReportViewById(String id, Integer scene) {
        BaseResult<ReportViewVO> result = new BaseResult<>()
        if (id && scene > -1) {
            // 报表
            Report report = Report.createCriteria().get{
                and{
                    eq("id", id)
                    styleList {
                        eq ("scene", scene)
                    }
                }
            }
            if (report) {
                Set<ReportTables> tablesSet = report.tableList
                if (!tablesSet) {
                    result.error = "数据为空"
                    render result as JSON
                }



                // 前端显示输入参数样式
                List<ViewParameterVO> viewParameterVOList = new LinkedList<>()

                // 不重复的输入参数
                Map<String, ViewParameterVO> parameterVOMap = new HashMap<>()

                // 只获取数据表中用到的输入参数
                tablesSet.each { ReportTables table ->
                    String sql = table.sqlText
                    List<String> paramList = CommonUtil.analysisSql(sql)

                    // 遍历使用的参数
                    paramList.each { param ->
                        // 参数名称
                        String paramName = param[param.indexOf(CommonValue.PARAM_PREFIX)+1..param.indexOf(CommonValue.PARAM_SUFFIX) - 1]

                        // 系统参数跳过
                        ReportSystemParamEnum reportSystemParamEnum = ReportSystemParamEnum.getEnumByName(paramName)
                        if (reportSystemParamEnum) {
                            return // 闭包中return相当于continue
                        }

                        // 输入参数
                        ReportInputs inputs = ReportInputs.createCriteria().get {
                            and{
                                rpt{
                                    eq("id", report.id)
                                }
                                eq("name", paramName)
                            }
                        }

                        ViewParameterVO viewParameterVO = new ViewParameterVO()
                        viewParameterVO.accessInputsProperties(grailsDomainClassMappingContext, inputs)

                        if (!viewParameterVO.id){
                            viewParameterVO.id = ""
                        }

                        // 只有输入参数类型为年月日时，使用默认值类型
                        InputDataTypeEnum inputDataTypeEnum = InputDataTypeEnum.getEnumByDataType(inputs.dataType)
                        if (inputDataTypeEnum != InputDataTypeEnum.TYPE_MONTH
                                && inputDataTypeEnum != InputDataTypeEnum.TYPE_DAY
                                && inputDataTypeEnum != InputDataTypeEnum.TYPE_YEAR) {
                            viewParameterVO.defType = ""
                        }

                        if (!inputs.defValue) {
                            viewParameterVO.defValue = ""
                        }
                        parameterVOMap.put(inputs.name, viewParameterVO)
                    }
                }
                viewParameterVOList.addAll(parameterVOMap.values())

                // 前端显示
                ReportViewVO reportViewVO = new ReportViewVO()
                // 最后更新时间（时间戳）
                Long timeLong = 0L
                if (report.editTime) {
                    timeLong = report.editTime.getTime()
                }

                // 报表样式
                ReportStyle style = report.styleList[0]
                String fileUrl = style.fileUrl
                if (fileUrl) {
                    /**
                     * TODO 获取xslt真实访问路径
                     */
                }
                reportViewVO.access(report.id, report.code, report.name, report.grpCode, fileUrl, style.chart, report.editTime, timeLong, report.runway)
                reportViewVO.inputParams = viewParameterVOList
                result.one = reportViewVO
                render result as JSON
                return
            } else {
                result.setError("报表不存在")
            }
        } else {
            result.setError("标识不能为空")
        }
        render result as JSON
    }

    def deal() {
        // 数据表
        Set<ReportTables> tablesSet = report.tableList
        if (tableSet) {
            // 前端显示
            ReportViewVO reportViewVO = new ReportViewVO()

            //report赋值给reportViewVO
            reportViewVO.accessReportProperties(grailsDomainClassMappingContext, report)

            // 最后更新时间（时间戳）
            Long timeLong = 0L
            if (report.editTime) {
                timeLong = report.editTime.getTime()
            }
            reportViewVO.updateTime = timeLong

            // 参数解析
            HashMap<String, ViewParameterVO> inputParam = new HashMap<String, ViewParameterVO>();
            tablesSet.each { table ->
                String sql = table.sqlText

                // 获取sql中存在的参数，包含[]
                List<String> paramList = analysisSql(sql)
                paramList.each { param ->
                    // 将sql中参数替换为？，变为可执行参数
                    sql.replace(param, "?")
                    // 去掉[]，获取参数名称
                    String paramName = param[param.indexOf(CommonValue.PARAM_PREFIX)+1..param.indexOf(CommonValue.PARAM_SUFFIX) - 1]

                    ReportInputVO reportInputVO = null
                    // 系统参数
                    ReportSystemParamEnum systemParamEnum = ReportSystemParamEnum.getEnumByName(paramName)
                    if (systemParamEnum) {
                        reportInputVO = new ReportInputVO()
                        reportInputVO.caption = systemParamEnum.title
                        reportInputVO.name = systemParamEnum.name
                        reportInputVO.dataType = (ParamDataTypeEnum.TYPE_STRING.code.toString())
                        reportInputVO.system = true

                    }else { // 不是系统参数
                        // 输入参数
                        ReportInputs reportInputs = ReportInputs.createCriteria().get {
                            and {
                                rpt {
                                    eq("id", report.id)
                                }
                                eq("name", paramName)
                            }
                        }
                        if (!reportInputs) {
                            render result.error = "参数不存在"
                            return
                        }
                        reportInputVO = new ReportInputVO()
                        reportInputVO.system = false

                        // 输入参数赋值给参数视图对象
                        reportInputVO.accessInputsProperties(grailsDomainClassMappingContext, reportInputs)

                    }

                }
            }
        } else {
            result.setError("数据为空")
        }
    }

    public static void main(String[] args) {
        def param = "[kind]"
        println param[param.indexOf(CommonValue.PARAM_PREFIX)+1..param.indexOf(CommonValue.PARAM_SUFFIX) - 1]
    }
    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'report.label', default: 'Report'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

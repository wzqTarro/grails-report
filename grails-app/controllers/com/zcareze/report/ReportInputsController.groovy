package com.zcareze.report

import com.report.bo.QueryInputDesignBO
import com.report.common.CommonValue
import com.report.dto.ReportParamValue
import com.report.enst.QueryInputDefTypeEnum
import com.report.enst.ReportSystemParamEnum
import com.report.result.BaseResult
import com.report.result.QueryInputValueResult
import com.report.result.Result
import com.report.util.CommonUtil
import com.report.vo.ReportInputVO
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class ReportInputsController {

    ReportInputsService reportInputsService

    def grailsDomainClassMappingContext

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond reportInputsService.list(params), model:[reportInputsCount: reportInputsService.count()]
    }

    def show(String reportId, String name) {
        def reportInput = ReportInputs.where {
            report {
                eq("id", reportId)
            }
            and{
                eq("name", name)
            }
        }
        // 只输出结果
        //render reportInput.find() as JSON
        // 结果输出并跳转到页面
        respond reportInput.find()
    }

    def create() {
        respond new ReportInputs(params)
    }

    def save(ReportInputs reportInputs) {
        if (reportInputs == null) {
            notFound()
            return
        }

        try {
            reportInputsService.save(reportInputs)
        } catch (ValidationException e) {
            respond reportInputs.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'reportInputs.label', default: 'ReportInputs'), reportInputs.id])
                redirect reportInputs
            }
            '*' { respond reportInputs, [status: CREATED] }
        }
    }

    /*def edit(String reportId, String name) {
        def reportInput = ReportInputs.where {
            report {
                eq("id", reportId)
            }
            and{
                eq("name", name)
            }
        }
        respond reportInput.find()
    }*/

    def update(ReportInputs reportInputs) {
        println(reportInputs as JSON)
        if (reportInputs == null) {
            notFound()
            return
        }
        try {
            reportInputsService.save(reportInputs)
        } catch (ValidationException e) {
            respond reportInputs.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'reportInputs.label', default: 'ReportInputs'), reportInputs.name])
                redirect reportInputs
            }
            '*'{ respond reportInputs, [status: OK] }
        }
    }

    def delete(String reportId, String name) {
        if (reportId == null || name == null || reportId.equals("") || name.equals("")) {
            notFound()
            return
        }

        def input = ReportInputs.where {
            report {
                eq("id", reportId)
            }
            and{
                eq("name", name)
            }
        }.find()
        input.delete(flush:true)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'reportInputs.label', default: 'ReportInputs'), input.name])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    /**
     * 新增报表参数
     * @param reportInputs
     */
    @Transactional
    def addReportInput(ReportInputs reportInputs) {

        // 参数验证 ps:由于修改了主表主键，导致hasErrors无法验证外键约束情况，需要手动验证外键，
        if (reportInputs.hasErrors()) {
            def errorMessage = hasError(reportInputs)
            render Result.error(errorMessage) as JSON
            return
        }

        // 验证外键
        def report = reportInputs.rpt
        if (report) {
            if (!report.id) {
                render Result.error("报表不存在") as JSON
                return
            }
        } else {
            render Result.error("报表不存在") as JSON
        }

        // 判断是否为系统内置参数
        String name = reportInputs.name
        ReportSystemParamEnum e = ReportSystemParamEnum.getEnumByName(name)
        if (e != null) {
            render Result.error("系统内置参数名称") as JSON
            return
        }

        /**
         * TODO 更新报表编辑时间 ps:调用领域类中封装方法修改属性值失败，暂时改为直接赋值
         */
        reportInputs.rpt.editorId = "1"
        reportInputs.rpt.editorName = "王"
        reportInputs.rpt.editTime = new Date()
        // 主表数据改变后，默认也会更新
        reportInputs.save(flush: true)
        render Result.success() as JSON
    }

    /**
     * 修改报表参数
     * @param reportInputs
     */
    @Transactional
    def edit() {
        if (params."rptId" && params."name") {
            ReportInputs reportInputs = ReportInputs.createCriteria().get {
                and {
                    rpt {
                        eq("id", params."rptId")
                    }
                    eq("name", params."name")
                }
            }
            if (reportInputs) {
                reportInputs.properties = params

                // 判断是否为系统内置参数
                def name = reportInputs.name
                ReportSystemParamEnum e = ReportSystemParamEnum.getEnumByName(name)
                if (e != null) {
                    render Result.error("系统内置参数名称") as JSON
                    return
                }

                /**
                 * TODO 更新报表编辑时间 ps:调用领域类中封装方法修改属性值失败，暂时改为直接赋值
                 */
                reportInputs.rpt.editorId = "1"
                reportInputs.rpt.editorName = "王"
                reportInputs.rpt.editTime = new Date()

                // 主表数据改变后，默认也会更新
                if (reportInputs.save(flush: true)) {
                    render Result.success() as JSON
                    return
                } else {
                    def errorMessage = hasError(reportInputs)
                    render Result.error(errorMessage) as JSON
                    return
                }
            }
            render Result.error("输入参数不存在") as JSON
            return
        }
        render Result.error("报表标识和输入参数名称不能为空") as JSON
    }

    /**
     * 删除
     * @param reportId
     * @param name
     * @return
     */
    def deleteReportInput(String reportId, String name) {
        if (reportId && name) {
            ReportInputs reportInput = ReportInputs.createCriteria().get {
                and{
                    rpt {
                        eq("id", reportId)
                    }
                    eq("name", name)
                }
            }
            if (reportInput) {
                /**
                 * TODO 更新报表编辑时间 ps:调用领域类中封装方法修改属性值失败，暂时改为直接赋值
                 */
                reportInput.rpt.editorId = "1"
                reportInput.rpt.editorName = "王"
                reportInput.rpt.editTime = new Date()
                reportInput.delete(flush: true)
                render Result.success() as JSON
                return
            }
            render Result.error("输入参数不存在") as JSON
        }
        render Result.error("报表标识和名称都不能为空") as JSON
    }

    /**
     * 获取指定报表的参数列表
     * @param reportId
     * @return
     */
    @Transactional(readOnly = true)
    def getPeportInputsByRptId(String reportId) {
        BaseResult<ReportInputVO> result = new BaseResult<>()
        if (reportId) {
            List<ReportInputVO> reportInputVOList = new LinkedList<>()
            ReportInputs.createCriteria().list {
                rpt{
                    eq("id", reportId)
                }
                order("seqNum", "asc")
            }.each { inputs ->
                ReportInputVO reportInputVO = new ReportInputVO()
                reportInputVO.accessInputsProperties(grailsDomainClassMappingContext, inputs)
                reportInputVO.rptId = inputs.rpt.id
                reportInputVO.system = false
                reportInputVOList.add(reportInputVO)
            }
            result.list = reportInputVOList
        } else {
            result.error = "参数不能为空"
        }
        render result as JSON
    }

    /**
     * 获取指定的报表参数
     * @param reportId
     * @param name
     */
    @Transactional(readOnly = true)
    def getReportInput(String reportId, String name) {
        BaseResult<ReportInputVO> result = new BaseResult<>()
        if (reportId && name) {
            ReportInputs inputs = ReportInputs.createCriteria().get {
                and {
                    rpt{
                        eq("id", reportId)
                    }
                    eq("name", name)
                }
                order("seqNum", "asc")
            }
            ReportInputVO reportInputVO = new ReportInputVO()
            reportInputVO.accessInputsProperties(grailsDomainClassMappingContext, inputs)
            reportInputVO.rptId = inputs.rpt.id
            reportInputVO.system = false

            result.one = reportInputVO
        } else {
            result.error = "参数不能为空"
        }
        render result as JSON
    }

    private QueryInputDesignBO getQueryInputParam(ReportInputs reportInputs) {

    }

    /**
     * 获取动态查询参数的参数值信息
     * @param reportId
     * @param name
     */
    def getReportQueryInputValue(String reportId, String name) {
        QueryInputValueResult result = new QueryInputValueResult()
        if (reportId && name) {
            // 输入参数
            ReportInputs reportInputs = ReportInputs.createCriteria().get {
                and {
                    rpt{
                        eq("id", reportId)
                    }
                    eq("name", name)
                }
            }

            String sql = null
            if (reportInputs) {
                sql = reportInputs.sqlText
            }
            if (!sql) {
                result.error = "动态查询参数sql为空"
                render result as JSON
                return
            }

            // 输入参数动态查询参数解析
            List<String> paramList = CommonUtil.analysisSql(sql)
            // 系统参数列表
            List<String> sysParamList = new LinkedList<>(paramList.size())
            paramList.each { param ->
                // 将sql中参数替换为？，变为可执行参数
                sql.replace(param, "?")
                // 去掉[]，获取参数名称
                String paramName = param[param.indexOf(CommonValue.PARAM_PREFIX) + 1..param.indexOf(CommonValue.PARAM_SUFFIX) - 1]

                ReportInputVO reportInputVO = null
                // 系统参数
                ReportSystemParamEnum systemParamEnum = ReportSystemParamEnum.getEnumByName(paramName)

                // 输入参数查询语句中的参数只能是系统参数
                if (!systemParamEnum) {
                    result.error = "动态查询参数sql错误"
                    render result as JSON
                    return
                }
                sysParamList.add(paramName)
            }

            // 默认值类型
            String defValue = reportInputs.defValue
            QueryInputDefTypeEnum defTypeEnum = QueryInputDefTypeEnum.getEnumByDefType(reportInputs.defType)
            if (defTypeEnum) {
                // 页面显示文本及其值
                ReportParamValue reportParamValue = CommonUtil.getInputParamValue(defTypeEnum)
                if (!reportParamValue) {
                    defValue = reportInputs.defValue
                } else {
                    defValue = reportParamValue.value
                }
            }
            if (!defValue) {
                defValue = ""
            }
            result.setDefValue(defValue)


        }
        result.error = "参数不能为空"
        render result as JSON
    }
    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'reportInputs.label', default: 'ReportInputs'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

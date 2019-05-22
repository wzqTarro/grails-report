package com.zcareze.report

import com.report.bo.QueryInputDesignBO
import com.report.common.CommonValue
import com.report.common.ReportXmlConstant
import com.report.dto.ReportParamValue
import com.report.dto.TableParamDTO
import com.report.enst.DatasourceConfigKindEnum
import com.report.enst.QueryInputDefTypeEnum
import com.report.enst.ReportSystemParamEnum
import com.report.enst.ResultEnum
import com.report.param.ReportViewParam
import com.report.result.ApiWrapper
import com.report.result.BaseResult
import com.report.result.QueryInputValueResult
import com.report.result.Result
import com.report.util.CommonUtil
import com.report.vo.DatasourceConfigVO
import com.report.vo.QueryInputValueVO
import com.report.vo.ReportDataVO
import com.report.vo.ReportInputVO
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import org.springframework.beans.factory.annotation.Value

import static org.springframework.http.HttpStatus.*

class ReportInputsController {

    ReportInputsService reportInputsService

    def grailsDomainClassMappingContext

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    String staffId = "1"
    String staffName = "王"

    /**
     * 新增报表参数
     * @param reportInputs
     */
    @Transactional
    def addReportInput(ReportInputs reportInputs) {

        // 参数验证 ps:由于修改了主表主键，导致hasErrors无法验证外键约束情况，需要手动验证外键，
        if (!reportInputs.validate()) {
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

        def dataSource = reportInputs.dataSource
        if (!dataSource) {
            render Result.error("数据源不能为空") as JSON
            return
        }

        // 判断是否为系统内置参数
        String name = reportInputs.name
        ReportSystemParamEnum e = ReportSystemParamEnum.getEnumByName(name)
        if (e != null) {
            render Result.error("系统内置参数名称") as JSON
            return
        }

        // 主表数据改变后，默认也会更新
        reportInputs.save(flush: true)
        render Result.success() as JSON
    }

    /**
     * 修改报表参数
     * @param reportInputs
     */
    @Transactional
    def editReportInput() {
        if (params."rptId" && params."name") {
            // 判断是否为系统内置参数
            def name = params.name
            ReportSystemParamEnum e = ReportSystemParamEnum.getEnumByName(name)
            if (e != null) {
                render Result.error("系统内置参数名称") as JSON
                return
            }

            ReportInputs reportInputs = ReportInputs.createCriteria().get {
                and {
                    rpt {
                        eq("id", params."rptId")
                    }
                    eq("name", params."name")
                }
            }
            if (reportInputs) {
                def dataSource = params."dataSource"
                if (dataSource) {
                    def reportDataSource = ReportDatasource.findByCode(dataSource)
                    if (!reportDataSource) {
                        render Result.error("数据源有误") as JSON
                        return
                    }
                    params.dataSource = null
                    reportInputs.dataSource = reportDataSource
                }
                reportInputs.properties = params

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
                bindData(reportInputVO, inputs)
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
            }
            if (!inputs) {
                render result as JSON
                return
            }
            ReportInputVO reportInputVO = new ReportInputVO()
            bindData(reportInputVO, inputs)
            reportInputVO.rptId = inputs.rpt.id
            reportInputVO.system = false

            result.one = reportInputVO
        } else {
            result.error = "参数不能为空"
        }
        render result as JSON
    }

    /**
     * api获取动态查询参数的参数值信息
     * @param reportId
     * @param name
     */
    def getReportQueryInputValueOld(String reportId, String name) {
        ApiWrapper wrapper = new ApiWrapper()
        Map<String, Object> map = new HashMap<>()
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
                wrapper.errmsg = "动态查询参数sql为空"
                wrapper.errcode = ResultEnum.PARAM_ERROR.code
                render wrapper as JSON
                return
            }

            // 输入参数动态查询参数解析
            List<String> paramList = CommonUtil.analysisSql(sql)
            // 系统参数列表
            List<String> sysParamValues = new ArrayList<>()
            paramList.each { param ->
                // 将sql中参数替换为？，变为可执行参数
                sql = sql.replace(param, "?")
                // 去掉[]，获取参数名称
                String paramName = param[param.indexOf(CommonValue.PARAM_PREFIX) + 1..param.indexOf(CommonValue.PARAM_SUFFIX) - 1]

                // 系统参数
                ReportSystemParamEnum systemParamEnum = ReportSystemParamEnum.getEnumByName(paramName)

                // 输入参数查询语句中的参数只能是系统参数
                if (!systemParamEnum) {
                    wrapper.errmsg = "动态查询参数sql错误"
                    wrapper.errcode = ResultEnum.PARAM_ERROR.code
                    render wrapper as JSON
                    return
                }

                // 获取系统参数的值
                ReportParamValue reportParamValue =  CommonUtil.getSystemParamValue(paramName)
                sysParamValues.add(reportParamValue.value)
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
            map.put("defValue", defValue)

            // 数据源
//            ReportDatasource dataSource = reportInputs.dataSource
//            def dataSourceMap = new JsonSlurper().parseText(dataSource.config)
//            DatasourceConfigVO datasourceConfigVO = new DatasourceConfigVO(dataSourceMap)
//            def dataSourceKind = datasourceConfigVO.kind
//            // 存在区域云ID为特定的区域数据源
//            if (DatasourceConfigKindEnum.SPECIALLY.kind == dataSourceKind) {
                /**
                 * TODO 获取指定区域云数据
                 */
//                def dataSourceCloudId = datasourceConfigVO.cloudId
//            } else if (DatasourceConfigKindEnum.CENTER.kind == dataSourceKind){
                /**
                 * TODO 中心区域数据
                 */
//            } else if (DatasourceConfigKindEnum.REGION.kind == dataSourceKind){
                /**
                 * TODO 区域数据源 获取职员所在区域数据
                 */
//            }
            /**
             * TODO 执行sql获取查询结果
             */
            def url = grailsApplication.config.getProperty("select.dataSource.url")
            def user = grailsApplication.config.getProperty("select.dataSource.user")
            def pwd = grailsApplication.config.getProperty("select.dataSource.pwd")
            def driverClassName = grailsApplication.config.getProperty("select.dataSource.driverClassName")
            // 执行sql
            Sql db = Sql.newInstance(url,
                    user, pwd,
                    driverClassName )


            // 查询结果
            List rowList = db.rows(sql, sysParamValues)
            if (!rowList) {
                wrapper.errmsg = "动态查询参数数据错误"
                wrapper.errcode = ResultEnum.PARAM_ERROR.code
                render wrapper as JSON
                return
            }

            // 前端显示的文本及其对应值
            List<QueryInputValueVO> queryInputValueVOList = new LinkedList<>()
            rowList.each { row ->
                try {
                    // 值
                    def value = row.getProperty(ReportXmlConstant.QUERYINPUT_VALUE)
                    // 显示文本
                    def title = row.getProperty(ReportXmlConstant.QUERYINPUT_TITLE)

                    QueryInputValueVO queryInputValueVO = new QueryInputValueVO()
                    queryInputValueVO.colTitle = title
                    queryInputValueVO.colValue =  value
                    queryInputValueVOList.add(queryInputValueVO)
                } catch (Exception e) {
                    wrapper.errmsg = "动态查询参数数据错误"
                    wrapper.errcode = ResultEnum.PARAM_ERROR.code
                    render wrapper as JSON
                    return
                }
            }


            map.put("queryInputValueVOs", queryInputValueVOList)
            wrapper.bizData = map
            render wrapper as JSON
            return
        }
        wrapper.errmsg = "参数不能为空"
        wrapper.errcode = ResultEnum.PARAM_ERROR.code
        render wrapper as JSON
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
            List<String> sysParamValues = new ArrayList<>()
            paramList.each { param ->
                // 将sql中参数替换为？，变为可执行参数
                sql = sql.replace(param, "?")
                // 去掉[]，获取参数名称
                String paramName = param[param.indexOf(CommonValue.PARAM_PREFIX) + 1..param.indexOf(CommonValue.PARAM_SUFFIX) - 1]

                // 系统参数
                ReportSystemParamEnum systemParamEnum = ReportSystemParamEnum.getEnumByName(paramName)

                // 输入参数查询语句中的参数只能是系统参数
                if (!systemParamEnum) {
                    result.error = "动态查询参数sql错误"
                    render result as JSON
                    return
                }

                // 获取系统参数的值
                ReportParamValue reportParamValue =  CommonUtil.getSystemParamValue(paramName)
                sysParamValues.add(reportParamValue.value)
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
            result.defValue = defValue

            /**
             * TODO 执行sql获取查询结果
             */
            def url = grailsApplication.config.getProperty("select.dataSource.url")
            def user = grailsApplication.config.getProperty("select.dataSource.user")
            def pwd = grailsApplication.config.getProperty("select.dataSource.pwd")
            def driverClassName = grailsApplication.config.getProperty("select.dataSource.driverClassName")
            // 执行sql
            Sql db = Sql.newInstance(url,
                    user, pwd,
                    driverClassName )


            // 查询结果
            List rowList = db.rows(sql, sysParamValues)
            if (!rowList) {
                render Result.error("动态查询参数数据错误") as JSON
                return
            }

            // 前端显示的文本及其对应值
            List<QueryInputValueVO> queryInputValueVOList = new LinkedList<>()
            rowList.each { row ->
                try {
                    // 值
                    def value = row.getProperty(ReportXmlConstant.QUERYINPUT_VALUE)
                    // 显示文本
                    def title = row.getProperty(ReportXmlConstant.QUERYINPUT_TITLE)

                    QueryInputValueVO queryInputValueVO = new QueryInputValueVO()
                    queryInputValueVO.colTitle = title
                    queryInputValueVO.colValue =  value
                    queryInputValueVOList.add(queryInputValueVO)
                } catch (Exception e) {
                    render Result.error("动态查询参数数据错误") as JSON
                    return
                }
            }

            result.list = queryInputValueVOList
            render result as JSON
            return
        }
        result.error = "参数不能为空"
        render result as JSON
    }
}

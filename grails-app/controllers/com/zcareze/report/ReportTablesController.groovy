package com.zcareze.report

import com.report.common.CommonValue
import com.report.common.ReportXmlConstant
import com.report.dto.ReportParamValue
import com.report.dto.TableParamDTO
import com.report.dto.XmlDataDTO
import com.report.enst.QueryInputDefTypeEnum
import com.report.enst.ReportSystemParamEnum
import com.report.enst.ResultEnum
import com.report.param.KpiReportParam
import com.report.param.ReportViewParam
import com.report.param.ScreenReportParam
import com.report.result.ApiWrapper
import com.report.result.BaseResult
import com.report.result.Result

import com.report.util.CommonUtil
import com.report.vo.ReportDataVO
import com.report.vo.ReportTableVO
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import org.grails.web.json.JSONElement

import java.sql.ResultSetMetaData

import static org.springframework.http.HttpStatus.*

class ReportTablesController {

    DataFacadeService dataFacadeService

    ReportTablesService reportTablesService

    String staffId = "1"
    String staffName = "王"

    def grailsDomainClassMappingContext

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    /**
     * 添加报表数据表
     * @param reportTables
     */
    def addReportTable(ReportTables reportTables) {
        if (reportTables) {
            // 参数验证
            if (!reportTables.validate()) {
                String errorMessage = hasError(reportTables)
                render Result.error(errorMessage) as JSON
                return
            }

            // 外键验证
            Report rpt = reportTables.rpt
            if (!rpt || !(rpt.id)) {
                render Result.error("报表不能为空") as JSON
                return
            }

            if (!reportTables.dataSource) {
                render Result.error("数据源不能为空") as JSON
                return
            }

            // 数据源编码
//            String code = params."dataSource"
//            if (!code) {
//                render Result.error("数据源编码不能为空") as JSON
//                return
//            }
//            // 数据源
//            ReportDatasource dataSource = ReportDatasource.findByCode(code)
//            if (!dataSource) {
//                render Result.error("数据源编码有误") as JSON
//                return
//            }
//            reportTables.dataSource = dataSource

            reportTables.save(flush: true)
            render Result.success() as JSON
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
                String dataSourceCode = params."dataSource"
                params."dataSource" = null
                reportTables.properties = params


                if (dataSourceCode) {
                    def reportDatasource = ReportDatasource.findByCode(dataSourceCode)
                    if (!reportDatasource) {
                        render Result.error("数据源编码有误") as JSON
                        return
                    }
                    reportTables.dataSource = reportDatasource
                }

                if (reportTables.save(flush: true)) {
                    render Result.success() as JSON
                    return
                } else {
                    def errorMessage = hasError(reportTables)
                    render Result.error(errorMessage) as JSON
                    return
                }

            }
            render Result.error("数据表不存在") as JSON
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
        BaseResult<ReportTableVO> result = new BaseResult<>()
        if (reportId) {
            // 前端展示样式
            List<ReportTableVO> reportTableVOList = new LinkedList<>()

            List<ReportTables> reportTablesList = ReportTables.createCriteria().list {
                rpt {
                    eq("id", reportId)
                }
                order("seqNum", "asc")
            }.each { ReportTables tables ->
                // 属性赋值
                ReportTableVO vo = new ReportTableVO()
                bindData(vo, tables)
                vo.rptId = tables.rpt.id
                vo.dataSource = tables.dataSource.code
                reportTableVOList.add(vo)
            }

            result.list = reportTableVOList
            render result as JSON
        }
        result.error = "报表标识不能为空"
        render result as JSON
    }

    /**
     * 获取数据表详情
     * @param reportTablesList 数据表
     * @param paramNameMap 已使用的参数
     * @return
     */
//    private XmlDataDTO getTableData(List<ReportTables> reportTablesList, List<ReportParamValue> usedParamList) {
//        if (!reportTablesList){
//            return null
//        }
//        // 所有涉及到的参数列表
//        Map<String, ReportParamValue> paramNameMap = new HashMap<>()
//        if (usedParamList) {
//            usedParamList.each { param ->
//                paramNameMap.put(param.name, param)
//            }
//        }
//        List<TableParamDTO> tableParamDTOList = new ArrayList<>()
//        reportTablesList.each { ReportTables table ->
//            String reportId = table.rpt.id
//            TableParamDTO tableParamDTO = new TableParamDTO()
//            tableParamDTO.reportTables = table
//
//            /** 解析查询语句中的参数 **/
//            // sql语句
//            String sql = table.sqlText
//
//            // 输入参数动态查询参数解析
//            List<String> paramList = CommonUtil.analysisSql(sql)
//
//            // 参数列表
//            List<String> paramValues = new ArrayList<>()
//
//            // 遍历参数
//            paramList.each { param ->
//                // 将sql中参数替换为？，变为可执行参数
//                sql = sql.replace(param, "?")
//                // 去掉[]，获取参数名称
//                String paramName = param[param.indexOf(CommonValue.PARAM_PREFIX) + 1..param.indexOf(CommonValue.PARAM_SUFFIX) - 1]
//
//                if (paramNameMap.containsKey(paramName)) {
//                    paramValues.add(paramNameMap.get(paramName).value)
//                    return
//                }
//
//                // 系统参数
//                ReportSystemParamEnum systemParamEnum = ReportSystemParamEnum.getEnumByName(paramName)
//                // 参数为系统参数
//                if (systemParamEnum) {
//                    // 取默认值
//                    ReportParamValue reportParamValue = CommonUtil.getSystemParamValue()
//                    if (!reportParamValue) {
//                        return
//                    }
//
//                    paramNameMap.put(paramName, reportParamValue)
//                    paramValues.add(reportParamValue.value)
//                } else {
//
//                    // 查询对应输入参数
//                    ReportInputs reportInputs = ReportInputs.createCriteria().get {
//                        and {
//                            rpt {
//                                eq("id", reportId)
//                            }
//                            eq("name", paramName)
//                        }
//                    }
//
//                    // 取默认值
//                    ReportParamValue reportParamValue = CommonUtil.getInputParamValue(QueryInputDefTypeEnum.getEnumByDefType(reportInputs.defType))
//                    if (!reportParamValue) {
//                        return
//                    }
//
//                    paramNameMap.put(paramName, reportParamValue)
//                    paramValues.add(reportParamValue.value)
//                }
//            }
//            /**
//             * TODO
//             */
//            // 查询结果
//            def url = grailsApplication.config.getProperty("select.dataSource.url")
//            def user = grailsApplication.config.getProperty("select.dataSource.user")
//            def pwd = grailsApplication.config.getProperty("select.dataSource.pwd")
//            def driverClassName = grailsApplication.config.getProperty("select.dataSource.driverClassName")
//            // 执行sql
//            Sql db = Sql.newInstance(url,
//                    user, pwd,
//                    driverClassName )
//            List rows = db.rows(sql, paramValues)
//
//            tableParamDTO.rowList = rows
//
//            tableParamDTOList.add(tableParamDTO)
//        }
//        // 参数列表
//        List<ReportParamValue> paramList = new ArrayList<>()
//        paramList.addAll(paramNameMap.values())
//
//        XmlDataDTO xmlData = new XmlDataDTO()
//        xmlData.paramValueList = paramList
//        xmlData.tableParamDTOList = tableParamDTOList
//        return xmlData
//    }
    public static void main(String[] args) {
        // 执行sql
//        Sql db = Sql.newInstance("jdbc:mysql://rm-bp16c06lj5gooo69a3o.mysql.rds.aliyuncs.com/dev_region?createDatabaseIfNotExist=true&amp;useUnicode=true&amp;characterEncoding=utf-8",
//                "developer_user", "Developer_root",
//                "com.mysql.jdbc.Driver" )
//        Sql s = new Sql(db.getConnection())
//        db.rows("select * from org_list where id = '0'",{ ResultSetMetaData result ->
//            int count = result.getColumnCount()
//            println count
//            for (int i = 1; i < count; i++) {
//                def name = result.getColumnName(i)
//                println name
//            }
//
//        })
        StringWriter out = new StringWriter()
        def xml = new MarkupBuilder(out)
        xml.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
        xml."report"(){
            "var_set"() {
                "var_name"("名称")
            }
        }
        println out.toString()
    }
    /**
     * api获取指定报表的数据
     * @param reportParam
     */
    @Transactional(readOnly = true)
    def getReportDataOld(String paramJson) {
        ApiWrapper wrapper = new ApiWrapper()
        // 参数校验
        if (!paramJson) {
            wrapper.errmsg = "参数为空"
            wrapper.errcode = ResultEnum.PARAM_ERROR.code
            render wrapper as JSON
            return
        }
        JSONElement reportParam = JSON.parse(paramJson)

        // 报表ID
        String reportId = reportParam.reportId

        // 前端已用到的输入参数
        List<ReportParamValue> usedParamList = reportParam.paramValues

        // 数据表
        List<ReportTables> reportTablesList = ReportTables.createCriteria().list {
            rpt {
                eq("id", reportId)
            }
        }
        if (!reportTablesList) {
            wrapper.errcode = ResultEnum.PARAM_ERROR.code
            wrapper.errmsg = "报表信息为空"
            render wrapper as JSON
            return
        }

        // 获取报表数据 xml格式
        XmlDataDTO xmlDataDTO = dataFacadeService.getTableData(reportTablesList, usedParamList)

        ReportDataVO reportDataVO = new ReportDataVO()
        reportDataVO.xmlData = toXmlData(xmlDataDTO)

        Map<String, Object> map = new HashMap<>(1)
        map.put("reportDataVO", reportDataVO)
        wrapper.bizData = map
        render wrapper as JSON
    }

    /**
     * 获取指定报表的数据
     * @param reportParam
     */
    @Transactional(readOnly = true)
    def getReportData(ReportViewParam reportParam) {
        BaseResult<ReportDataVO> result = new BaseResult<>()

        // 参数校验
        if (!reportParam) {
            render Result.error("参数为空") as JSON
            return
        }

        // 报表ID
        String reportId = reportParam.reportId

        // 前端已用到的输入参数
        List<ReportParamValue> usedParamList = reportParam.paramValues

        // 数据表
        List<ReportTables> reportTablesList = ReportTables.createCriteria().list {
            rpt {
                eq("id", reportId)
            }
        }
        if (!reportTablesList) {
            result.setError("报表信息为空")
            render result as JSON
            return
        }

        // 获取报表数据 xml格式
        XmlDataDTO xmlDataDTO = dataFacadeService.getTableData(reportTablesList, usedParamList)

        ReportDataVO reportDataVO = new ReportDataVO()
        reportDataVO.xmlData = toXmlData(xmlDataDTO)
        result.one = reportDataVO
        render result as JSON
    }

    /**
     * api获取指定大屏的所有数据表的结构
     * @param reportId
     */
    @Transactional(readOnly = true)
    def getScreenReportStructOld(String reportId) {
        ApiWrapper wrapper = new ApiWrapper()

        // 数据表
        List<ReportTables> reportTablesList = ReportTables.createCriteria().list {
            rpt {
                eq("id", reportId)
            }
        }
        if (!reportTablesList) {
            wrapper.errmsg = "报表信息为空"
            wrapper.errcode = ResultEnum.PARAM_ERROR.code
            render wrapper as JSON
            return
        }

        // 获取报表xml文本填充数据
        XmlDataDTO xmlDataDTO = dataFacadeService.getTableData(reportTablesList, null)
        String xml = toStructData(xmlDataDTO)

        ReportDataVO reportDataVO = new ReportDataVO()
        reportDataVO.xmlData = xml

        Map<String, Object> map = new HashMap<>(1)
        map.put("reportDataVO", reportDataVO)
        wrapper.bizData = map
        render wrapper as JSON
    }

    /**
     * 获取指定大屏的所有数据表的结构
     * @param reportId
     */
    @Transactional(readOnly = true)
    def getScreenReportStruct(String reportId) {
        BaseResult<ReportDataVO> result = new BaseResult<>()

        // 数据表
        List<ReportTables> reportTablesList = ReportTables.createCriteria().list {
            rpt {
                eq("id", reportId)
            }
        }
        if (!reportTablesList) {
            result.setError("报表信息为空")
            render result as JSON
            return
        }

        // 获取报表xml文本填充数据
        XmlDataDTO xmlDataDTO = dataFacadeService.getTableData(reportTablesList, null)
        String xml = toStructData(xmlDataDTO)

        ReportDataVO reportDataVO = new ReportDataVO()
        reportDataVO.xmlData = xml
        result.one = reportDataVO
        render result as JSON
    }

    /**
     * api获取大屏指定表数据
     * @param screenReportParam
     */
    @Transactional(readOnly = true)
    def getScreenReportDataOld(String paramJson) {
        ApiWrapper wrapper = new ApiWrapper()
        if (!paramJson) {
            wrapper.errmsg = "参数为空"
            wrapper.errcode = ResultEnum.PARAM_ERROR.code
            render wrapper as JSON
            return
        }
        JSONElement screenReportParam = JSON.parse(paramJson)
        BaseResult<ReportDataVO> result = new BaseResult<>()

        // 报表ID
        String reportId = screenReportParam.reportId

        // 数据表名列表
        List<String> tableName = screenReportParam.tableNames
        if (!tableName) {
            wrapper.errmsg = "数据表名不能为空"
            wrapper.errcode = ResultEnum.PARAM_ERROR.code
            render wrapper as JSON
            return
        }

        // 数据表列表
        List<ReportTables> reportTablesList = ReportTables.createCriteria().list {
            and{
                rpt {
                    eq("id", reportId)
                }
                or {
                    tableName.each { name ->
                        eq("name", name)
                    }
                }
            }
        }
        if (!reportTablesList) {
            render wrapper as JSON
            return
        }

        XmlDataDTO xmlDataDTO = dataFacadeService.getTableData(reportTablesList, screenReportParam.paramValues)
        String xml = toXmlData(xmlDataDTO)

        ReportDataVO reportDataVO = new ReportDataVO()
        reportDataVO.xmlData = xml

        Map<String, Object> map = new HashMap<>(1)
        map.put("reportDataVO", reportDataVO)
        wrapper.bizData = map
        render wrapper as JSON
    }

    /**
     * 获取大屏指定表数据
     * @param screenReportParam
     */
    @Transactional(readOnly = true)
    def getScreenReportData(ScreenReportParam screenReportParam) {
        BaseResult<ReportDataVO> result = new BaseResult<>()
        if (!screenReportParam) {
            result.setError("参数为空")
            render result as JSON
            return
        }

        // 报表ID
        String reportId = screenReportParam.reportId

        // 数据表名列表
        List<String> tableName = screenReportParam.tableNames
        if (!tableName) {
            result.error = "数据表名不能为空"
            render result as JSON
            return
        }

        // 数据表列表
        List<ReportTables> reportTablesList = ReportTables.createCriteria().list {
            and{
                rpt {
                    eq("id", reportId)
                }
                or {
                    tableName.each { name ->
                        eq("name", name)
                    }
                }
            }
        }
        if (!reportTablesList) {
            render result as JSON
            return
        }

        XmlDataDTO xmlDataDTO = dataFacadeService.getTableData(reportTablesList, screenReportParam.paramValues)
        String xml = toXmlData(xmlDataDTO)

        ReportDataVO reportDataVO = new ReportDataVO()
        reportDataVO.xmlData = xml
        result.one = reportDataVO
        render result as JSON
    }

    /**
     * 获取指标关联报表数据，关联的指标仅能将组织ID和周期值当做参数
     * @param kpiReportParam
     */
    @Transactional(readOnly = true)
    def getKpiReportData(KpiReportParam kpiReportParam) {
        BaseResult<ReportDataVO> result = new BaseResult<>()

        if (!kpiReportParam) {
            result.setError("参数不能为空")
            render result as JSON
            return
        }

        /**
         * TODO 根据指标ID和报表ID查询指标对应报表的参数列表
         */
        List<ReportParamValue> useParamList = new ArrayList<>()
        List<String> paramNameList = new ArrayList<>()

        // 遍历指标参数列表
        for (int i = 0; i < 10; i++) {
            // 指标参数分为3种赋值方式
            String value = ""
            String title = ""
            switch ("指标赋值方式") {
                case 1: // 指标期间
                    value = kpiReportParam.cycValue
                    title = value
                    break
                case 2: //指标组织ID
                    value = kpiReportParam.orgId
                    title = "对应组织名称"
                    break
                default:
                    break
            }
            if (!paramNameList.contains("指标赋值的参数名称")) {
                ReportParamValue reportParamValue = new ReportParamValue()
                reportParamValue.name = "指标赋值的参数名称"
                reportParamValue.title = title
                reportParamValue.value = value
                useParamList.add(reportParamValue)

                paramNameList.add("指标赋值的参数名称")
            }
        }

        // 报表ID
        String reportId = kpiReportParam.reportId
        // 关联数据表
        List<ReportTables> reportTablesList = ReportTables.createCriteria().list {
            rpt{
                eq("id", reportId)
            }
        }
        // 获取报表关联数据表详情
        XmlDataDTO xmlDataDTO = dataFacadeService.getTableData(reportTablesList, useParamList)
        // xml格式数据
        String xml = toXmlData(xmlDataDTO)

        ReportDataVO reportDataVO = new ReportDataVO()
        reportDataVO.xmlData = xml
        result.one = reportDataVO
        render result as JSON
    }
    /**
     * 生成xml格式报表显示数据
     * @param xmlDataDTO
     * @return
     */
    private String toXmlData(XmlDataDTO xmlDataDTO) {
        if (!xmlDataDTO) {
            return null
        }
        List<TableParamDTO> tableParamDTOList = xmlDataDTO.tableParamDTOList
        List<ReportParamValue> paramValueList = xmlDataDTO.paramValueList
        if (tableParamDTOList && paramValueList) {
            StringWriter out = new StringWriter()
            def xml = new MarkupBuilder(out)
            xml.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
            xml."${ReportXmlConstant.XML_ROOTNODE}"(){
                "${ReportXmlConstant.XML_PARAMETER}"(){
                    paramValueList.each { param ->
                        "${ReportXmlConstant.XML_PARAMETER_RECORD}"() {
                            "${ReportXmlConstant.XML_PARAMETER_RECORD_NAME}"(param.name)
                            "${ReportXmlConstant.XML_PARAMETER_RECORD_VALUE}"(param.value)
                            "${ReportXmlConstant.XML_PARAMETER_RECORD_TITLE}"(param.title)
                        }
                    }

                }
                tableParamDTOList.each{ TableParamDTO table ->
                    "${ReportXmlConstant.XML_TABLE}"("${ReportXmlConstant.XML_TABLE_NAME}": table.reportTables.name, "${ReportXmlConstant.XML_TABLE_SEQNUM}": table.reportTables.seqNum){
                        table.rowList.each { row ->
                            "${ReportXmlConstant.XML_TABLE_RECORD}"() {
                                row.each { name, value ->
                                    String key = CommonUtil.toHumpStr(name)
                                    "${key}" value
                                }
                            }
                        }
                    }
                }

            }
            return out.toString()
        }
        return null
    }
    /**
     * 转换为大屏所用的xml格式数据
     * @param xmlDataDTO
     * @return
     */
    private String toStructData(XmlDataDTO xmlDataDTO) {
        if (!xmlDataDTO) {
            return null
        }
        List<TableParamDTO> tableParamDTOList = xmlDataDTO.tableParamDTOList
        List<ReportParamValue> paramValueList = xmlDataDTO.paramValueList
        if (tableParamDTOList && paramValueList) {
            StringWriter out = new StringWriter()
            def xml = new MarkupBuilder(out)
            xml.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
            xml."${ReportXmlConstant.XML_ROOTNODE}"(){
                "${ReportXmlConstant.XML_PARAMETER}"(){
                    paramValueList.each { param ->
                        "${ReportXmlConstant.XML_PARAMETER_RECORD}"() {
                            "${ReportXmlConstant.XML_PARAMETER_RECORD_NAME}"(param.name)
                            "${ReportXmlConstant.XML_PARAMETER_RECORD_VALUE}"(param.value)
                            "${ReportXmlConstant.XML_PARAMETER_RECORD_TITLE}"(param.title)
                        }
                    }

                }
                tableParamDTOList.each{ TableParamDTO table ->
                    "${ReportXmlConstant.XML_TABLE}"(){
                        "${ReportXmlConstant.XML_TABLE_NAME}"(table.reportTables.name)
                        "${ReportXmlConstant.XML_TABLE_COLUMNS}"() {
                            table.columnNameList.each { name ->
                                "${ReportXmlConstant.XML_TABLE_COLUMN}"(name)
                            }
                        }

                    }
                }
            }
            return out.toString()
        }
        return null
    }
}

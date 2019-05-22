package com.zcareze.report


import com.report.common.CommonValue
import com.report.enst.CtrlStatusEnum
import com.report.enst.DatasourceConfigKindEnum
import com.report.enst.InputDataTypeEnum
import com.report.enst.ReportSystemParamEnum
import com.report.enst.ResultEnum
import com.report.enst.StyleSceneEnum
import com.report.result.ApiWrapper
import com.report.result.BaseResult
import com.report.result.Result
import com.report.service.IFileService
import com.report.util.CommonUtil
import com.report.vo.DatasourceConfigVO
import com.report.vo.ReportViewVO
import com.report.vo.ViewParameterVO
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import org.grails.web.json.JSONObject

import static org.springframework.http.HttpStatus.*

class ReportController {

    ReportService reportService

    IFileService fileService

    String staffName = "王"
    String staffId = "1"
    String cloudId = "1"
    boolean isCloudManage = false

    def grailsDomainClassMappingContext

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    /**
     * 新增报表
     * @param report
     * @return
     */
    def addReportList(Report report) {
        if (!report.validate()) {
            def errorMessage = hasError(report)
            render Result.error(errorMessage) as JSON
            return
        }

        report.ctrlStatus = CtrlStatusEnum.BZZCY.code
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
            String reportId = report.id
            String name = report.name
            String code = report.code
            if (!reportId) {
                render Result.error("报表标识不能为空") as JSON
                return
            }
            if (!name) {
                render Result.error("报表名称不能为空") as JSON
                return
            }
            if (!code) {
                render Result.error("报表编码不能为空") as JSON
                return
            }
            Report actualReport = Report.get(reportId)
            if (!actualReport) {
                render Result.error("报表不存在") as JSON
                return
            }
            String cloudId = report.cloudId
            Report existsReport = Report.withTenant(cloudId){
                Report.findByNameOrCode(name, code)
            }
            if (existsReport != null && !(existsReport.id == report.id)) {
                render Result.error("报表已存在") as JSON
                return
            }
            bindData(actualReport, report)

            actualReport.save(flush: true)
            render Result.success() as JSON
            return
        }
        render Result.error("报表不存在") as JSON
    }

    /**
     * 删除报表
     * @param id
     */
    @Transactional
    def deleteReportList(String id) {
        if (id) {
            Report report = Report.get(id)
            if (!report) {
                render Result.error("报表不存在") as JSON
                return
            }

            // 只有草稿态才能删除
            if (report.ctrlStatus != CtrlStatusEnum.BZZCY.code) {
                render Result.error("只有草稿状态的报表才能删除") as JSON
                return
            }

            // 删除个人常用报表
            def reportUsually = ReportUsually.where {
                rpt {
                    eq("id", id)
                }
            }.each {
                it.delete()
            }

            // 删除报表及其级联数据
            if (report) {
                report.delete(flush: true)
            }
            render Result.success() as JSON
        } else {
            render Result.error("报表标识不能为空") as JSON
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
            result.setError("报表标识不能为空")
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
        result.list = Report.withTenant(cloudId){
            Report.createCriteria().list {
                if (name || groupCode) {
                    and {
                        if (name) {
                            ilike("name", "%" + name + "%")
                        }
                        if (groupCode) {
                            eq("grpCode", groupCode)
                        } else {
                            ne("grpCode", "99")
                        }
                    }
                } else {
                    or {
                        ne("grpCode", "99")
                        isNull("grpCode")
                    }
                }
                if (pageNow > -1 && pageSize > -1) {
                    firstResult pageNow * pageSize
                    maxResults pageSize
                }
                order "code", "asc"
            }
        }
        render result as JSON
    }

    /**
     * 条件查询报表清单
     * @param groupCode
     * @param reportName
     * @param rptCloudId
     * @param ctrlStatus
     * @param pageNow
     * @param pageSize
     * @return
     */
    def getByCondition(String groupCode, String reportName, String rptCloudId, Integer ctrlStatus, Integer pageNow, Integer pageSize) {
        BaseResult<Report> result = new BaseResult<>()
        if (!rptCloudId) {
            rptCloudId = null
        }
        result.list = Report.withTenant(rptCloudId){
            Report.createCriteria().list {
                and {
                    if (groupCode) {
                        eq("grpCode", groupCode)
                    } else {
                        or {
                            ne("grpCode", "99")
                            isNull("grpCode")
                        }
                    }
                    if (reportName) {
                        ilike("name", "%"+reportName+"%")
                    }
                    if (ctrlStatus > -1) {
                        eq("ctrlStatus", ctrlStatus)
                    }
                }
                order "code", "asc"
                if (pageNow > -1 && pageSize > -1) {
                    firstResult pageNow * pageSize
                    maxResults pageSize
                }
            }
        }
        render result as JSON
    }

    /**
     * api获取监控大屏报表清单
     * @param name
     */
    def getScreenReportListOld(String name) {
        ApiWrapper wrapper = new ApiWrapper()
        def reportList = Report.withTenant(cloudId){
            Report.createCriteria().list {
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
        }
        Map<String, Object> map = new HashMap<>(1)
        map.put("reportListVOs", reportList)
        wrapper.bizData = map
        render wrapper as JSON
    }

    /**
     * 获取监控大屏报表清单
     * @param name
     */
    def getScreenReportList(String name) {
        BaseResult<Report> result = new BaseResult<>()
        result.list = Report.withTenant(cloudId){
            Report.createCriteria().list {
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
        }
        render result as JSON
    }

    /**
     * 获取指定报表最后修改时间
     * @param id
     */
//    def getReportListUpdateTime(String id) {
//        BaseResult<Date> result = new BaseResult<>()
//        if (id) {
//            Report report = Report.get(id)
//            if (report) {
//                Date updateTime = report.editTime
//                result.one = updateTime
//                render result as JSON
//            } else {
//                result.setError("报表不存在")
//                render result as JSON
//            }
//        } else {
//            result.setError("标识不能为空")
//            render result as JSON
//        }
//    }

    /**
     * 获取xlst文件的实际访问地址 (用于获取oss访问地址)
     * @param fileUrl
     */
    def getVisitFileUrl(String fileUrl) {
        BaseResult<String> result = new BaseResult<>()
        if (!fileUrl) {
            result.error = "地址不能为空"
            render result as JSON
            return
        }
        /**
         * TODO 文件服务，获取oss真实访问路径
         */
        JSONObject jsonObject = fileService.getReportFileVisitUrl(fileUrl, staffId)
        if (!jsonObject) {
            render Result.error("获取文件访问路径失败") as JSON
            return
        }
        def errCode = jsonObject.get("errcode")
        if (errCode == 0) {
            def visitUrlDTO = jsonObject.get("visitUrlDTO")
            if (visitUrlDTO) {
                result.one = visitUrlDTO.ossUrl
            }
        }
        render result as JSON
    }

    /**
     * api获取报表信息 主要用于前端获取报表基本信息使用
     * @param id
     * @param scene
     */
    @Transactional(readOnly = true)
    def getReportViewByIdOld(String id, Integer scene) {
        ApiWrapper wrapper = new ApiWrapper()
        BaseResult<ReportViewVO> result = new BaseResult<>()
        if (id) {
            // 报表
            Report report = Report.createCriteria().get{
                and{
                    eq("id", id)
                    styleList {
                        eq ("scene", scene)
                    }
                }
            }
            if (!report) {
                report = Report.createCriteria().get{
                    and{
                        eq("id", id)
                        styleList {
                            eq ("scene", StyleSceneEnum.CURRENCY.scene)
                        }
                    }
                }
            }
            if (report) {
                Set<ReportTables> tablesSet = report.tableList
                if (!tablesSet) {
                    wrapper.errmsg = "数据为空"
                    wrapper.errcode = ResultEnum.PARAM_ERROR.code
                    render wrapper as JSON
                    return
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
                        bindData(viewParameterVO, inputs)

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
//                Long timeLong = 0L
//                if (report.editTime) {
//                    timeLong = report.editTime.getTime()
//                }

                // 报表样式
                ReportStyle style = report.styleList[0]
                String fileUrl = style.fileUrl
                if (fileUrl) {
                    /**
                     * TODO 获取xslt真实访问路径
                     */
                    JSONObject jsonObject = fileService.getReportFileVisitUrl(style.fileUrl, staffId)
                    if (!jsonObject) {
                        wrapper.errmsg = "获取文件访问路径失败"
                        wrapper.errcode = ResultEnum.PARAM_ERROR.code
                        render wrapper as JSON
                        return
                    }
                    def errCode = jsonObject.get("errcode")
                    if (errCode == 0) {
                        def visitUrlDTO = jsonObject.get("visitUrlDTO")
                        if (visitUrlDTO) {
                            fileUrl = visitUrlDTO.ossUrl
                        }
                    }
                }
                //reportViewVO.access(report.id, report.code, report.name, report.grpCode, fileUrl, style.chart, report.editTime, timeLong, report.runway)
                reportViewVO.access(report.id, report.code, report.name, report.grpCode, fileUrl, style.chart, null, null, report.runway)
                reportViewVO.inputParams = viewParameterVOList

                Map<String, Object> map = new HashMap<>(1)
                map.put("reportViewVOs", reportViewVO)
                wrapper.bizData = map
                render wrapper as JSON
                return
            } else {
                wrapper.errmsg = "报表不存在"
                wrapper.errcode = ResultEnum.PARAM_ERROR.code
            }
        } else {
            wrapper.errmsg = "报表标识不能为空"
            wrapper.errcode = ResultEnum.PARAM_ERROR.code
        }
        render wrapper as JSON
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
                    return
                }

                // 前端显示输入参数样式
                List<ViewParameterVO> viewParameterVOList = new ArrayList<>()

                // 不重复的输入参数
                Map<String, ViewParameterVO> parameterVOMap = new HashMap<>()

                // 只获取数据表中用到的输入参数
                tablesSet.each { ReportTables table ->
                    // 数据源
                    ReportDatasource datasource = table.dataSource
                    // 数据源配置
                    String config  = datasource.config
                    def configMap = new JsonSlurper().parseText(config)
                    DatasourceConfigVO datasourceConfigVO = new DatasourceConfigVO(configMap)
                    // 数据源类型
                    Integer kind = datasourceConfigVO.kind

                    List<String> paramList = new ArrayList<>()

                    // 自定义方式
                    if (DatasourceConfigKindEnum.DATA_CENTER_REAL_TABLE.kind != kind) {
                        String sql = table.sqlText
                        paramList = CommonUtil.analysisSql(sql)
                    } else {
                        paramList.add("startWith");
                        paramList.add("endWith");
                    }

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
                        bindData(viewParameterVO, inputs)

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
//                Long timeLong = 0L
//                if (report.editTime) {
//                    timeLong = report.editTime.getTime()
//                }

                // 报表样式
                ReportStyle style = report.styleList[0]
                String fileUrl = style.fileUrl
                if (fileUrl) {
                    /**
                     * TODO 获取xslt真实访问路径
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
                            fileUrl = visitUrlDTO.ossUrl
                        }
                    }
                }
                //reportViewVO.access(report.id, report.code, report.name, report.grpCode, fileUrl, style.chart, report.editTime, timeLong, report.runway)
                reportViewVO.access(report.id, report.code, report.name, report.grpCode, fileUrl, style.chart, null, null, report.runway)
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
}

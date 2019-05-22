package com.zcareze.report

import com.report.common.CommonValue
import com.report.dto.ReportParamValue
import com.report.dto.TableParamDTO
import com.report.dto.XmlDataDTO
import com.report.enst.DatasourceConfigKindEnum
import com.report.enst.QueryInputDefTypeEnum
import com.report.enst.ReportSystemParamEnum
import com.report.service.IDataCenterService
import com.report.util.CommonUtil
import com.report.vo.DatasourceConfigVO
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.apache.http.HttpEntity
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder

import java.sql.ResultSetMetaData

@Transactional
class DataFacadeService {

    @Autowired
    IDataCenterService dataCenterService

    XmlDataDTO getTableData(List<ReportTables> reportTablesList, List<ReportParamValue> usedParamList) {
        if (!reportTablesList){
            return null
        }
        // 所有涉及到的参数列表
        Map<String, ReportParamValue> paramNameMap = new HashMap<>()
        if (usedParamList) {
            usedParamList.each { param ->
                paramNameMap.put(param.name, param)
            }
        }
        List<TableParamDTO> tableParamDTOList = new ArrayList<>()
        reportTablesList.each { ReportTables table ->
            TableParamDTO tableParamDTO = new TableParamDTO()
            tableParamDTO.reportTables = table

            String reportId = table.rpt.id

            // 数据源
            ReportDatasource datasource = table.dataSource
            // 数据源配置
            String config  = datasource.config
            def configMap = new JsonSlurper().parseText(config)
            DatasourceConfigVO datasourceConfigVO = new DatasourceConfigVO(configMap)
            // 数据源类型
            Integer kind = datasourceConfigVO.kind

            // 不是数据中心实表查询
            if (DatasourceConfigKindEnum.DATA_CENTER_REAL_TABLE.kind != kind) {
                /** 解析查询语句中的参数 **/
                // sql语句
                String sql = table.sqlText

                // 输入参数动态查询参数解析
                List<String> paramList = CommonUtil.analysisSql(sql)

                // 参数列表
                List<String> paramValues = new ArrayList<>()

                // 遍历参数
                paramList.each { param ->
                    // 将sql中参数替换为？，变为可执行参数
                    sql = sql.replace(param, "?")
                    // 去掉[]，获取参数名称
                    String paramName = param[param.indexOf(CommonValue.PARAM_PREFIX) + 1..param.indexOf(CommonValue.PARAM_SUFFIX) - 1]

                    if (paramNameMap.containsKey(paramName)) {
                        paramValues.add(paramNameMap.get(paramName).value)
                        return
                    }

                    // 系统参数
                    ReportSystemParamEnum systemParamEnum = ReportSystemParamEnum.getEnumByName(paramName)
                    // 参数为系统参数
                    if (systemParamEnum) {
                        // 取默认值
                        ReportParamValue reportParamValue = CommonUtil.getSystemParamValue()
                        if (!reportParamValue) {
                            return
                        }

                        paramNameMap.put(paramName, reportParamValue)
                        paramValues.add(reportParamValue.value)
                    } else {

                        // 查询对应输入参数
                        ReportInputs reportInputs = ReportInputs.createCriteria().get {
                            and {
                                rpt {
                                    eq("id", reportId)
                                }
                                eq("name", paramName)
                            }
                        }

                        // 取默认值
                        ReportParamValue reportParamValue = CommonUtil.getInputParamValue(QueryInputDefTypeEnum.getEnumByDefType(reportInputs.defType))
                        if (!reportParamValue) {
                            return
                        }

                        paramNameMap.put(paramName, reportParamValue)
                        paramValues.add(reportParamValue.value)
                    }
                }
                /**
                 * TODO
                 */
                List rows = null
                List<String> columnNameList = new ArrayList()
                // 非数据中心虚表查询
                if (DatasourceConfigKindEnum.DATA_CENTER_VIRTUAL_TABLE.kind != kind) {
                    def grailsApplication = ((GrailsWebRequest)RequestContextHolder.currentRequestAttributes()).getAttributes().getGrailsApplication()
                    // 查询结果
                    def url = grailsApplication.config.getProperty("select.dataSource.url")
                    def user = grailsApplication.config.getProperty("select.dataSource.user")
                    def pwd = grailsApplication.config.getProperty("select.dataSource.pwd")
                    def driverClassName = grailsApplication.config.getProperty("select.dataSource.driverClassName")
                    // 执行sql
                    Sql db = Sql.newInstance(url,
                            user, pwd,
                            driverClassName)
                    rows = db.rows(sql, paramValues, { ResultSetMetaData result ->
                        // 字段名
                        int count = result.getColumnCount()
                        for (i in 1..count) {
                            def name = result.getColumnName(i)
                            String key = CommonUtil.toHumpStr(name)
                            columnNameList.add(key)
                        }
                    })
                } else {
                    rows = dataCenterService.getVirtualData(sql, paramValues)
                    if (rows) {
                        def value = rows.get(0)
                        value.each {k, v ->
                            columnNameList.add(k)
                        }
                    }
                }
                tableParamDTO.rowList = rows
                tableParamDTO.columnNameList = columnNameList
            }else{ // 数据中心
                // 开始时间
                def startTimeParam = paramNameMap.get("startWith")
                // 结束时间
                def endTimeParam = paramNameMap.get("endWith")
                String startTime = startTimeParam?.value
                String endTime = endTimeParam?.value

                List<String> columnNameList = new ArrayList()
                if (startTime && endTime) {
                    tableParamDTO.rowList = dataCenterService.getData(startTime, endTime, table.name, new HashMap<String, Object>())
                    if (tableParamDTO.rowList) {
                        def value = tableParamDTO.rowList.get(0)
                        value.each {k, v ->
                            columnNameList.add(k)
                        }
                    }
                }
                tableParamDTO.columnNameList = columnNameList
            }
            tableParamDTOList.add(tableParamDTO)
        }
        // 参数列表
        List<ReportParamValue> paramList = new ArrayList<>()
        paramList.addAll(paramNameMap.values())

        XmlDataDTO xmlData = new XmlDataDTO()
        xmlData.paramValueList = paramList
        xmlData.tableParamDTOList = tableParamDTOList
        return xmlData
    }
}

package com.zcareze.report

import com.report.common.CommonValue
import com.report.dto.ReportParamValue
import com.report.dto.TableParamDTO
import com.report.dto.XmlDataDTO
import com.report.enst.QueryInputDefTypeEnum
import com.report.enst.ReportSystemParamEnum
import com.report.service.IDataCenterService
import com.report.util.CommonUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
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
            Integer queryMode = table.queryMode

            // 自定义查询
            if (queryMode == 0) {
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
                List<String> columnNameList = new ArrayList()
                List rows = db.rows(sql, paramValues, { ResultSetMetaData result ->
                    // 字段名
                    int count = result.getColumnCount()
                    for (i in 1..count) {
                        def name = result.getColumnName(i)
                        String key = CommonUtil.toHumpStr(name)
                        columnNameList.add(key)
                    }
                })

                tableParamDTO.rowList = rows
                tableParamDTO.columnNameList = columnNameList
            } else if (queryMode == 1) { // 数据中心
                // 开始时间
                def startTimeParam = paramNameMap.get("startWith")
                // 结束时间
                def endTimeParam = paramNameMap.get("endWith")
                String startTime = startTimeParam?.value
                String endTime = endTimeParam?.value
                if (startTime && endTime) {
                    tableParamDTO.rowList = dataCenterService.getData(startTime, endTime, table.name, new HashMap<String, Object>())
                    tableParamDTO.columnNameList = new ArrayList()
                }
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

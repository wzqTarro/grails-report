package com.zcareze.report

import au.com.dius.pact.consumer.PactVerificationResult
import au.com.dius.pact.consumer.groovy.PactBuilder
import com.report.dto.ReportParamValue
import com.report.param.ReportViewParam
import com.report.param.ScreenReportParam
import com.report.service.IDataCenterService
import com.report.util.XmlUtil
import feign.Feign
import feign.gson.GsonDecoder
import feign.gson.GsonEncoder
import feign.httpclient.ApacheHttpClient
import grails.converters.JSON
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import groovy.sql.Sql
import org.springframework.test.annotation.Rollback
import spock.lang.Ignore
import spock.lang.Specification

import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Validator

@Integration
@Rollback
//@Ignore // 忽略当前测试用例
class ReportTablesControllerPactSpec extends Specification implements ControllerUnitTest<ReportTablesController> {
    void setupSpec() {
        def url = grailsApplication.config.getProperty("select.dataSource.url")
        def user = grailsApplication.config.getProperty("select.dataSource.user")
        def pwd = grailsApplication.config.getProperty("select.dataSource.pwd")
        def driverClassName = grailsApplication.config.getProperty("select.dataSource.driverClassName")
        Sql db = Sql.newInstance(url,
                user, pwd,
                driverClassName )
        db.execute("DROP TABLE IF EXISTS org_list;\n" +
                "CREATE TABLE org_list(ID CHAR(32) PRIMARY KEY, NAME VARCHAR(50), KIND VARCHAR(1));\n" +
                "INSERT INTO `org_list`(`id`,  `name`, `kind`) VALUES ('ef325711521b11e6bbd8d017c2939671',  '众康云医院', 'H');\n" +
                "INSERT INTO `org_list`(`id`,  `name`, `kind`) VALUES ('3bb0a1735bbb4f03af816720d6fcf537','外联机构', 'H');\n" +
                "INSERT INTO `org_list`(`id`, `name`, `kind`) VALUES ('3b8d8669f0cf459d91cdd0aea81ed65b', '松树桥卫生服务中心', 'H');")
        db.execute("DROP TABLE IF EXISTS resident_list;\n" +
                "CREATE TABLE RESIDENT_LIST(ID CHAR(32) PRIMARY KEY, commit_time DATE, ORG_ID CHAR(32));\n" +
                "INSERT INTO `resident_list`(`id`,  `commit_time`, `ORG_ID`) VALUES ('ef325711521b11e6bbd8d017c2939671',  '2019-03-02', 'ef325711521b11e6bbd8d017c2939671');\n" +
                "INSERT INTO `resident_list`(`id`,  `commit_time`, `ORG_ID`) VALUES ('3bb0a1735bbb4f03af816720d6fcf537','2018-01-01', 'ef325711521b11e6bbd8d017c2939671');\n" +
                "INSERT INTO `resident_list`(`id`, `commit_time`, `ORG_ID`) VALUES ('3b8d8669f0cf459d91cdd0aea81ed65b', '2019-03-16', 'ef325711521b11e6bbd8d017c2939671');")
    }
    void setup() {
        Report.withNewSession {
            def group = new ReportGroups(code:"06", name:"pc报表样式")
            group.save()
            def report4 = new Report(code: "0315", name:"建档情况分析表", runway: 1, grpCode: group.code)
            report4.save(flush: true)

            new ReportStyle(rpt: report4, scene:0, fileUrl:"asd",chart: "<chart name=\\\"chart1\\\" theme=\\\"walden\\\" tab=\\\"建档情况\\\"><type>bar</type><title>机构建档情况</title><x_col>机构名称</x_col><y_col><field>建档数</field><name>建档数</name></y_col></chart>").save()
            new ReportTables(queryMode: 0, name:"createRecord", rpt: report4, seqNum: 2, sqlText: "select ol.`id` as name, COUNT(rl.`id`) as count FROM org_list ol INNER JOIN `resident_list` rl on rl.`org_id`= ol.`id` where ol.`id` = [orgID] and rl.`commit_time` BETWEEN '2019-03-01' and '2019-03-30' GROUP BY ol.`id`").save()
            new ReportTables(queryMode: 1, name:"dataCenter", rpt: report4, seqNum: 1).save()
            new ReportInputs(caption: "机构", dataType: "31", defType: "我的机构", inputType: 3, name: "orgID", rpt: report4,
                    seqNum: 1, sqlText: "select ol.id col_value,ol.name col_title from org_list ol where ol.id=[my_org_id]",
            ).save()
            new ReportInputs(caption: "开始日期", dataType: "23", defType: "月初", inputType: 1, name: "startWith", rpt: report4,
                    seqNum: 2
            ).save()
            new ReportInputs(caption: "结束日期", dataType: "23", defType: "今天", inputType: 1, name: "endWith", rpt: report4,
                    seqNum: 3
            ).save()
        }
    }
    void cleanup() {
        Report.withNewSession {
            ReportUsually.executeUpdate("delete ReportUsually")
            ReportTables.executeUpdate("delete ReportTables")
            ReportStyle.executeUpdate("delete ReportStyle")
            ReportGrantTo.executeUpdate("delete ReportGrantTo")
            ReportOpenTo.executeUpdate("delete ReportOpenTo")
            ReportInputs.executeUpdate("delete ReportInputs")
            ReportGroups.executeUpdate("delete ReportGroups")
            Report.executeUpdate("delete Report")
        }
    }
    void "获取指定报表的数据-契约测试1"() {
        given:
            def api = new PactBuilder()
            api{
                serviceConsumer "DATA-CENTER-SERVICE"
                hasPactWith "DataSrv"
                port 1234
            }
            api{
                given("获取数据")
                withAttributes(method: 'get', path: '/metrics',
                        headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                        query: [startWith:'2019-01-12', endWith:'2019-04-20', dataType: 'dataCenter']
                )
                willRespondWith(
                        status: 200,
                        headers: ['Content-Type': 'application/json'],
                        body:[
                                [
                                        orgid:"1",
                                        orgName:"firstHospital"
                                ],
                                [
                                        orgid:"2",
                                        orgName:"lastHospital"
                                ]

                        ]
                )
            }

        when:"执行"
            def result = api.runTest { mockServer ->
                def dataApi = Feign.builder()
                        .client(new ApacheHttpClient())
                        .decoder(new GsonDecoder())
                        .encoder(new GsonEncoder())
                        .target(IDataCenterService.class, mockServer.url as String)

                def reportId = null
                Report.withNewSession {
                    Report report = Report.findByCode("0315")
                    reportId = report.id
                }

                ReportViewParam reportParam = new ReportViewParam()
                reportParam.reportId = reportId

                ReportParamValue paramValue = new ReportParamValue()
                paramValue.name = "orgID"
                paramValue.value = "ef325711521b11e6bbd8d017c2939671"
                paramValue.title = "zky"

                ReportParamValue startWith = new ReportParamValue()
                startWith.name = "startWith"
                startWith.value = "2019-01-12"
                startWith.title = "startTime"

                ReportParamValue endWith = new ReportParamValue()
                endWith.name = "endWith"
                endWith.value = "2019-04-20"
                endWith.title = "endTime"
                List<ReportParamValue> paramValues = new ArrayList<>()
                paramValues.add(paramValue)
                paramValues.add(startWith)
                paramValues.add(endWith)
                reportParam.paramValues = paramValues

                DataFacadeService dataFacadeService = new DataFacadeService()
                dataFacadeService.dataCenterService = dataApi
                controller.dataFacadeService = dataFacadeService
                controller.getReportData(reportParam)

                assert response.status == 200
                assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

                def jsonData = response.json
                assert jsonData.code == 1
                assert jsonData.message == "执行成功"

                def one = jsonData.one
                assert one
                def xml = one.xmlData
                assert xml

                Validator xmlValidate = XmlUtil.validateReport()
                assert xmlValidate.validate(new StreamSource(new ByteArrayInputStream(xml.bytes))) == null

                def langs = new XmlParser().parseText(xml)

                // 根节点
                def rootName = langs.name()
                assert rootName == "report"

                def varSet = langs.var_set
                assert varSet
                assert varSet.size() == 1

                def varRecord = varSet.var_record
                assert varRecord
                assert varRecord.size() == 3
                def record = varRecord[0]
                assert record.name() == "var_record"

                def record1 = varRecord[1]
                assert record1.name() == "var_record"

                def record2 = varRecord[2]
                assert record2.name() == "var_record"

                assert record.value().find{ r ->
                    r.name() == "var_name" && r.value()[0] == "endWith"
                }
                assert record.value().find{ r ->
                    r.name() == "var_value" && r.value()[0] == "2019-04-20"
                }
                assert record.value().find{ r ->
                    r.name() == "var_title" && r.value()[0] == "endTime"
                }

                assert record1.value().find{ r ->
                    r.name() == "var_name" && r.value()[0] == "orgID"
                }
                assert record1.value().find{ r ->
                    r.name() == "var_value" && r.value()[0] == "ef325711521b11e6bbd8d017c2939671"
                }
                assert record1.value().find{ r ->
                    r.name() == "var_title" && r.value()[0] == "zky"
                }

                assert record2.value().find{ r ->
                    r.name() == "var_name" && r.value()[0] == "startWith"
                }
                assert record2.value().find{ r ->
                    r.name() == "var_value" && r.value()[0] == "2019-01-12"
                }
                assert record2.value().find{ r ->
                    r.name() == "var_title" && r.value()[0] == "startTime"
                }

                def dataTableList = langs.data_table
                assert dataTableList
                assert dataTableList.size() == 2

                def dt = dataTableList[0]
                assert dt
                assert dt.name() == "data_table"

                def dt1 = dataTableList[1]
                assert dt1
                assert dt1.name() == "data_table"

                assert dataTableList.find{ d ->
                    d.attributes().name == "createRecord" && d.attributes().seq_num == "2" && d.value()[0].name() == "record" && d.value()[0].value()[0].name() == "NAME" && d.value()[0].value()[0].value()[0] == "ef325711521b11e6bbd8d017c2939671" && d.value()[0].value()[1].name() == "COUNT" && d.value()[0].value()[1].value()[0] == "2"
                }
                assert dataTableList.find{ d ->
                    d.attributes().name == "dataCenter" && d.attributes().seq_num == "1" && d.value()[0].name() == "record" && d.value()[0].value()[0].name() == "orgid"  && d.value()[0].value()[0].value()[0] == "1" && d.value()[0].value()[1].name() == "orgName"  && d.value()[0].value()[1].value()[0] == "firstHospital" && d.value()[1].name() == "record" && d.value()[1].value()[0].name() == "orgid"  && d.value()[1].value()[0].value()[0] == "2" && d.value()[1].value()[1].name() == "orgName"  && d.value()[1].value()[1].value()[0] == "lastHospital"
                }
            }
        then: "结果"
            result == PactVerificationResult.Ok.INSTANCE
    }

    void "api获取指定报表的数据-契约测试1"() {
        given:
        def api = new PactBuilder()
        api{
            serviceConsumer "DATA-CENTER-SERVICE"
            hasPactWith "DataSrv"
            port 1234
        }
        api{
            given("获取数据")
            withAttributes(method: 'get', path: '/metrics',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    query: [startWith:'2019-01-12', endWith:'2019-04-20', dataType: 'dataCenter']
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body:[
                            [
                                    orgid:"1",
                                    orgName:"firstHospital"
                            ],
                            [
                                    orgid:"2",
                                    orgName:"lastHospital"
                            ]

                    ]
            )
        }

        when:"执行"
        def result = api.runTest { mockServer ->
            def dataApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .encoder(new GsonEncoder())
                    .target(IDataCenterService.class, mockServer.url as String)

            def reportId = null
            Report.withNewSession {
                Report report = Report.findByCode("0315")
                reportId = report.id
            }

            ReportViewParam reportParam = new ReportViewParam()
            reportParam.reportId = reportId

            ReportParamValue paramValue = new ReportParamValue()
            paramValue.name = "orgID"
            paramValue.value = "ef325711521b11e6bbd8d017c2939671"
            paramValue.title = "zky"

            ReportParamValue startWith = new ReportParamValue()
            startWith.name = "startWith"
            startWith.value = "2019-01-12"
            startWith.title = "startTime"

            ReportParamValue endWith = new ReportParamValue()
            endWith.name = "endWith"
            endWith.value = "2019-04-20"
            endWith.title = "endTime"
            List<ReportParamValue> paramValues = new ArrayList<>()
            paramValues.add(paramValue)
            paramValues.add(startWith)
            paramValues.add(endWith)
            reportParam.paramValues = paramValues

            DataFacadeService dataFacadeService = new DataFacadeService()
            dataFacadeService.dataCenterService = dataApi
            controller.dataFacadeService = dataFacadeService
            controller.getReportDataOld((reportParam as JSON) as String)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData

            def one = bizData.reportDataVO
            assert one
            def xml = one.xmlData
            assert xml

            Validator xmlValidate = XmlUtil.validateReport()
            assert xmlValidate.validate(new StreamSource(new ByteArrayInputStream(xml.bytes))) == null

            def langs = new XmlParser().parseText(xml)

            // 根节点
            def rootName = langs.name()
            assert rootName == "report"

            def varSet = langs.var_set
            assert varSet
            assert varSet.size() == 1

            def varRecord = varSet.var_record
            assert varRecord
            assert varRecord.size() == 3
            def record = varRecord[0]
            assert record.name() == "var_record"

            def record1 = varRecord[1]
            assert record1.name() == "var_record"

            def record2 = varRecord[2]
            assert record2.name() == "var_record"

            assert record.value().find{ r ->
                r.name() == "var_name" && r.value()[0] == "endWith"
            }
            assert record.value().find{ r ->
                r.name() == "var_value" && r.value()[0] == "2019-04-20"
            }
            assert record.value().find{ r ->
                r.name() == "var_title" && r.value()[0] == "endTime"
            }

            assert record1.value().find{ r ->
                r.name() == "var_name" && r.value()[0] == "orgID"
            }
            assert record1.value().find{ r ->
                r.name() == "var_value" && r.value()[0] == "ef325711521b11e6bbd8d017c2939671"
            }
            assert record1.value().find{ r ->
                r.name() == "var_title" && r.value()[0] == "zky"
            }

            assert record2.value().find{ r ->
                r.name() == "var_name" && r.value()[0] == "startWith"
            }
            assert record2.value().find{ r ->
                r.name() == "var_value" && r.value()[0] == "2019-01-12"
            }
            assert record2.value().find{ r ->
                r.name() == "var_title" && r.value()[0] == "startTime"
            }

            def dataTableList = langs.data_table
            assert dataTableList
            assert dataTableList.size() == 2

            def dt = dataTableList[0]
            assert dt
            assert dt.name() == "data_table"

            def dt1 = dataTableList[1]
            assert dt1
            assert dt1.name() == "data_table"

            assert dataTableList.find{ d ->
                d.attributes().name == "createRecord" && d.attributes().seq_num == "2" && d.value()[0].name() == "record" && d.value()[0].value()[0].name() == "NAME" && d.value()[0].value()[0].value()[0] == "ef325711521b11e6bbd8d017c2939671" && d.value()[0].value()[1].name() == "COUNT" && d.value()[0].value()[1].value()[0] == "2"
            }
            assert dataTableList.find{ d ->
                d.attributes().name == "dataCenter" && d.attributes().seq_num == "1" && d.value()[0].name() == "record" && d.value()[0].value()[0].name() == "orgid"  && d.value()[0].value()[0].value()[0] == "1" && d.value()[0].value()[1].name() == "orgName"  && d.value()[0].value()[1].value()[0] == "firstHospital" && d.value()[1].name() == "record" && d.value()[1].value()[0].name() == "orgid"  && d.value()[1].value()[0].value()[0] == "2" && d.value()[1].value()[1].name() == "orgName"  && d.value()[1].value()[1].value()[0] == "lastHospital"
            }
        }
        then: "结果"
        result == PactVerificationResult.Ok.INSTANCE
    }

    @Ignore
    void "获取指定大屏的所有数据表的结构-契约测试1"() {
        given:
        def api = new PactBuilder()
        api{
            serviceConsumer "DATA-CENTER-SERVICE"
            hasPactWith "DATA-CENTER-SERVICE"
            port 1234
        }
        api{
            given("获取数据")
            withAttributes(method: 'get', path: '/metrics',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    query: [startWith:'2019-01-12', endWith:'2019-04-20', dataType: 'dataCenter']
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body:[
                            [
                                    orgid:"1",
                                    orgName:"firstHospital"
                            ],
                            [
                                    orgid:"2",
                                    orgName:"lastHospital"
                            ]

                    ]
            )
        }

        when:"执行"
        def result = api.runTest { mockServer ->
            def dataApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .encoder(new GsonEncoder())
                    .target(IDataCenterService.class, mockServer.url as String)

            def reportId = null
            Report.withNewSession {
                Report report = Report.findByCode("0315")
                reportId = report.id
            }

            DataFacadeService dataFacadeService = new DataFacadeService()
            dataFacadeService.dataCenterService = dataApi
            controller.dataFacadeService = dataFacadeService
            controller.getScreenReportStruct(reportId)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"

            def one = jsonData.one
            assert one
            def xml = one.xmlData
            assert xml

            Validator xmlValidate = XmlUtil.validateScreen()
            assert xmlValidate.validate(new StreamSource(new ByteArrayInputStream(xml.bytes))) == null
        }
        then: "结果"
        result == PactVerificationResult.Ok.INSTANCE
    }

    @Ignore
    void "获取大屏指定表数据-契约测试1"() {
        given:
        def api = new PactBuilder()
        api{
            serviceConsumer "DATA-CENTER-SERVICE"
            hasPactWith "DATA-CENTER-SERVICE"
            port 1234
        }
        api{
            given("获取数据")
            withAttributes(method: 'get', path: '/metrics',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    query: [startWith:'2019-01-12', endWith:'2019-04-20', dataType: 'dataCenter']
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body:[
                            [
                                    orgid:"1",
                                    orgName:"firstHospital"
                            ],
                            [
                                    orgid:"2",
                                    orgName:"lastHospital"
                            ]

                    ]
            )
        }

        when:"执行"
        def result = api.runTest { mockServer ->
            def dataApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .encoder(new GsonEncoder())
                    .target(IDataCenterService.class, mockServer.url as String)

            def reportId = null
            Report.withNewSession {
                Report report = Report.findByCode("0315")
                reportId = report.id
            }

            ScreenReportParam param = new ScreenReportParam()
            List<String> tables = new ArrayList<>()
            tables.add("createRecord")
            param.reportId = reportId
            param.tableNames = tables

            DataFacadeService dataFacadeService = new DataFacadeService()
            dataFacadeService.dataCenterService = dataApi
            controller.dataFacadeService = dataFacadeService
            controller.getScreenReportData(param)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"

            def one = jsonData.one
            assert one
            def xml = one.xmlData
            assert xml

            Validator xmlValidate = XmlUtil.validateReport()
            assert xmlValidate.validate(new StreamSource(new ByteArrayInputStream(xml.bytes))) == null
        }
        then: "结果"
        result == PactVerificationResult.Ok.INSTANCE
    }
}

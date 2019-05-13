package com.zcareze.report

import au.com.dius.pact.consumer.PactVerificationResult
import au.com.dius.pact.consumer.groovy.PactBuilder
import com.report.service.IFileService
import feign.Feign
import feign.gson.GsonDecoder
import feign.httpclient.ApacheHttpClient
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.test.annotation.Rollback
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Rollback
//@Ignore // 忽略当前测试用例
class ReportControllerPactSpec extends Specification implements ControllerUnitTest<ReportController> {
    def setup(){
        Report.withNewSession {
            def group1 = new ReportGroups(code:"01", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表", color: "ffc100");
            def group2 = new ReportGroups(code:"02", name:"服务管理", comment:"有关服务工作开展情况和开展内容等信息的呈现", color: "7BAFA1");
            def group3 = new ReportGroups(code:"99", name:"监控大屏", comment:"内置专门存放监控大屏报表的分组", color: "b8e986");
            group1.save();
            group2.save();
            group3.save();

            def report = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: group1.code, runway: 2, editorName: "王", editorId: "1");
            def report1 = new Report(code: "TZYB", name: "医生团队月报", grpCode: group2.code, runway: 1, editorName: "王", editorId: "1");
            // 大屏
            def report2 = new Report(code: "CS", name: "测试大屏", grpCode: group3.code, runway: 1, editorName: "王", editorId: "1");
            def report3 = new Report(code: "ZS", name: "正式大屏", grpCode: group3.code, runway: 2, editorName: "王", editorId: "1");
            report.save()
            report1.save()
            report3.save()
            report2.save(flush: true)

            new ReportStyle(rpt: report, scene: 0, fileUrl: "123").save()

            Report.executeUpdate("update Report r set r.editTime=:editTime where r.code=:code", [editTime:Date.parse("yyyy-MM-dd HH:mm:ss", "2018-04-18 16:00:00"), code: 'JCYCD'])

            report = Report.findByCode('JCYCD');

            new ReportInputs(rpt: report1, name: "orgid", caption: "机构", seqNum: 0, dataType: "31", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构" ).save()
            new ReportStyle(rpt: report1, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt").save()
            new ReportTables(queryMode: 0,  rpt: report1, name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1).save()
            new ReportGrantTo(rpt: report1, orgId: "1", roles: "11", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report1, orgId: "2", roles: "03;04;11", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report1, orgId: "3", roles: "01;02;11", manage: 1, granter: "王").save()
            new ReportOpenTo(rpt: report1, orgTreeId: 1, orgTreeLayer: 1, roles: "01").save()

            new ReportUsually(staffId: "1", rpt: report1, seqNum: 3).save();
            new ReportUsually(staffId: "2", rpt: report1, seqNum: 2).save();

            def group = new ReportGroups(code:"06", name:"pc报表样式")
            group.save()
            def report4 = new Report(code: "0315", name:"建档情况分析表", runway: 1, grpCode: group.code)
            report4.save(flush: true)

            new ReportStyle(rpt: report4, scene:0, fileUrl:"asd",chart: "<chart name=\\\"chart1\\\" theme=\\\"walden\\\" tab=\\\"建档情况\\\"><type>bar</type><title>机构建档情况</title><x_col>机构名称</x_col><y_col><field>建档数</field><name>建档数</name></y_col></chart>").save()
            new ReportTables(queryMode: 0,  name:"建档情况", rpt: report4, seqNum: 1, sqlText: "select ol.`name` 机构名称, COUNT(rl.`id`) 建档数 FROM org_list ol INNER JOIN `resident_list` rl on rl.`org_id`= ol.`id` where ol.`id` = [orgID] and rl.`commit_time` BETWEEN '2019-03-01' and '2019-03-30' GROUP BY ol.`id`").save()
            new ReportInputs(caption: "机构", dataType: "31", defType: "我的机构", inputType: 3, name: "orgID", rpt: report4,
                    seqNum: 1, sqlText: "select ol.id col_value,ol.name col_title from org_list ol where ol.id=[my_org_id]",
            ).save()
            new ReportInputs(caption: "开始日期", dataType: "23", defType: "月初", inputType: 1, name: "startDate", rpt: report4,
                    seqNum: 2
            ).save()
            new ReportInputs(caption: "结束日期", dataType: "23", defType: "今天", inputType: 1, name: "endDate", rpt: report4,
                    seqNum: 3
            ).save()
        }
    }

    def cleanup() {
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

    void "获取文件真实访问路径-测试1"(){
        given:
        def api = new PactBuilder()
        api {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        api {
            given("获取文件授权访问路径")
            withAttributes(method: 'post', path: '/fileBizService/getReportFileVisitUrl',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body: "fileUrl=asd&accountId=1"
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode    : 0,
                            errmsg     : "成功",
                            error      : "SUCCESS",
                            visitUrlDTO: [
                                    ossUrl: "文件访问路径"
                            ]
                    ]
            )
        }
        when:
        def result = api.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            controller.getVisitFileUrl("asd")

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"

            def one = jsonData.one
            assert one == "文件访问路径"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }

    void "获取文件真实访问路径-测试2"(){
        given:
        def api = new PactBuilder()
        api {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        api {

        }
        when:
        def result = api.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            controller.getVisitFileUrl("")

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData
            assert jsonData.code == 3
            assert jsonData.message == "地址不能为空"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "获取报表信息-测试1"() {
        given:
            def api = new PactBuilder()
            api {
                serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
                hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
                port 1234                       // The port number for the service. It is optional, leave it out to use a random one
            }
            api {
                given("获取文件授权访问路径")
                withAttributes(method: 'post', path: '/fileBizService/getReportFileVisitUrl',
                        headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                        body: "fileUrl=asd&accountId=1"
                )
                willRespondWith(
                        status: 200,
                        headers: ['Content-Type': 'application/json'],
                        body: [
                                errcode    : 0,
                                errmsg     : "成功",
                                error      : "SUCCESS",
                                visitUrlDTO: [
                                        ossUrl: "文件访问路径"
                                ]
                        ]
                )
            }
        when:
        def result = api.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def reportId = null
            Report.withNewSession {
                Report report = Report.findByCode("0315")
                reportId = report.id
            }
            def scene = 0
            controller.getReportViewById(reportId, scene)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"

            def one = jsonData.one
            assert one
            assert one.id == reportId
            assert one.code == "0315"
            assert one.grpCode == "06"
            assert one.chart == "<chart name=\\\"chart1\\\" theme=\\\"walden\\\" tab=\\\"建档情况\\\"><type>bar</type><title>机构建档情况</title><x_col>机构名称</x_col><y_col><field>建档数</field><name>建档数</name></y_col></chart>"
            assert one.runway == 1
            assert one.name == "建档情况分析表"
            assert one.fileUrl == "文件访问路径"

            def list = one.inputParams
            assert list?.size() == 1
            def param = list.get(0)
            assert param.name == "orgID"
            assert param.caption == "机构"
            assert param.dataType == "31"
            assert param.inputType == 3
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "获取报表信息-测试2"() {
        given:
        def api = new PactBuilder()
        api {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        api {

        }
        when:
            def result = api.runTest { mockServer ->
                def fileApi = Feign.builder()
                        .client(new ApacheHttpClient())
                        .decoder(new GsonDecoder())
                        .target(IFileService.class, mockServer.url as String)

                controller.fileService = fileApi

                def reportId = null
                def scene = 0
                controller.getReportViewById(reportId, scene)

                assert response.status == 200
                assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

                def jsonData = response.json
                assert jsonData
                assert jsonData.code == 3
                assert jsonData.message == "标识不能为空"
            }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }

    void "获取报表信息-测试3"() {
        given:
        def api = new PactBuilder()
        api {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        api {

        }
        when:
        def result = api.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def reportId = null
            Report.withNewSession {
                Report report = Report.findByCode("JCYCD")
                reportId = report.id
            }
            def scene = 0
            controller.getReportViewById(reportId, scene)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData
            assert jsonData.code == 3
            assert jsonData.message == "数据为空"
        }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }

    void "api获取报表信息-测试1"() {
        given:
        def api = new PactBuilder()
        api {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        api {
            given("获取文件授权访问路径")
            withAttributes(method: 'post', path: '/fileBizService/getReportFileVisitUrl',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body: "fileUrl=asd&accountId=1"
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode    : 0,
                            errmsg     : "成功",
                            error      : "SUCCESS",
                            visitUrlDTO: [
                                    ossUrl: "文件访问路径"
                            ]
                    ]
            )
        }
        when:
        def result = api.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def reportId = null
            Report.withNewSession {
                Report report = Report.findByCode("0315")
                reportId = report.id
            }
            def scene = 0
            controller.getReportViewByIdOld(reportId, scene)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData
            def one = bizData.reportViewVOs
            assert one
            assert one.id == reportId
            assert one.code == "0315"
            assert one.grpCode == "06"
            assert one.chart == "<chart name=\\\"chart1\\\" theme=\\\"walden\\\" tab=\\\"建档情况\\\"><type>bar</type><title>机构建档情况</title><x_col>机构名称</x_col><y_col><field>建档数</field><name>建档数</name></y_col></chart>"
            assert one.runway == 1
            assert one.name == "建档情况分析表"
            assert one.fileUrl == "文件访问路径"

            def list = one.inputParams
            assert list?.size() == 1
            def param = list.get(0)
            assert param.name == "orgID"
            assert param.caption == "机构"
            assert param.dataType == "31"
            assert param.inputType == 3
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "api获取报表信息-测试2"() {
        given:
        def api = new PactBuilder()
        api {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        api {

        }
        when:
        def result = api.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def reportId = null
            def scene = 0
            controller.getReportViewByIdOld(reportId, scene)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData
            assert jsonData.errcode == 3
            assert jsonData.errmsg == "报表标识不能为空"
        }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }

    void "api获取报表信息-测试3"() {
        given:
        def api = new PactBuilder()
        api {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        api {

        }
        when:
        def result = api.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def reportId = null
            Report.withNewSession {
                Report report = Report.findByCode("JCYCD")
                reportId = report.id
            }
            def scene = 0
            controller.getReportViewByIdOld(reportId, scene)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData
            assert jsonData.errcode == 3
            assert jsonData.errmsg == "数据为空"
        }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
}

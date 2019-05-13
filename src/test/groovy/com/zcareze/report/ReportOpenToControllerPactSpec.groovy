package com.zcareze.report

import au.com.dius.pact.consumer.PactVerificationResult
import au.com.dius.pact.consumer.groovy.PactBuilder
import com.report.service.IOrgListService
import feign.Feign
import feign.gson.GsonDecoder
import feign.httpclient.ApacheHttpClient
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.web.json.JSONElement
import spock.lang.Specification

@Integration
@Rollback
//@Ignore
class ReportOpenToControllerPactSpec extends Specification implements ControllerUnitTest<ReportOpenToController> {
    def setup() {
        def group1 = new ReportGroups(code:"01", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表", color: "ffc100");
        group1.save();
        
        // 报表
        def report5 = new Report(code: "YPFX", name: "药品分析", grpCode: group1.code, runway: 1, editorName: "王", editorId: "1")
        report5.save(flush:true)

        new ReportOpenTo(rpt: report5, orgTreeId: "1", orgTreeLayer: 0, roles: "02;11", granter: "王").save()
        new ReportOpenTo(rpt: report5, orgTreeId: null, orgTreeLayer: 1, roles: "01", granter: "王").save()
        new ReportOpenTo(rpt: report5, orgTreeId: "3", orgTreeLayer: 0, roles: "02;03", granter: "王").save()
    }
    def cleanup() {
        ReportUsually.executeUpdate("delete ReportUsually")
        ReportStyle.executeUpdate("delete ReportStyle")
        ReportGrantTo.executeUpdate("delete ReportGrantTo")
        ReportOpenTo.executeUpdate("delete ReportOpenTo")
        ReportInputs.executeUpdate("delete ReportInputs")
        ReportGroups.executeUpdate("delete ReportGroups")
        Report.executeUpdate("delete Report")
    }
    void "获取指定报表的开放记录-测试1"() {
        given:"参数"
        def orgList = new PactBuilder() // Create a new PactBuilder
        orgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        orgList{
            given("查询指定组织的所有上级组织名称")
            uponReceiving("b") // 描述

            withAttributes(method: 'post', path: '/orgService/getOrgTreeList',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body: "jsonData=\"{\"isUpOrDown\":true,\"list\":[1,3]}\"")// 指定输入参数-接口需要添加@Body注解，写明参数格式
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS",
                            map: [
                                    1: [
                                            [
                                                    orgName: "众康云医院",
                                                    orgId: "1"
                                            ]
                                    ],
                                    3: [
                                            [
                                                    orgName: "众康云医院",
                                                    orgId: "1"
                                            ],
                                            [
                                                    orgName: "心血管科",
                                                    orgId: "2"
                                            ],
                                            [
                                                    orgName: "赵志王心血管组",
                                                    orgId: "3"
                                            ]
                                    ]
                            ],
                            success: true
                    ]
            )
        }

        when:"执行"
        def result = orgList.runTest { mockServer ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            controller.orgListService = orgListApi

            Report report = Report.findByCode("YPFX")
            def reportId = report.id
            controller.getReportOpenToList(reportId)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 3

            assert list.any{ openTo ->
                openTo.rptId == reportId && openTo.orgTreeId == "1" && openTo.orgTreeName == "众康云医院" && openTo.orgTreeLayer == 0 && openTo.roles == "02;11"
            }
            assert list.any{ openTo ->
                openTo.rptId == reportId && openTo.orgTreeId == "3" && openTo.orgTreeName == "众康云医院>心血管科>赵志王心血管组" && openTo.orgTreeLayer == 0 && openTo.roles == "02;03"
            }
            assert list.any{ openTo ->
                openTo.rptId == reportId && openTo.orgTreeId == null && openTo.orgTreeName == "" && openTo.roles == "01" && openTo.orgTreeLayer == 1
            }
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取指定报表的开放记录-测试2"() {
        given:"参数"
        def orgList = new PactBuilder() // Create a new PactBuilder
        orgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        orgList{
            given("查询指定组织的所有上级组织名称")
            uponReceiving("b") // 描述

            withAttributes(method: 'post', path: '/orgService/getOrgTreeList',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body: "jsonData=\"{\"isUpOrDown\":true,\"list\":[1,3]}\"")// 指定输入参数-接口需要添加@Body注解，写明参数格式
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 1,
                            errmsg: "失败",
                            error: "ERROR",
                    ]
            )
        }

        when:"执行"
        def result = orgList.runTest { mockServer ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            controller.orgListService = orgListApi

            Report report = Report.findByCode("YPFX")
            def reportId = report.id
            controller.getReportOpenToList(reportId)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 3
            assert jsonData.message == "组织机构名称获取失败"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取指定报表的开放记录-测试3"() {
        given:"参数"
        def orgList = new PactBuilder() // Create a new PactBuilder
        orgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        when:"执行"
        def result = orgList.runTest { mockServer ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            controller.orgListService = orgListApi

            def reportId = null
            controller.getReportOpenToList(reportId)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 3
            assert jsonData.message == "报表标识不能为空"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取指定报表的开放记录-测试4"() {
        given:"参数"
        def orgList = new PactBuilder() // Create a new PactBuilder
        orgList { // 名称必须定义
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        when:"执行"
        def result = orgList.runTest { mockServer ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            controller.orgListService = orgListApi

            def reportId = "2"
            controller.getReportOpenToList(reportId)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"

            def list = jsonData.list
            assert list?.size() == 0
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }
}

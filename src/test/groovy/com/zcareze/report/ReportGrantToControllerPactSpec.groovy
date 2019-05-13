package com.zcareze.report

import au.com.dius.pact.consumer.PactVerificationResult
import au.com.dius.pact.consumer.groovy.PactBuilder
import com.report.result.BaseResult
import com.report.service.IOrgListService
import com.report.vo.ReportListVO
import com.report.vo.ReportUsuallyVO
import feign.Feign
import feign.gson.GsonDecoder
import feign.httpclient.ApacheHttpClient
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.web.json.JSONElement
import org.springframework.test.annotation.Rollback
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Rollback
//@Ignore // 忽略当前测试用例
class ReportGrantToControllerPactSpec extends Specification implements ControllerUnitTest<ReportGrantToController> {
    // 初始化数据
    def setupSpec() {

    }

    // 每次测试生成一次，互不干扰
    def setup() {
        ReportGroups.withNewSession {
            def group1 = new ReportGroups(code:"01", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表", color: "ffc100");
            def group2 = new ReportGroups(code:"02", name:"服务管理", comment:"有关服务工作开展情况和开展内容等信息的呈现", color: "7BAFA1");
            def group3 = new ReportGroups(code:"99", name:"监控大屏", comment:"内置专门存放监控大屏报表的分组", color: "b8e986");
            group1.save();
            group2.save();
            group3.save();

            // 报表
            def report1 = new Report(code: "KZRZB", name: "科主任周报", grpCode: group1.code, runway: 1, editorName: "王", editorId: "1",cloudId:"1");
            def report2 = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: group2.code, runway: 2, editorName: "王", editorId: "1",cloudId:"2");
            def report3 = new Report(code: "TZYB", name: "医生团队月报", grpCode: group1.code, runway: 1, editorName: "王", editorId: "1",cloudId:"1");
            def report4 = new Report(code: "YYYCD", name: "用药依从性统计", grpCode: group2.code, runway: 1, editorName: "王", editorId: "1",cloudId:"2");
            def report5 = new Report(code: "YPFX", name: "药品分析", grpCode: group1.code, runway: 1, editorName: "王", editorId: "1",cloudId:"1")

            report1.save()
            report2.save()
            report3.save()
            report4.save()
            report5.save(flush:true)

            Report.executeUpdate("update Report r set r.editTime=:editTime where r.code=:code", [editTime:new Date()-1, code: 'JCYCD'])

            report2 = Report.findByCode('JCYCD');

            new ReportInputs(rpt: report1, name: "orgid", caption: "机构", seqNum: 0, dataType: "31", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构" ).save()
            new ReportStyle(rpt: report1, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt").save()
            new ReportGrantTo(rpt: report1, orgId: "1", roles: "11", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report1, orgId: "2", roles: "03;04;11", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report1, orgId: "3", roles: "01;02;11", manage: 1, granter: "王").save()
            new ReportOpenTo(rpt: report1, orgTreeId: 1, orgTreeLayer: 1, roles: "01").save()

            //report2.save();
            new ReportInputs(rpt: report2, name: "endtime", caption: "结束时间", seqNum: 3, dataType: "23", inputType: 1, defType: "今天" ).save()
            new ReportInputs(rpt: report2, name: "month", caption: "月份", seqNum: 0, dataType: "12", inputType: 2, optionList: "1;2;3;4;5;6;7;8;9;10;11;12", defValue: "12" ).save()
            new ReportInputs(rpt: report2, name: "name", caption: "医生", seqNum: 2, dataType: "11", inputType: 1).save()
            new ReportInputs(rpt: report2, name: "year", caption: "开始时间", seqNum: 1, dataType: "21", inputType: 1, defType: "本年").save()
            new ReportStyle(rpt: report2, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt").save()
            new ReportStyle(rpt: report2, scene: 1, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/6a04cb4e3ed9420aa1f1c881650325ff.xslt").save()
            new ReportGrantTo(rpt: report2, orgId: "4", roles: "01;02;03;06;11", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report2, orgId: "2", roles: "01;11", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report2, orgId: "3", roles: "11;02", manage: 0, granter: "王").save()

            //report3.save();
            new ReportStyle(rpt: report3, scene: 2, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/3913c97922e4485d93fd5a4f7e05bd65/0e8f634358764214a508c77abafcd55a.xslt").save()
            new ReportGrantTo(rpt: report3, orgId: "3", roles: "01;02", manage: 0, granter: "王").save()
            new ReportGrantTo(rpt: report3, orgId: "1", roles: "02", manage: 0, granter: "王").save()

            //report4.save();
            new ReportStyle(rpt: report4, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/608639880900448ead762ca2246659ce/464ff931e29645088c3fbd325bed5227.xslt").save()
            new ReportGrantTo(rpt: report4, orgId: "1", roles: "01;02", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report4, orgId: "2", roles: "02", manage: 0, granter: "王").save()
            new ReportOpenTo(rpt: report4, orgTreeId: "1", orgTreeLayer: 0, roles: "02", granter: "王").save()

            // 个人常用报表
            new ReportUsually(staffId: "1", rpt: report1, seqNum: 1, cloudId: "1").save()
            //new ReportUsually(staffId: "1", rpt: report2, seqNum: 3).save();
            new ReportUsually(staffId: "1", rpt: report3, seqNum: 2, cloudId: "2").save();

            new ReportUsually(staffId: "2", rpt: report1, seqNum: 1, cloudId: "1").save();


            // 大屏
            def screenReport1 = new Report(code: "01", name: "众康云测试大屏", grpCode: group3.code, runway: 1, editorName: "王", editorId: "1", comment: "测试说明", cloudId: "1");
            def screenReport2 = new Report(code: "02", name: "众康云正式大屏", grpCode: group3.code, runway: 1, editorName: "王", editorId: "1", comment: "正式说明", cloudId: "2");
            def screenReport3 = new Report(code: "03", name: "众康云大屏", grpCode: group3.code, runway: 1, editorName: "王", editorId: "1", comment: "说明", cloudId: "1");
            screenReport1.save()
            screenReport2.save()
            screenReport3.save()

            new ReportStyle(rpt: screenReport1, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/3913c97922e4485d93fd5a4f7e05bd65/0e8f634358764214a508c77abafcd55a.xslt").save()
            new ReportGrantTo(rpt: screenReport1, orgId: "1", roles: "11", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: screenReport1, orgId: "2", roles: "03;04;11", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: screenReport1, orgId: "3", roles: "01;02;11", manage: 1, granter: "王").save()
            new ReportOpenTo(rpt: screenReport1, orgTreeId: 1, orgTreeLayer: 1, roles: "01").save()

            new ReportStyle(rpt: screenReport2, scene: 2, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/3913c97922e4485d93fd5a4f7e05bd65/0e8f634358764214a508c77abafcd55a.xslt").save()
            new ReportGrantTo(rpt: screenReport2, orgId: "4", roles: "01;02;03;06;11", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: screenReport2, orgId: "2", roles: "01;11", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: screenReport2, orgId: "3", roles: "11;02", manage: 0, granter: "王").save()

            new ReportStyle(rpt: screenReport3, scene: 2, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/3913c97922e4485d93fd5a4f7e05bd65/0e8f634358764214a508c77abafcd55a.xslt").save()
            new ReportGrantTo(rpt: screenReport3, orgId: "3", roles: "01;02", manage: 0, granter: "王").save()
            new ReportGrantTo(rpt: screenReport3, orgId: "1", roles: "02", manage: 0, granter: "王").save()
        }

    }

    def cleanup() {
        Report.withNewSession {
            ReportUsually.executeUpdate("delete ReportUsually")
            ReportStyle.executeUpdate("delete ReportStyle")
            ReportGrantTo.executeUpdate("delete ReportGrantTo")
            ReportOpenTo.executeUpdate("delete ReportOpenTo")
            ReportInputs.executeUpdate("delete ReportInputs")
            ReportGroups.executeUpdate("delete ReportGroups")
            Report.executeUpdate("delete Report")
        }
    }

    void "获取我有权限的报表-测试1"() {
        given:
            def staffOrgList = new PactBuilder() // Create a new PactBuilder
            staffOrgList {
                serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
                    hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
                    port 1234                       // The port number for the service. It is optional, leave it out to use a random one
            }
            staffOrgList{
                given("获取职员的所属组织列表")
                uponReceiving("a")// 描述
                withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
                willRespondWith(
                        status: 200,
                        headers: ['Content-Type': 'application/json'],
                        //body: [provider:"sampleApi2@serviceSet2",dependency:dependency]
                        body: [
                                errcode: null,
                                errmsg: null,
                                error: "SUCCESS",
                                orgList: [[
                                                  classes: "07",
                                                  orgId: "3" ], [
                                                  classes: "01;04;02;11;03",
                                                  orgId: "4"
                                          ]],
                                success: true
                        ]
                )
                given("查询组织和层级对应的职员的组织列表")
                uponReceiving("c")
                withAttributes(method: 'post', path: '/orgService/getOrgListByOrgTreeAndLayer')
                willRespondWith(
                        status: 200,
                        headers: ['Content-Type': 'application/json'],
                        body: [
                                errcode: 0,
                                errmsg: "成功",
                                error: "SUCCESS",
                                map: [
                                        "1-1": [[
                                                        classes: "04;02;11;03;01",
                                                        orgId: "2"
                                                ]]
                                ],
                                success: true
                        ]
                )
            }
        when:
            def orgResult = staffOrgList.runTest { mockServer ->

                def orgListApi = Feign.builder()
                        .client(new ApacheHttpClient())
                        .decoder(new GsonDecoder())
                        .target(IOrgListService.class, mockServer.url as String)

                def cloudId = "1"
                def scene = 0
                def isCloudManage = false
                controller.isCloudManage = isCloudManage
                controller.cloudId = cloudId
                controller.orgListService = orgListApi
                controller.getMyGrantToReports(scene, 0, 20)
                assert response.status == 200
                assert response.header("Content-Type") == "application/json;charset=UTF-8"
                def jsonData = response.json
                assert jsonData instanceof JSONElement
                assert jsonData.code == 1

                def list = jsonData.list
                assert list?.size() == 1

                ReportUsuallyVO reportUsuallyVO0 = list.get(0)
                assert reportUsuallyVO0.reportName == "科主任周报"
                assert reportUsuallyVO0.groupCode == "01"
                assert reportUsuallyVO0.seqNum == 1
                assert reportUsuallyVO0.usually == true
                assert reportUsuallyVO0.noRead == null
                assert reportUsuallyVO0.groupName == "运营简报"
            }
        then :
            orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取我有权限的报表-测试1-1"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{
            given("获取职员的所属组织列表")
            uponReceiving("a")// 描述
            withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    //body: [provider:"sampleApi2@serviceSet2",dependency:dependency]
                    body: [
                            errcode: null,
                            errmsg: null,
                            error: "SUCCESS",
                            orgList: [[
                                              classes: "07",
                                              orgId: "3" ], [
                                              classes: "01;04;02;11;03",
                                              orgId: "4"
                                      ]],
                            success: true
                    ]
            )
            given("查询组织和层级对应的职员的组织列表")
            uponReceiving("c")
            withAttributes(method: 'post', path: '/orgService/getOrgListByOrgTreeAndLayer')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS",
                            map: [
                                    "1-1": [[
                                                    classes: "04;02;11;03;01",
                                                    orgId: "2"
                                            ]]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def cloudId = "2"
            def scene = 0
            def isCloudManage = false
            controller.isCloudManage = isCloudManage
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyGrantToReports(scene, 0, 20)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 1

            ReportUsuallyVO reportUsuallyVO0 = list.get(0)
            assert reportUsuallyVO0.reportName == "监测依从度统计"
            assert reportUsuallyVO0.groupCode == "02"
            assert reportUsuallyVO0.seqNum == 1
            assert reportUsuallyVO0.usually == false
            assert reportUsuallyVO0.noRead == null
            assert reportUsuallyVO0.groupName == "服务管理"
        }
        then :
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取我有权限的报表-测试2"() {
        given:
            def staffOrgList = new PactBuilder() // Create a new PactBuilder
            staffOrgList {
                serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
                hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
                port 1234                       // The port number for the service. It is optional, leave it out to use a random one
            }
            staffOrgList{
                given("获取职员的所属组织列表")
                uponReceiving("a")// 描述
                withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
                willRespondWith(
                        status: 200,
                        headers: ['Content-Type': 'application/json'],
                        //body: [provider:"sampleApi2@serviceSet2",dependency:dependency]
                        body: [
                                errcode: null,
                                errmsg: null,
                                error: "SUCCESS",
                                orgList: [[
                                                  classes: "07",
                                                  orgId: "3" ], [
                                                  classes: "01;04;02;11;03",
                                                  orgId: "4"
                                          ]],
                                success: true
                        ]
                )

                given("查询组织和层级对应的职员的组织列表")
                uponReceiving("c")
                withAttributes(method: 'post', path: '/orgService/getOrgListByOrgTreeAndLayer')
                willRespondWith(
                        status: 200,
                        headers: ['Content-Type': 'application/json'],
                        body: [
                                errcode: 0,
                                errmsg: "成功",
                                error: "SUCCESS",
                                map: [
                                        "1-1": [[
                                                        classes: "04;02;11;03;01",
                                                        orgId: "2"
                                                ]]
                                ],
                                success: true
                        ]
                )
            }
        when:
            def orgResult = staffOrgList.runTest { mockServer ->

                def orgListApi = Feign.builder()
                        .client(new ApacheHttpClient())
                        .decoder(new GsonDecoder())
                        .target(IOrgListService.class, mockServer.url as String)

                def cloudId = "2"
                def scene = 1
                def isCloudManage = false
                controller.isCloudManage = isCloudManage
                controller.cloudId = cloudId
                controller.orgListService = orgListApi
                controller.getMyGrantToReports(scene, 0, 20)
                assert response.status == 200
                assert response.header("Content-Type") == "application/json;charset=UTF-8"
                def jsonData = response.json
                assert jsonData instanceof JSONElement
                assert jsonData.code == 1

                def list = jsonData.list
                assert list?.size() == 1

                ReportUsuallyVO reportUsuallyVO0 = list.get(0)
                assert reportUsuallyVO0.reportName == "监测依从度统计"
                assert reportUsuallyVO0.groupCode == "02"
                assert reportUsuallyVO0.seqNum == 1
                assert reportUsuallyVO0.usually == false
                assert reportUsuallyVO0.noRead == null
                assert reportUsuallyVO0.groupName == "服务管理"
            }
        then :
            orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取我有权限的报表-测试2-1"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{

        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def cloudId = "1"
            def scene = 1
            def isCloudManage = false
            controller.isCloudManage = isCloudManage
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyGrantToReports(scene, 0, 20)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 0
        }
        then :
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取我有权限的报表-测试3"() {
        given:
            def staffOrgList = new PactBuilder() // Create a new PactBuilder
            staffOrgList {
                serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
                hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
                port 1234                       // The port number for the service. It is optional, leave it out to use a random one
            }
            staffOrgList{
                given("获取职员的所属组织列表")
                uponReceiving("a")// 描述
                withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
                willRespondWith(
                        status: 200,
                        headers: ['Content-Type': 'application/json'],
                        //body: [provider:"sampleApi2@serviceSet2",dependency:dependency]
                        body: [
                                errcode: null,
                                errmsg: null,
                                error: "SUCCESS",
                                orgList: [[
                                                  classes: "07",
                                                  orgId: "3" ], [
                                                  classes: "01;04;02;11;03",
                                                  orgId: "4"
                                          ]],
                                success: true
                        ]
                )

                given("查询组织和层级对应的职员的组织列表")
                uponReceiving("c")
                withAttributes(method: 'post', path: '/orgService/getOrgListByOrgTreeAndLayer')
                willRespondWith(
                        status: 200,
                        headers: ['Content-Type': 'application/json'],
                        body: [
                                errcode: 0,
                                errmsg: "成功",
                                error: "SUCCESS",
                                map: [
                                        "1-1": [[
                                                        classes: "04;02;11;03;01",
                                                        orgId: "2"
                                                ]]
                                ],
                                success: true
                        ]
                )
            }
        when:
            def orgResult = staffOrgList.runTest { mockServer ->

                def orgListApi = Feign.builder()
                        .client(new ApacheHttpClient())
                        .decoder(new GsonDecoder())
                        .target(IOrgListService.class, mockServer.url as String)

                def cloudId = "1"
                def isCloudManage = false
                controller.isCloudManage = isCloudManage
                controller.cloudId = cloudId
                controller.orgListService = orgListApi
                controller.getMyGrantToReports(2, 0, 20)
                assert response.status == 200
                assert response.header("Content-Type") == "application/json;charset=UTF-8"
                def jsonData = response.json
                assert jsonData instanceof JSONElement
                assert jsonData.code == 1

                def list = jsonData.list
                assert list?.size() == 0
            }
        then :
            orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取我有权限的报表-测试4"() {
        given:
            def staffOrgList = new PactBuilder() // Create a new PactBuilder
            staffOrgList {
                serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
                hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
                port 1234                       // The port number for the service. It is optional, leave it out to use a random one
            }
            staffOrgList{

            }
        when:
            def orgResult = staffOrgList.runTest { mockServer ->

                def orgListApi = Feign.builder()
                        .client(new ApacheHttpClient())
                        .decoder(new GsonDecoder())
                        .target(IOrgListService.class, mockServer.url as String)

                def cloudId = "1"
                def isCloudManage = false
                controller.isCloudManage = isCloudManage
                controller.cloudId = cloudId
                controller.orgListService = orgListApi
                controller.getMyGrantToReports(3, 0, 20)
                assert response.status == 200
                assert response.header("Content-Type") == "application/json;charset=UTF-8"
                def jsonData = response.json
                assert jsonData instanceof JSONElement
                assert jsonData.code == 1

                def list = jsonData.list
                assert list?.size() == 0
            }
        then :
            orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取我有权限的报表-测试5"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{

        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def cloudId = "1"
            def isCloudMange = true
            controller.isCloudManage = isCloudMange
            controller.cloudId = cloudId
            controller.orgListService = orgListApi

            controller.getMyGrantToReports(0, 0, 20)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 1

            ReportUsuallyVO reportUsuallyVO1 = list.get(0)
            assert reportUsuallyVO1.reportName == "科主任周报"
            assert reportUsuallyVO1.groupCode == "01"
            assert reportUsuallyVO1.seqNum == 1
            assert reportUsuallyVO1.usually == true
            assert reportUsuallyVO1.noRead == null
            assert reportUsuallyVO1.groupName == "运营简报"
        }
        then :
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "api获取我有权限的报表-测试1"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{
            given("获取职员的所属组织列表")
            uponReceiving("a")// 描述
            withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    //body: [provider:"sampleApi2@serviceSet2",dependency:dependency]
                    body: [
                            errcode: null,
                            errmsg: null,
                            error: "SUCCESS",
                            orgList: [[
                                              classes: "07",
                                              orgId: "3" ], [
                                              classes: "01;04;02;11;03",
                                              orgId: "4"
                                      ]],
                            success: true
                    ]
            )
            given("查询组织和层级对应的职员的组织列表")
            uponReceiving("c")
            withAttributes(method: 'post', path: '/orgService/getOrgListByOrgTreeAndLayer')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS",
                            map: [
                                    "1-1": [[
                                                    classes: "04;02;11;03;01",
                                                    orgId: "2"
                                            ]]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def cloudId = "1"
            def scene = 0
            def isCloudManage = false
            controller.isCloudManage = isCloudManage
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyGrantToReportOld(scene)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData
            def list = bizData.reportList
            assert list?.size() == 1

            ReportUsuallyVO reportUsuallyVO1 = list.get(0)
            assert reportUsuallyVO1.reportName == "科主任周报"
            assert reportUsuallyVO1.groupCode == "01"
            assert reportUsuallyVO1.seqNum == 1
            assert reportUsuallyVO1.usually == true
            assert reportUsuallyVO1.noRead == null
            assert reportUsuallyVO1.groupName == "运营简报"
        }
        then :
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "api获取我有权限的报表-测试1-1"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{
            given("获取职员的所属组织列表")
            uponReceiving("a")// 描述
            withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    //body: [provider:"sampleApi2@serviceSet2",dependency:dependency]
                    body: [
                            errcode: null,
                            errmsg: null,
                            error: "SUCCESS",
                            orgList: [[
                                              classes: "07",
                                              orgId: "3" ], [
                                              classes: "01;04;02;11;03",
                                              orgId: "4"
                                      ]],
                            success: true
                    ]
            )
            given("查询组织和层级对应的职员的组织列表")
            uponReceiving("c")
            withAttributes(method: 'post', path: '/orgService/getOrgListByOrgTreeAndLayer')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS",
                            map: [
                                    "1-1": [[
                                                    classes: "04;02;11;03;01",
                                                    orgId: "2"
                                            ]]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def cloudId = "2"
            def scene = 0
            def isCloudManage = false
            controller.isCloudManage = isCloudManage
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyGrantToReportOld(scene)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData
            def list = bizData.reportList
            assert list?.size() == 1

            ReportUsuallyVO reportUsuallyVO0 = list.get(0)
            assert reportUsuallyVO0.reportName == "监测依从度统计"
            assert reportUsuallyVO0.groupCode == "02"
            assert reportUsuallyVO0.seqNum == 1
            assert reportUsuallyVO0.usually == false
            assert reportUsuallyVO0.noRead == null
            assert reportUsuallyVO0.groupName == "服务管理"
        }
        then :
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "api获取我有权限的报表-测试2"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{
            given("获取职员的所属组织列表")
            uponReceiving("a")// 描述
            withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    //body: [provider:"sampleApi2@serviceSet2",dependency:dependency]
                    body: [
                            errcode: null,
                            errmsg: null,
                            error: "SUCCESS",
                            orgList: [[
                                              classes: "07",
                                              orgId: "3" ], [
                                              classes: "01;04;02;11;03",
                                              orgId: "4"
                                      ]],
                            success: true
                    ]
            )

            given("查询组织和层级对应的职员的组织列表")
            uponReceiving("c")
            withAttributes(method: 'post', path: '/orgService/getOrgListByOrgTreeAndLayer')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS",
                            map: [
                                    "1-1": [[
                                                    classes: "04;02;11;03;01",
                                                    orgId: "2"
                                            ]]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def cloudId = "2"
            def scene = 1
            def isCloudManage = false
            controller.isCloudManage = isCloudManage
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyGrantToReportOld(scene)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData
            def list = bizData.reportList
            assert list?.size() == 1

            ReportUsuallyVO reportUsuallyVO0 = list.get(0)
            assert reportUsuallyVO0.reportName == "监测依从度统计"
            assert reportUsuallyVO0.groupCode == "02"
            assert reportUsuallyVO0.seqNum == 1
            assert reportUsuallyVO0.usually == false
            assert reportUsuallyVO0.noRead == null
            assert reportUsuallyVO0.groupName == "服务管理"
        }
        then :
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "api获取我有权限的报表-测试2-1"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{
        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def cloudId = "1"
            def scene = 1
            def isCloudManage = false
            controller.isCloudManage = isCloudManage
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyGrantToReportOld(scene)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData
            def list = bizData.reportList
            assert list?.size() == 0
        }
        then :
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "api获取我有权限的报表-测试3"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{
            given("获取职员的所属组织列表")
            uponReceiving("a")// 描述
            withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    //body: [provider:"sampleApi2@serviceSet2",dependency:dependency]
                    body: [
                            errcode: null,
                            errmsg: null,
                            error: "SUCCESS",
                            orgList: [[
                                              classes: "07",
                                              orgId: "3" ], [
                                              classes: "01;04;02;11;03",
                                              orgId: "4"
                                      ]],
                            success: true
                    ]
            )

            given("查询组织和层级对应的职员的组织列表")
            uponReceiving("c")
            withAttributes(method: 'post', path: '/orgService/getOrgListByOrgTreeAndLayer')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS",
                            map: [
                                    "1-1": [[
                                                    classes: "04;02;11;03;01",
                                                    orgId: "2"
                                            ]]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def cloudId = "1"
            def isCloudManage = false
            controller.cloudId = cloudId
            controller.isCloudManage = isCloudManage
            controller.orgListService = orgListApi
            controller.getMyGrantToReportOld(2)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData
            def list = bizData.reportList
            assert list?.size() == 0
        }
        then :
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "api获取我有权限的报表-测试4"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{

        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def cloudId = "1"
            def isCloudManage = false
            controller.cloudId = cloudId
            controller.isCloudManage = isCloudManage
            controller.orgListService = orgListApi
            controller.getMyGrantToReportOld(3)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData
            def list = bizData.reportList
            assert list?.size() == 0
        }
        then :
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "api获取我有权限的报表-测试5"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{

        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def cloudId = "1"
            def isCloudMange = true
            controller.isCloudManage = isCloudMange
            controller.cloudId = cloudId
            controller.orgListService = orgListApi

            controller.getMyGrantToReportOld(0)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData
            def list = bizData.reportList
            assert list?.size() == 1

            ReportUsuallyVO reportUsuallyVO1 = list.get(0)
            assert reportUsuallyVO1.reportName == "科主任周报"
            assert reportUsuallyVO1.groupCode == "01"
            assert reportUsuallyVO1.seqNum == 1
            assert reportUsuallyVO1.usually == true
            assert reportUsuallyVO1.noRead == null
            assert reportUsuallyVO1.groupName == "运营简报"
        }
        then :
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取我有权限的监控大屏报表清单-测试1"() {
        given:
            def staffOrgList = new PactBuilder() // Create a new PactBuilder
            staffOrgList {
                serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
                hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
                port 1234                       // The port number for the service. It is optional, leave it out to use a random one
            }
            staffOrgList{
                given("获取职员的所属组织列表")
                uponReceiving("a")// 描述
                withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
                willRespondWith(
                        status: 200,
                        headers: ['Content-Type': 'application/json'],
                        //body: [provider:"sampleApi2@serviceSet2",dependency:dependency]
                        body: [
                                errcode: null,
                                errmsg: null,
                                error: "SUCCESS",
                                orgList: [[
                                                  classes: "07",
                                                  orgId: "3" ], [
                                                  classes: "01;04;02;11;03",
                                                  orgId: "4"
                                          ]],
                                success: true
                        ]
                )

                given("查询组织和层级对应的职员的组织列表")
                uponReceiving("c")
                withAttributes(method: 'post', path: '/orgService/getOrgListByOrgTreeAndLayer')
                willRespondWith(
                        status: 200,
                        headers: ['Content-Type': 'application/json'],
                        body: [
                                errcode: 0,
                                errmsg: "成功",
                                error: "SUCCESS",
                                map: [
                                        "1-1": [[
                                                        classes: "04;02;11;03;01",
                                                        orgId: "2"
                                                ]]
                                ],
                                success: true
                        ]
                )
            }
        when:
            def orgResult = staffOrgList.runTest { mockServer ->

                def orgListApi = Feign.builder()
                        .client(new ApacheHttpClient())
                        .decoder(new GsonDecoder())
                        .target(IOrgListService.class, mockServer.url as String)

                def cloudId = "1"
                def isCloudManage = false
                controller.cloudId = cloudId
                controller.isCloudManage = isCloudManage
                controller.orgListService = orgListApi
                controller.getMyGrantScreenReportList()
                assert response.status == 200
                assert response.header("Content-Type") == "application/json;charset=UTF-8"
                def jsonData = response.json
                assert jsonData instanceof JSONElement
                assert jsonData.code == 1

                def list = jsonData.list
                assert list?.size() == 1

                def data = list.get(0)
                assert data.name == "众康云测试大屏"
                assert data.code == "01"
                assert data.grpCode == "99"
                assert data.runway == 1
                assert data.editorName == "王"
                assert data.editorId == "1"
                assert data.comment == "测试说明"
            }
        then:
            orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取我有权限的监控大屏报表清单-测试2"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{
            given("获取职员的所属组织列表")
            uponReceiving("a")// 描述
            withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    //body: [provider:"sampleApi2@serviceSet2",dependency:dependency]
                    body: [
                            errcode: null,
                            errmsg: null,
                            error: "SUCCESS",
                            orgList: [[
                                              classes: "07",
                                              orgId: "3" ], [
                                              classes: "01;04;02;11;03",
                                              orgId: "4"
                                      ]],
                            success: true
                    ]
            )

            given("查询组织和层级对应的职员的组织列表")
            uponReceiving("c")
            withAttributes(method: 'post', path: '/orgService/getOrgListByOrgTreeAndLayer')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS",
                            map: [
                                    "1-1": [[
                                                    classes: "04;02;11;03;01",
                                                    orgId: "2"
                                            ]]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def cloudId = "2"
            def isCloudManage = false
            controller.cloudId = cloudId
            controller.isCloudManage = isCloudManage
            controller.orgListService = orgListApi
            controller.getMyGrantScreenReportList()
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 1

            def data1 = list.get(0)
            assert data1.name == "众康云正式大屏"
            assert data1.code == "02"
            assert data1.grpCode == "99"
            assert data1.runway == 1
            assert data1.editorName == "王"
            assert data1.editorId == "1"
            assert data1.comment == "正式说明"
        }
        then:
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取我有权限的监控大屏报表清单-测试3"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{

        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def cloudId = "1"
            def isCloudManage = true
            controller.cloudId = cloudId
            controller.isCloudManage = isCloudManage
            controller.orgListService = orgListApi
            controller.getMyGrantScreenReportList()
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 2

            def data = list.get(0)
            assert data.name == "众康云测试大屏"
            assert data.code == "01"
            assert data.grpCode == "99"
            assert data.runway == 1
            assert data.editorName == "王"
            assert data.editorId == "1"
            assert data.comment == "测试说明"

            def data1 = list.get(1)
            assert data1.name == "众康云大屏"
            assert data1.code == "03"
            assert data1.grpCode == "99"
            assert data1.runway == 1
            assert data1.editorName == "王"
            assert data1.editorId == "1"
            assert data1.comment == "说明"
        }
        then:
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "模拟输入参数测试"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{
            given("查询指定组织的所有上级或下级组织名称")
            uponReceiving("b") // 描述

            withAttributes(method: 'post', path: '/orgService/getOrgTreeList',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body: "jsonData=123")// 指定输入参数-接口需要添加@Body注解，写明参数格式
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
                                    2: [
                                            [
                                                    orgName: "众康云医院",
                                                    orgId: "1"
                                            ],[
                                                    orgName: "心血管科",
                                                    orgId: "2"
                                            ]
                                    ],
                                    3: [
                                            [
                                                    orgName: "众康云医院",
                                                    orgId: "1"
                                            ],[
                                                    orgName: "心血管科",
                                                    orgId: "2"
                                            ],[
                                                    orgName: "赵志王心血管组",
                                                    orgId: "3"
                                            ]
                                    ],
                                    4: [
                                            [
                                                    orgName: "人民医院",
                                                    orgId: "4"
                                            ]
                                    ]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def resp = orgListApi.getOrgTreeList("123")

            assert resp instanceof JSONElement

        }
        then:
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取有管理权限的报表（只有业务管理才有管理权）-测试1"() {
        given:
            def staffOrgList = new PactBuilder() // Create a new PactBuilder
            staffOrgList {
                serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
                hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
                port 1234                       // The port number for the service. It is optional, leave it out to use a random one
            }
            staffOrgList{
                given("查询指定组织的所有上级组织名称")
                uponReceiving("b") // 描述

                withAttributes(method: 'post', path: '/orgService/getOrgTreeList',
                        headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                        body: "jsonData=\"{\"isUpOrDown\":true,\"list\":[1,2,3]}\"")// 指定输入参数-接口需要添加@Body注解，写明参数格式
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
                                        2: [
                                                [
                                                    orgName: "众康云医院",
                                                    orgId: "1"
                                                ],[
                                                    orgName: "心血管科",
                                                    orgId: "2"
                                                ]
                                        ],
                                        3: [
                                                [
                                                    orgName: "众康云医院",
                                                    orgId: "1"
                                                ],[
                                                    orgName: "心血管科",
                                                    orgId: "2"
                                                ],[
                                                    orgName: "赵志王心血管组",
                                                    orgId: "3"
                                                ]
                                        ]
                                ],
                                success: true
                        ]
                )
            }
        when:
            def orgResult = staffOrgList.runTest { mockServer ->

                def orgListApi = Feign.builder()
                        .client(new ApacheHttpClient())
                        .decoder(new GsonDecoder())
                        .target(IOrgListService.class, mockServer.url as String)

                def orgId = null
                def cloudId = "1"
                controller.cloudId = cloudId
                controller.orgListService = orgListApi
                controller.getMyManageReports(orgId, 0, 20)
                assert response.status == 200
                assert response.header("Content-Type") == "application/json;charset=UTF-8"

                def jsonData = response.json
                assert jsonData instanceof JSONElement
                assert jsonData.code == 1

                def list = jsonData.list
                assert list?.size() == 3

                /* 1 */
                def data1 = list.get(0)
                assert data1.reportName == "科主任周报"
                assert data1.groupName == "运营简报"
                assert data1.orgId == null
                assert data1.orgName == ""

                def grantToList1 = data1.grantList
                assert grantToList1?.size() == 3
                assert grantToList1.any{ grantTo ->
                    grantTo.orgId == "3" && grantTo.orgName == "众康云医院>心血管科>赵志王心血管组" && grantTo.roles == "01;02;11" && grantTo.manage == 1
                }
                assert grantToList1.any{ grantTo ->
                    grantTo.orgId == "1" && grantTo.orgName == "众康云医院" && grantTo.roles == "11" && grantTo.manage == 1
                }
                assert grantToList1.any{ grantTo ->
                    grantTo.orgId == "2" && grantTo.orgName == "众康云医院>心血管科" && grantTo.roles == "03;04;11" && grantTo.manage == 1
                }

                /* 2 */
                def data2 = list.get(1)
                assert data2.reportName == "医生团队月报"
                assert data2.groupName == "运营简报"
                assert data2.orgId == null
                assert data2.orgName == ""

                def grantToList2 = data2.grantList
                assert grantToList2?.size() == 2
                assert grantToList2.any{ grantTo ->
                    grantTo.orgId == "3" && grantTo.orgName == "众康云医院>心血管科>赵志王心血管组" && grantTo.roles == "01;02" && grantTo.manage == 0
                }
                assert grantToList2.any{ grantTo ->
                    grantTo.orgId == "1" && grantTo.orgName == "众康云医院" && grantTo.roles == "02" && grantTo.manage == 0
                }

                /* 3 */
                def data3 = list.get(2)
                assert data3.reportName == "药品分析"
                assert data3.groupName == "运营简报"
                assert data3.orgId == null
                assert data3.orgName == "(未授权)"

                def grantToList3 = data3.grantList
                assert grantToList3?.size() == 0
            }
        then:
            orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取有管理权限的报表（只有业务管理才有管理权）-测试1-1"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{
            given("查询指定组织的所有上级组织名称")
            uponReceiving("b") // 描述

            withAttributes(method: 'post', path: '/orgService/getOrgTreeList',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body: "jsonData=\"{\"isUpOrDown\":true,\"list\":[1,2,3,4]}\"")// 指定输入参数-接口需要添加@Body注解，写明参数格式
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
                                    2: [
                                            [
                                                    orgName: "众康云医院",
                                                    orgId: "1"
                                            ],[
                                                    orgName: "心血管科",
                                                    orgId: "2"
                                            ]
                                    ],
                                    3: [
                                            [
                                                    orgName: "众康云医院",
                                                    orgId: "1"
                                            ],[
                                                    orgName: "心血管科",
                                                    orgId: "2"
                                            ],[
                                                    orgName: "赵志王心血管组",
                                                    orgId: "3"
                                            ]
                                    ],
                                    4: [
                                            [
                                                    orgName: "人民医院",
                                                    orgId: "4"
                                            ]
                                    ]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def orgId = null
            def cloudId = "2"
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyManageReports(orgId, 0, 20)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 2

            /* 1 */
            def data = list.get(0)
            assert data.reportName == "监测依从度统计"
            assert data.groupName == "服务管理"
            assert data.orgId == null
            assert data.orgName == ""

            def grantToList = data.grantList
            assert grantToList?.size() == 3
            assert grantToList.any{ grantTo ->
                grantTo.orgId == "3" && grantTo.orgName == "众康云医院>心血管科>赵志王心血管组" && grantTo.roles == "11;02" && grantTo.manage == 0
            }
            assert grantToList.any{ grantTo ->
                grantTo.orgId == "4" && grantTo.orgName == "人民医院" && grantTo.roles == "01;02;03;06;11" && grantTo.manage == 1
            }
            assert grantToList.any{ grantTo ->
                grantTo.orgId == "2" && grantTo.orgName == "众康云医院>心血管科" && grantTo.roles == "01;11" && grantTo.manage == 1
            }

            /* 2 */
            def data4 = list.get(1)
            assert data4.reportName == "用药依从性统计"
            assert data4.groupName == "服务管理"
            assert data4.orgId == null
            assert data4.orgName == ""

            def grantToList4 = data4.grantList
            assert grantToList4?.size() == 2
            assert grantToList4.any{ grantTo ->
                grantTo.orgId == "2" && grantTo.orgName == "众康云医院>心血管科" && grantTo.roles == "02" && grantTo.manage == 0
            }
            assert grantToList4.any{ grantTo ->
                grantTo.orgId == "1" && grantTo.orgName == "众康云医院" && grantTo.roles == "01;02" && grantTo.manage == 1
            }
        }
        then:
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取有管理权限的报表（只有业务管理才有管理权）-测试2"() {
        given:
            def staffOrgList = new PactBuilder() // Create a new PactBuilder
            staffOrgList {
                serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
                hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
                port 1234                       // The port number for the service. It is optional, leave it out to use a random one
            }
            staffOrgList{
                given("查询指定组织的所有上级组织名称")
                uponReceiving("b") // 描述
                withAttributes(method: 'post', path: '/orgService/getOrgTreeList',
                        headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                        body: "jsonData=\"{\"isUpOrDown\":true,\"list\":[1,2,3]}\"")// 指定输入参数-接口需要添加@Body注解，写明参数格式
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
                                        2: [
                                                [
                                                        orgName: "众康云医院",
                                                        orgId: "1"
                                                ],[
                                                        orgName: "心血管科",
                                                        orgId: "2"
                                                ]
                                        ],
                                        3: [
                                                [
                                                        orgName: "众康云医院",
                                                        orgId: "1"
                                                ],[
                                                        orgName: "心血管科",
                                                        orgId: "2"
                                                ],[
                                                        orgName: "赵志王心血管组",
                                                        orgId: "3"
                                                ]
                                        ]
                                ],
                                success: true
                        ]
                )

                given("查询指定组织的所有下级组织名称")
                uponReceiving("a") // 描述
                withAttributes(method: 'post', path: '/orgService/getOrgTreeList',
                        headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                        body: "jsonData=\"{\"isUpOrDown\":false,\"list\":[1,2,3]}\"")// 指定输入参数-接口需要添加@Body注解，写明参数格式
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
                                                ],[
                                                        orgName: "心血管科",
                                                        orgId: "2"
                                                ],[
                                                        orgName: "赵志王心血管组",
                                                        orgId: "3"
                                                ]
                                        ],
                                        2: [
                                                [
                                                        orgName: "心血管科",
                                                        orgId: "2"
                                                ],[
                                                        orgName: "赵志王心血管组",
                                                        orgId: "3"
                                                ]
                                        ],
                                        3: [
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
        when:
            def orgResult = staffOrgList.runTest { mockServer ->

                def orgListApi = Feign.builder()
                        .client(new ApacheHttpClient())
                        .decoder(new GsonDecoder())
                        .target(IOrgListService.class, mockServer.url as String)

                def cloudId = "1"
                controller.cloudId = cloudId
                controller.orgListService = orgListApi
                controller.getMyManageReports("1", 0, 20)
                assert response.status == 200
                assert response.header("Content-Type") == "application/json;charset=UTF-8"

                def jsonData = response.json
                assert jsonData instanceof JSONElement
                assert jsonData.code == 1

                def list = jsonData.list
                assert list?.size() == 1

                /* 1 */
                def data = list.get(0)
                assert data.reportName == "科主任周报"
                assert data.groupName == "运营简报"
                assert data.orgId == "1"
                assert data.orgName == ""

                def grantToList = data.grantList
                assert grantToList?.size() == 3
                assert grantToList.any{ grantTo ->
                    grantTo.orgId == "3" && grantTo.orgName == "众康云医院>心血管科>赵志王心血管组" && grantTo.roles == "01;02;11" && grantTo.manage == 1
                }
                assert grantToList.any{ grantTo ->
                    grantTo.orgId == "1" && grantTo.orgName == "众康云医院" && grantTo.roles == "11" && grantTo.manage == 1
                }
                assert grantToList.any{ grantTo ->
                    grantTo.orgId == "2" && grantTo.orgName == "众康云医院>心血管科" && grantTo.roles == "03;04;11" && grantTo.manage == 1
                }
            }
        then:
            orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取有管理权限的报表（只有业务管理才有管理权）-测试2-1"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{
            given("查询指定组织的所有上级组织名称")
            uponReceiving("b") // 描述
            withAttributes(method: 'post', path: '/orgService/getOrgTreeList',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body: "jsonData=\"{\"isUpOrDown\":true,\"list\":[1,2,3,4]}\"")// 指定输入参数-接口需要添加@Body注解，写明参数格式
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
                                    2: [
                                            [
                                                    orgName: "众康云医院",
                                                    orgId: "1"
                                            ],[
                                                    orgName: "心血管科",
                                                    orgId: "2"
                                            ]
                                    ],
                                    3: [
                                            [
                                                    orgName: "众康云医院",
                                                    orgId: "1"
                                            ],[
                                                    orgName: "心血管科",
                                                    orgId: "2"
                                            ],[
                                                    orgName: "赵志王心血管组",
                                                    orgId: "3"
                                            ]
                                    ],
                                    4: [
                                            [
                                                    orgName: "人民医院",
                                                    orgId: "4"
                                            ]
                                    ]
                            ],
                            success: true
                    ]
            )

            given("查询指定组织的所有下级组织名称")
            uponReceiving("a") // 描述
            withAttributes(method: 'post', path: '/orgService/getOrgTreeList',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body: "jsonData=\"{\"isUpOrDown\":false,\"list\":[1,2,3,4]}\"")// 指定输入参数-接口需要添加@Body注解，写明参数格式
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
                                            ],[
                                                    orgName: "心血管科",
                                                    orgId: "2"
                                            ],[
                                                    orgName: "赵志王心血管组",
                                                    orgId: "3"
                                            ]
                                    ],
                                    2: [
                                            [
                                                    orgName: "心血管科",
                                                    orgId: "2"
                                            ],[
                                                    orgName: "赵志王心血管组",
                                                    orgId: "3"
                                            ]
                                    ],
                                    3: [
                                            [
                                                    orgName: "赵志王心血管组",
                                                    orgId: "3"
                                            ]
                                    ],
                                    4: [
                                            [
                                                    orgName: "人民医院",
                                                    orgId: "4"
                                            ]
                                    ]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->

            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockServer.url as String)

            def cloudId = "2"
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyManageReports("1", 0, 20)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 1

            /* 1 */
            def data1 = list.get(0)
            assert data1.reportName == "用药依从性统计"
            assert data1.groupName == "服务管理"
            assert data1.orgId == "1"
            assert data1.orgName == ""

            def grantToList1 = data1.grantList
            assert grantToList1?.size() == 2
            assert grantToList1.any{ grantTo ->
                grantTo.orgId == "2" && grantTo.orgName == "众康云医院>心血管科" && grantTo.roles == "02" && grantTo.manage == 0
            }
            assert grantToList1.any{ grantTo ->
                grantTo.orgId == "1" && grantTo.orgName == "众康云医院" && grantTo.roles == "01;02" && grantTo.manage == 1
            }
        }
        then:
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取报表已有授权列表-测试1"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{
            given("查询指定组织的所有上级组织名称")
            uponReceiving("b") // 描述

            withAttributes(method: 'post', path: '/orgService/getOrgTreeList',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body: "jsonData=\"{\"isUpOrDown\":true,\"list\":[1,2,3]}\"")// 指定输入参数-接口需要添加@Body注解，写明参数格式
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
                                    2: [
                                            [
                                                    orgName: "众康云医院",
                                                    orgId: "1"
                                            ],[
                                                    orgName: "心血管科",
                                                    orgId: "2"
                                            ]
                                    ],
                                    3: [
                                            [
                                                    orgName: "众康云医院",
                                                    orgId: "1"
                                            ],[
                                                    orgName: "心血管科",
                                                    orgId: "2"
                                            ],[
                                                    orgName: "赵志王心血管组",
                                                    orgId: "3"
                                            ]
                                    ]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->
            Report.withNewSession {
                def orgListApi = Feign.builder()
                        .client(new ApacheHttpClient())
                        .decoder(new GsonDecoder())
                        .target(IOrgListService.class, mockServer.url as String)

                controller.orgListService = orgListApi

                Report report = Report.findByCode("KZRZB")
                String reportId = report.id
                controller.getReportGrantToByRptId(reportId)

                assert response.status == 200
                assert response.header("Content-Type") == "application/json;charset=UTF-8"

                def jsonData = response.json
                assert jsonData instanceof JSONElement
                assert jsonData.code == 1

                def list = jsonData.list
                assert list?.size() == 3

                assert list.any{ grantTo ->
                    grantTo.rptId == reportId && grantTo.orgId == "1" && grantTo.orgName == "众康云医院" && grantTo.roles == "11" && grantTo.manage == 1
                }
                assert list.any{ grantTo ->
                    grantTo.rptId == reportId && grantTo.orgId == "2" && grantTo.orgName == "众康云医院>心血管科" && grantTo.roles == "03;04;11" && grantTo.manage == 1
                }
                assert list.any{ grantTo ->
                    grantTo.rptId == reportId && grantTo.orgId == "3" && grantTo.orgName == "众康云医院>心血管科>赵志王心血管组" && grantTo.roles == "01;02;11" && grantTo.manage == 1
                }
            }
        }
        then:
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取报表已有授权列表-测试2"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{

        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->
            Report.withNewSession {
                def orgListApi = Feign.builder()
                        .client(new ApacheHttpClient())
                        .decoder(new GsonDecoder())
                        .target(IOrgListService.class, mockServer.url as String)

                controller.orgListService = orgListApi

                String reportId = null
                controller.getReportGrantToByRptId(reportId)

                assert response.status == 200
                assert response.header("Content-Type") == "application/json;charset=UTF-8"

                def jsonData = response.json
                assert jsonData instanceof JSONElement
                assert jsonData.code == 3
                assert jsonData.message == "参数不能为空"

                def list = jsonData.list
                assert list == null

            }
        }
        then:
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }

    void "获取报表已有授权列表-测试3"() {
        given:
        def staffOrgList = new PactBuilder() // Create a new PactBuilder
        staffOrgList {
            serviceConsumer "ORG-SERVICE" 	// Define the service consumer by name
            hasPactWith "ORG-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        staffOrgList{

        }
        when:
        def orgResult = staffOrgList.runTest { mockServer ->
            Report.withNewSession {
                def orgListApi = Feign.builder()
                        .client(new ApacheHttpClient())
                        .decoder(new GsonDecoder())
                        .target(IOrgListService.class, mockServer.url as String)

                controller.orgListService = orgListApi

                String reportId = "1"
                controller.getReportGrantToByRptId(reportId)

                assert response.status == 200
                assert response.header("Content-Type") == "application/json;charset=UTF-8"

                def jsonData = response.json
                assert jsonData instanceof JSONElement
                assert jsonData.code == 1
                assert jsonData.message == "执行成功"

                def list = jsonData.list
                assert list?.size() == 0

            }
        }
        then:
        orgResult == PactVerificationResult.Ok.INSTANCE  // This means it is all good
    }
}

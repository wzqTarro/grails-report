package com.zcareze.report

import au.com.dius.pact.consumer.PactVerificationResult
import au.com.dius.pact.consumer.groovy.PactBuilder
import com.report.service.IOrgListService
import feign.Feign
import feign.gson.GsonDecoder
import feign.httpclient.ApacheHttpClient
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.web.json.JSONElement
import org.junit.Ignore
import org.springframework.test.annotation.Rollback
import spock.lang.Specification

@Integration
@Rollback
//@Ignore
class ReportUsuallyControllerPactSpec extends Specification implements ControllerUnitTest<ReportUsuallyController>{
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
            def report1 = new Report(code: "KZRZB", name: "科主任周报", grpCode: group1.code, runway: 1, editorName: "王", editorId: "1",cloudId: "2");
            def report2 = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: group2.code, runway: 2, editorName: "王", editorId: "1", cloudId: "1");
            def report3 = new Report(code: "TZYB", name: "医生团队月报", grpCode: group1.code, runway: 1, editorName: "王", editorId: "1", cloudId: "2");
            def report4 = new Report(code: "YYYCD", name: "用药依从性统计", grpCode: group2.code, runway: 1, editorName: "王", editorId: "1", cloudId: "2");
            def report5 = new Report(code: "YPFX", name: "药品分析", grpCode: group1.code, runway: 1, editorName: "王", editorId: "1", cloudId:"1")

            report1.save()
            report2.save()
            report3.save()
            report4.save()
            report5.save(flush:true)

            Report.executeUpdate("update Report r set r.editTime=:editTime where r.code=:code", [editTime:new Date()-1, code: 'JCYCD'])

            report2 = Report.findByCode('JCYCD');

            new ReportInputs(rpt: report1, name: "orgid", caption: "机构", seqNum: 0, dataType: "31", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构" ).save()
            new ReportStyle(rpt: report1, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt").save()
            new ReportGrantTo(rpt: report1, orgId: "1", roles: "11;07", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report1, orgId: "2", roles: "03;04;11;07", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report1, orgId: "3", roles: "01;02;11;07", manage: 1, granter: "王").save()
            new ReportOpenTo(rpt: report1, orgTreeId: 1, orgTreeLayer: 1, roles: "01").save()

            //report2.save();
            new ReportInputs(rpt: report2, name: "endtime", caption: "结束时间", seqNum: 3, dataType: "23", inputType: 1, defType: "今天" ).save()
            new ReportInputs(rpt: report2, name: "month", caption: "月份", seqNum: 0, dataType: "12", inputType: 2, optionList: "1;2;3;4;5;6;7;8;9;10;11;12", defValue: "12" ).save()
            new ReportInputs(rpt: report2, name: "name", caption: "医生", seqNum: 2, dataType: "11", inputType: 1).save()
            new ReportInputs(rpt: report2, name: "year", caption: "开始时间", seqNum: 1, dataType: "21", inputType: 1, defType: "本年").save()
            new ReportStyle(rpt: report2, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt").save()
            new ReportStyle(rpt: report2, scene: 1, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/6a04cb4e3ed9420aa1f1c881650325ff.xslt").save()
            new ReportGrantTo(rpt: report2, orgId: "4", roles: "01;02;03;06;07", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report2, orgId: "2", roles: "01;11;07", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report2, orgId: "3", roles: "11;02", manage: 0, granter: "王").save()

            //report3.save();
            new ReportStyle(rpt: report3, scene: 2, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/3913c97922e4485d93fd5a4f7e05bd65/0e8f634358764214a508c77abafcd55a.xslt").save()
            new ReportGrantTo(rpt: report3, orgId: "3", roles: "01;02", manage: 0, granter: "王").save()
            new ReportGrantTo(rpt: report3, orgId: "1", roles: "02", manage: 0, granter: "王").save()

            //report4.save();
            new ReportStyle(rpt: report4, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/608639880900448ead762ca2246659ce/464ff931e29645088c3fbd325bed5227.xslt").save()
            new ReportGrantTo(rpt: report4, orgId: "1", roles: "01;02;07", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report4, orgId: "2", roles: "02", manage: 0, granter: "王").save()
            new ReportOpenTo(rpt: report4, orgTreeId: "1", orgTreeLayer: 0, roles: "02", granter: "王").save()

            // 个人常用报表
            new ReportUsually(staffId: "1", rpt: report1, seqNum: 2, cloudId: "2").save()
            new ReportUsually(staffId: "1", rpt: report2, seqNum: 3, cloudId: "1").save();
            new ReportUsually(staffId: "1", rpt: report4, seqNum: 1, cloudId: "2").save();

            new ReportUsually(staffId: "2", rpt: report1, seqNum: 1, cloudId: "2").save();


            // 大屏
            def screenReport1 = new Report(code: "01", name: "众康云测试大屏", grpCode: group3.code, runway: 1, editorName: "王", editorId: "1", comment: "测试说明");
            def screenReport2 = new Report(code: "02", name: "众康云正式大屏", grpCode: group3.code, runway: 1, editorName: "王", editorId: "1", comment: "正式说明");
            def screenReport3 = new Report(code: "03", name: "众康云大屏", grpCode: group3.code, runway: 1, editorName: "王", editorId: "1", comment: "说明");
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

    void "获取我的常用报表-测试1"() {
        given:
            def orgList = new PactBuilder()
            orgList {
                serviceConsumer "ORG-SERVICE"
                hasPactWith "ORG-SERVICE"
                port 1234
            }
            orgList {
                given("获取职员的所属组织列表")
                uponReceiving("a")// 描述
                withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
                willRespondWith(
                        status: 200,
                        headers: ['Content-Type': 'application/json'],
                        body: [
                                errcode: null,
                                errmsg: null,
                                error: "SUCCESS",
                                orgList: [
                                            [
                                                  classes: "01;04;02;11;03",
                                                  orgId: "4"
                                            ],
                                            [
                                                    classes: "02",
                                                    orgId: "2"
                                            ]
                                ],
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
                                        "1-0": [[
                                                        classes: "04;02;11;03;01",
                                                        orgId: "2"
                                                ]]
                                ],
                                success: true
                        ]
                )
            }
        when:
            def result = orgList.runTest { mockService ->
                def orgListApi = Feign.builder()
                                    .client(new ApacheHttpClient())
                                    .decoder(new GsonDecoder())
                                    .target(IOrgListService.class, mockService.url as String)

                def cloudId = "1"
                controller.cloudId = cloudId
                controller.orgListService = orgListApi
                controller.getMyUsuallyReports(0)
                assert response.status == 200
                assert response.header("Content-Type") == "application/json;charset=UTF-8"
                def jsonData = response.json
                assert jsonData instanceof JSONElement
                assert jsonData.code == 1

                def list = jsonData.list
                assert list?.size() == 1

                def data1 = list.get(0)
                assert data1.reportName == "监测依从度统计"
                assert data1.groupCode == "02"
                assert data1.groupName == "服务管理"
                assert data1.color == "7BAFA1"
                assert data1.seqNum == 3
                assert data1.usually == true
                assert data1.noRead == true
                assert data1.grantTo == null
            }
        then:
            result == PactVerificationResult.Ok.INSTANCE
    }

    void "获取我的常用报表-测试1-1"() {
        given:
        def orgList = new PactBuilder()
        orgList {
            serviceConsumer "ORG-SERVICE"
            hasPactWith "ORG-SERVICE"
            port 1234
        }
        orgList {
            given("获取职员的所属组织列表")
            uponReceiving("a")// 描述
            withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: null,
                            errmsg: null,
                            error: "SUCCESS",
                            orgList: [
                                    [
                                            classes: "01;04;02;11;03",
                                            orgId: "4"
                                    ],
                                    [
                                            classes: "02",
                                            orgId: "2"
                                    ]
                            ],
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
                                    "1-0": [[
                                                    classes: "04;02;11;03;01",
                                                    orgId: "2"
                                            ]]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def result = orgList.runTest { mockService ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockService.url as String)

            def cloudId = "2"
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyUsuallyReports(0)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 1

            def data = list.get(0)
            assert data.reportName == "用药依从性统计"
            assert data.groupCode == "02"
            assert data.groupName == "服务管理"
            assert data.color == "7BAFA1"
            assert data.seqNum == 1
            assert data.usually == true
            assert data.noRead == false
            assert data.grantTo == null

        }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }

    void "获取我的常用报表-测试2"() {
        given:
        def orgList = new PactBuilder()
        orgList {
            serviceConsumer "ORG-SERVICE"
            hasPactWith "ORG-SERVICE"
            port 1234
        }
        orgList {
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
                            orgList: [
                                    [
                                            classes: "01;04;02;11;03",
                                            orgId: "4"
                                    ],
                                    [
                                            classes: "02",
                                            orgId: "2"
                                    ]
                            ],
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
                                    "1-0": [[
                                                    classes: "04;02;11;03;01",
                                                    orgId: "2"
                                            ]]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def result = orgList.runTest { mockService ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockService.url as String)

            def cloudId = "1"
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyUsuallyReports(1)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 1

            def data = list.get(0)
            assert data.reportName == "监测依从度统计"
            assert data.groupCode == "02"
            assert data.groupName == "服务管理"
            assert data.color == "7BAFA1"
            assert data.seqNum == 3
            assert data.usually == true
            assert data.noRead == true
            assert data.grantTo == null
        }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }

    void "获取我的常用报表-测试2-1"() {
        given:
        def orgList = new PactBuilder()
        orgList {
            serviceConsumer "ORG-SERVICE"
            hasPactWith "ORG-SERVICE"
            port 1234
        }
        orgList {

        }
        when:
        def result = orgList.runTest { mockService ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockService.url as String)

            def cloudId = "2"
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyUsuallyReports(1)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 0
        }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }

    void "获取我的常用报表-测试3"() {
        given:
        def orgList = new PactBuilder()
        orgList {
            serviceConsumer "ORG-SERVICE"
            hasPactWith "ORG-SERVICE"
            port 1234
        }
        orgList {

        }
        when:
        def result = orgList.runTest { mockService ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockService.url as String)

            controller.cloudId = "1"
            controller.orgListService = orgListApi
            controller.getMyUsuallyReports(2)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 0
        }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }

    void "获取我的常用报表-测试4"() {
        given:
            def orgList = new PactBuilder()
            orgList {
                serviceConsumer "ORG-SERVICE"
                hasPactWith "ORG-SERVICE"
                port 1234
            }
            orgList {

            }
        when:
        def result = orgList.runTest { mockService ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockService.url as String)

            controller.cloudId = "1"
            controller.orgListService = orgListApi
            controller.getMyUsuallyReports(3)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 0
        }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }

    void "api获取我的常用报表-测试1"() {
        given:
        def orgList = new PactBuilder()
        orgList {
            serviceConsumer "ORG-SERVICE"
            hasPactWith "ORG-SERVICE"
            port 1234
        }
        orgList {
            given("获取职员的所属组织列表")
            uponReceiving("a")// 描述
            withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: null,
                            errmsg: null,
                            error: "SUCCESS",
                            orgList: [
                                    [
                                            classes: "01;04;02;11;03",
                                            orgId: "4"
                                    ],
                                    [
                                            classes: "02",
                                            orgId: "2"
                                    ]
                            ],
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
                                    "1-0": [[
                                                    classes: "04;02;11;03;01",
                                                    orgId: "2"
                                            ]]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def result = orgList.runTest { mockService ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockService.url as String)

            def cloudId = "1"
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyUsuallyReportOld(0)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData

            def list = bizData.reportUsuallyVOs
            assert list?.size() == 1

            def data1 = list.get(0)
            assert data1.reportName == "监测依从度统计"
            assert data1.groupCode == "02"
            assert data1.groupName == "服务管理"
            assert data1.color == "7BAFA1"
            assert data1.seqNum == 3
            assert data1.usually == true
            assert data1.noRead == true
            assert data1.grantTo == null
        }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }

    void "api获取我的常用报表-测试1-1"() {
        given:
        def orgList = new PactBuilder()
        orgList {
            serviceConsumer "ORG-SERVICE"
            hasPactWith "ORG-SERVICE"
            port 1234
        }
        orgList {
            given("获取职员的所属组织列表")
            uponReceiving("a")// 描述
            withAttributes(method: 'post', path: '/orgService/getOrgListByStaffId')
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: null,
                            errmsg: null,
                            error: "SUCCESS",
                            orgList: [
                                    [
                                            classes: "01;04;02;11;03",
                                            orgId: "4"
                                    ],
                                    [
                                            classes: "02",
                                            orgId: "2"
                                    ]
                            ],
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
                                    "1-0": [[
                                                    classes: "04;02;11;03;01",
                                                    orgId: "2"
                                            ]]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def result = orgList.runTest { mockService ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockService.url as String)

            def cloudId = "2"
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyUsuallyReportOld(0)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData

            def list = bizData.reportUsuallyVOs
            assert list?.size() == 1

            def data = list.get(0)
            assert data.reportName == "用药依从性统计"
            assert data.groupCode == "02"
            assert data.groupName == "服务管理"
            assert data.color == "7BAFA1"
            assert data.seqNum == 1
            assert data.usually == true
            assert data.noRead == false
            assert data.grantTo == null
        }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }

    void "api获取我的常用报表-测试2"() {
        given:
        def orgList = new PactBuilder()
        orgList {
            serviceConsumer "ORG-SERVICE"
            hasPactWith "ORG-SERVICE"
            port 1234
        }
        orgList {
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
                            orgList: [
                                    [
                                            classes: "01;04;02;11;03",
                                            orgId: "4"
                                    ],
                                    [
                                            classes: "02",
                                            orgId: "2"
                                    ]
                            ],
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
                                    "1-0": [[
                                                    classes: "04;02;11;03;01",
                                                    orgId: "2"
                                            ]]
                            ],
                            success: true
                    ]
            )
        }
        when:
        def result = orgList.runTest { mockService ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockService.url as String)

            def cloudId = "1"
            controller.cloudId = cloudId
            controller.orgListService = orgListApi
            controller.getMyUsuallyReportOld(1)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData

            def list = bizData.reportUsuallyVOs
            assert list?.size() == 1

            def data = list.get(0)
            assert data.reportName == "监测依从度统计"
            assert data.groupCode == "02"
            assert data.groupName == "服务管理"
            assert data.color == "7BAFA1"
            assert data.seqNum == 3
            assert data.usually == true
            assert data.noRead == true
            assert data.grantTo == null
        }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }

    void "api获取我的常用报表-测试3"() {
        given:
        def orgList = new PactBuilder()
        orgList {
            serviceConsumer "ORG-SERVICE"
            hasPactWith "ORG-SERVICE"
            port 1234
        }
        orgList {

        }
        when:
        def result = orgList.runTest { mockService ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockService.url as String)

            controller.orgListService = orgListApi
            controller.getMyUsuallyReportOld(2)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData

            def list = bizData.reportUsuallyVOs
            assert list?.size() == 0
        }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }

    void "api获取我的常用报表-测试4"() {
        given:
        def orgList = new PactBuilder()
        orgList {
            serviceConsumer "ORG-SERVICE"
            hasPactWith "ORG-SERVICE"
            port 1234
        }
        orgList {

        }
        when:
        def result = orgList.runTest { mockService ->
            def orgListApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IOrgListService.class, mockService.url as String)

            controller.orgListService = orgListApi
            controller.getMyUsuallyReportOld(3)
            assert response.status == 200
            assert response.header("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.errcode == 0

            def bizData = jsonData.bizData
            assert bizData

            def list = bizData.reportUsuallyVOs
            assert list?.size() == 0
        }
        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
}

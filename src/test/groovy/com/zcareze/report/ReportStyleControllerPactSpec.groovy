package com.zcareze.report

import au.com.dius.pact.consumer.PactVerificationResult
import au.com.dius.pact.consumer.groovy.PactBuilder
import com.report.service.IFileService
import feign.Feign
import feign.gson.GsonDecoder
import feign.httpclient.ApacheHttpClient
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import org.junit.Ignore
import org.springframework.test.annotation.Rollback
import spock.lang.Specification

@Integration
@Rollback
//@Ignore
class ReportStyleControllerPactSpec extends Specification implements ControllerUnitTest<ReportStyleController> {
    def setup() {
        Report.withNewSession {
            def group1 = new ReportGroups(code:"01", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表", color: "ffc100");
            def group2 = new ReportGroups(code:"02", name:"服务管理", comment:"有关服务工作开展情况和开展内容等信息的呈现", color: "7BAFA1");
            def group3 = new ReportGroups(code:"99", name:"监控大屏", comment:"内置专门存放监控大屏报表的分组", color: "b8e986");
            group1.save();
            group2.save();
            group3.save();

            def report1 = new Report(code: "KZRZB", name: "科主任周报", grpCode: group1.code, runway: 1, editorName: "王", editorId: "1",);
            def report2 = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: group2.code, runway: 2, editorName: "王", editorId: "1");
            report1.save();
            report2.save(flush: true);

            new ReportStyle(rpt: report1, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt").save()

            new ReportStyle(rpt: report2, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt").save()
            new ReportStyle(rpt: report2, scene: 1, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/6a04cb4e3ed9420aa1f1c881650325ff.xslt").save()

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
    void accessParam(params, param) {
        params["rptId"] = param.rpt
        params["scene"] = param.scene
        params["fileContent"] = param.fileContent
        params["comment"] = param.comment
        params["chart"] = param.chart
    }
    void "新增报表样式-测试1"(){

        given:
            def rptId = null
            Report.withNewSession {
                Report report = Report.findByCode("KZRZB")
                rptId = report.id
            }
            def fileBuilder = new PactBuilder()
            fileBuilder {
                serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
                hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
                port 1234                       // The port number for the service. It is optional, leave it out to use a random one
            }
            fileBuilder{
                given("上传文件")
                withAttributes(method: 'post', path: '/fileBizService/uploadReportFile',
                        headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                        body:"cloudId=cloudId&reportId="+rptId
                )
                willRespondWith(
                        status: 200,
                        headers: ['Content-Type': 'application/json'],
                        body: [
                                errcode: 0,
                                errmsg: "成功",
                                error: "SUCCESS",
                                strValue: "/cloudId/xslt/${rptId}/uuid.xslt"
                        ]
                )

            }
        when:
            def result = fileBuilder.runTest { mockServer ->
                def fileApi = Feign.builder()
                        .client(new ApacheHttpClient())
                        .decoder(new GsonDecoder())
                        .target(IFileService.class, mockServer.url as String)

                controller.fileService = fileApi

                def param = [rpt: rptId, scene: 2, fileContent: "123"]
                accessParam(controller.params, param)

                controller.addReportStyle()

                assert response.status == 200
                def jsonData = response.json
                assert jsonData.code == 1
                assert jsonData.message == "执行成功"
                ReportStyle style = ReportStyle.createCriteria().get {
                        and {
                            rpt{
                                eq("id", rptId)
                            }
                            eq("scene", 2)
                        }
                }
                assert style.fileUrl == "/cloudId/xslt/${rptId}/uuid.xslt"
                assert style.comment == null
                assert style.chart == null
            }

        then:
            result == PactVerificationResult.Ok.INSTANCE
    }
    void "新增报表样式-测试2"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{
            given("上传文件")
            withAttributes(method: 'post', path: '/fileBizService/uploadReportFile',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body:"cloudId=cloudId&reportId="+rptId
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 1,
                            errmsg: "成功",
                            error: "SUCCESS",
                            strValue: "/cloudId/xslt/${rptId}/uuid.xslt"
                    ]
            )
        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def param = [rpt: rptId, scene: 2, fileContent: "123"]
            accessParam(controller.params, param)

            controller.addReportStyle()

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "上传xslt失败"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "新增报表样式-测试3"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{

        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def param = [rpt: rptId, scene: 2, fileContent: ""]
            accessParam(controller.params, param)

            controller.addReportStyle()

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "xslt内容不能为空"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "新增报表样式-测试4"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{

        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def param = [rpt: null, scene: 2, fileContent: "123"]
            accessParam(controller.params, param)

            controller.addReportStyle()

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "报表标识不能为空"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "新增报表样式-测试5"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{

        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def param = [rpt: "123", scene: 2, fileContent: "123"]
            accessParam(controller.params, param)

            controller.addReportStyle()

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "报表不存在"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "新增报表样式-测试6"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{
            given("上传文件")
            withAttributes(method: 'post', path: '/fileBizService/uploadReportFile',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body:"cloudId=cloudId&reportId="+rptId
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS",
                            strValue: "/cloudId/xslt/${rptId}/uuid.xslt"
                    ]
            )

            given("删除文件")
            withAttributes(method: 'post', path: '/fileBizService/deleteReportFile',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body:"fileUrl=/cloudId/xslt/"+ rptId+ "/uuid.xslt"
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS"
                    ]
            )
        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def param = [rpt: rptId, scene: 0, fileContent: "123"]
            accessParam(controller.params, param)

            controller.addReportStyle()

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "[报表样式]类的属性[scene]的值[0]必须是唯一的;"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "新增报表样式-测试7"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{
            given("上传文件")
            withAttributes(method: 'post', path: '/fileBizService/uploadReportFile',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body:"cloudId=cloudId&reportId="+rptId
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS",
                            strValue: "/cloudId/xslt/${rptId}/uuid.xslt"
                    ]
            )

            given("删除文件")
            withAttributes(method: 'post', path: '/fileBizService/deleteReportFile',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body:"fileUrl=/cloudId/xslt/"+ rptId+ "/uuid.xslt"
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS"
                    ]
            )
        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def param = [rpt: rptId, scene: null, fileContent: "123"]
            accessParam(controller.params, param)

            controller.addReportStyle()

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "[报表样式]类的属性[scene]不能为null;"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "新增报表样式-测试8"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{
            given("上传文件")
            withAttributes(method: 'post', path: '/fileBizService/uploadReportFile',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body:"cloudId=cloudId&reportId="+rptId
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS",
                            strValue: "/cloudId/xslt/${rptId}/uuid.xslt"
                    ]
            )
            given("删除文件")
            withAttributes(method: 'post', path: '/fileBizService/deleteReportFile',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body:"fileUrl=/cloudId/xslt/"+ rptId+ "/uuid.xslt"
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS"
                    ]
            )
        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def param = [rpt: rptId, scene: 4, fileContent: "123"]
            accessParam(controller.params, param)

            controller.addReportStyle()

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "[报表样式]类的属性[scene]的值[4]不在列表的取值范围内;"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "修改样式-测试1"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{
            given("上传文件")
            withAttributes(method: 'post', path: '/fileBizService/uploadReportFile',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body:"cloudId=cloudId&reportId="+rptId
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS",
                            strValue: "/cloudId/xslt/${rptId}/uuid.xslt"
                    ]
            )
        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def param = [rpt: rptId, scene: 0, fileContent: "123", comment: "说明", chart:"<chart></chart>"]
            accessParam(controller.params, param)

            controller.editReportStyle()

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
            ReportStyle style = ReportStyle.createCriteria().get {
                and {
                    rpt{
                        eq("id", rptId)
                    }
                    eq("scene", 0)
                }
            }
            assert style.fileUrl == "/cloudId/xslt/${rptId}/uuid.xslt"
            assert style.comment == "说明"
            assert style.chart == "<chart></chart>"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "修改样式-测试2"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{
            given("上传文件")
            withAttributes(method: 'post', path: '/fileBizService/uploadReportFile',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body:"cloudId=cloudId&reportId="+rptId
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 1,
                            errmsg: "成功",
                            error: "SUCCESS",
                            strValue: "/cloudId/xslt/${rptId}/uuid.xslt"
                    ]
            )
        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def param = [rpt: rptId, scene: 0, fileContent: "123", comment: "说明", chart:"<chart></chart>"]
            accessParam(controller.params, param)

            controller.editReportStyle()

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "上传xslt失败"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "修改样式-测试3"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{

        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def param = [rpt: null, scene: 0, fileContent: "123", comment: "说明", chart:"<chart></chart>"]
            accessParam(controller.params, param)

            controller.editReportStyle()

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "报表标识不能为空"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "修改样式-测试4"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{

        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def param = [rpt: rptId, scene: -1, fileContent: "123", comment: "说明", chart:"<chart></chart>"]
            accessParam(controller.params, param)

            controller.editReportStyle()

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "场景标识不能为空"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "修改样式-测试5"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{

        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def param = [rpt: rptId, scene: 4, fileContent: "123", comment: "说明", chart:"<chart></chart>"]
            accessParam(controller.params, param)

            controller.editReportStyle()

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "样式不存在"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "删除报表的指定样式-测试1"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{
            given("删除文件")
            withAttributes(method: 'post', path: '/fileBizService/deleteReportFile',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body:"fileUrl=a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt"
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS"
                    ]
            )
        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def scene = 0

            controller.deleteReportStyle(rptId, scene)

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
            ReportStyle style = ReportStyle.createCriteria().get {
                and {
                    rpt{
                        eq("id", rptId)
                    }
                    eq("scene", scene)
                }
            }
            assert style == null
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "删除报表的指定样式-测试2"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{

        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def scene = 1

            controller.deleteReportStyle(rptId, scene)

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "样式不存在"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "删除报表的指定样式-测试3"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{
        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def scene = null

            controller.deleteReportStyle(null, scene)

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "报表标识和场景标识不能为空"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "删除报表的指定样式-测试4"(){

        given:
        def rptId = null
        Report.withNewSession {
            Report report = Report.findByCode("KZRZB")
            rptId = report.id
        }
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{
            given("删除文件")
            withAttributes(method: 'post', path: '/fileBizService/deleteReportFile',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body:"fileUrl=a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt"
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 1,
                            errmsg: "成功",
                            error: "SUCCESS"
                    ]
            )
        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def scene = 0

            controller.deleteReportStyle(rptId, scene)

            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "删除失败"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "获取指定报表的样式列表-测试1"(){

        given:
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{
            given("获取文件授权访问路径")
            withAttributes(method: 'post', path: '/fileBizService/getReportFileVisitUrl',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body:"fileUrl=a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/6a04cb4e3ed9420aa1f1c881650325ff.xslt&accountId=1"
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS",
                            visitUrlDTO:[
                                    ossUrl: "样式1路径"
                            ]
                    ]
            )
            given("获取文件授权访问路径")
            withAttributes(method: 'post', path: '/fileBizService/getReportFileVisitUrl',
                    headers: ['Content-Type': 'application/x-www-form-urlencoded'],
                    body:"fileUrl=a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt&accountId=1"
            )
            willRespondWith(
                    status: 200,
                    headers: ['Content-Type': 'application/json'],
                    body: [
                            errcode: 0,
                            errmsg: "成功",
                            error: "SUCCESS",
                            visitUrlDTO:[
                                ossUrl: "样式0路径"
                            ]
                    ]
            )
        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            def rptId = null
            Report.withNewSession {
                Report report = Report.findByCode("JCYCD")
                rptId = report.id
            }

            controller.getReportStyleByRptId(rptId)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"

            def list = jsonData.list
            assert list?.size() == 2
            def style = list.get(0)
            assert style.rptId == rptId
            assert style.scene == 0
            assert style.fileUrl == "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt"
            assert style.visitUrl == "样式0路径"
            assert style.comment == null
            assert style.chart == null

            def style1 = list.get(1)
            assert style1.rptId == rptId
            assert style1.scene == 1
            assert style1.fileUrl == "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/6a04cb4e3ed9420aa1f1c881650325ff.xslt"
            assert style1.visitUrl == "样式1路径"
            assert style1.comment == null
            assert style1.chart == null
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
    void "获取指定报表的样式列表-测试2"(){

        given:
        def fileBuilder = new PactBuilder()
        fileBuilder {
            serviceConsumer "FILE-SERVICE" 	// Define the service consumer by name
            hasPactWith "FILE-SERVICE"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to use a random one
        }
        fileBuilder{
        }
        when:
        def result = fileBuilder.runTest { mockServer ->
            def fileApi = Feign.builder()
                    .client(new ApacheHttpClient())
                    .decoder(new GsonDecoder())
                    .target(IFileService.class, mockServer.url as String)

            controller.fileService = fileApi

            controller.getReportStyleByRptId(null)

            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "报表标识不能为空"
        }

        then:
        result == PactVerificationResult.Ok.INSTANCE
    }
}

package com.zcareze.report.integrationtest

import com.zcareze.report.Report
import com.zcareze.report.ReportGrantTo
import com.zcareze.report.ReportGrantToController
import com.zcareze.report.ReportGroups
import com.zcareze.report.ReportInputs
import com.zcareze.report.ReportOpenTo
import com.zcareze.report.ReportStyle
import com.zcareze.report.ReportTables
import com.zcareze.report.ReportUsually
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.web.json.JSONElement
import spock.lang.Specification

@Integration
@Rollback
//@Ignore
class ReportGrantToControllerSpec extends Specification implements ControllerUnitTest<ReportGrantToController> {
    def setup() {
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

        new ReportGrantTo(rpt: report1, orgId: "2", roles: "03;04;11", manage: 1, granter: "王").save()
        new ReportGrantTo(rpt: report1, orgId: "3", roles: "01;02;11", manage: 1, granter: "王").save()

        new ReportGrantTo(rpt: report2, orgId: "4", roles: "01;02;03;06;11", manage: 1, granter: "王").save()
        new ReportGrantTo(rpt: report2, orgId: "2", roles: "01;11", manage: 1, granter: "王").save()
        new ReportGrantTo(rpt: report2, orgId: "3", roles: "11;02", manage: 0, granter: "王").save()
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

    void accessParam(params, param) {
        params["rptId"] = param.rptId
        params["orgId"] = param.orgId
        params["roles"] = param.roles
        params["manage"] = param.manage
    }
    void "新增报表授权-测试1"(){
        given:"参数"
            Report report = Report.findByCode("KZRZB")
            def reportId = report.id
            def param = [rptId:reportId, orgId:"1", roles: "11", manage: 1]
            accessParam(controller.params, param)
        when:"执行"
            controller.addReportGrantTo()
        then:"结果"
            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
        when: "验证"
            ReportGrantTo grantTo = ReportGrantTo.createCriteria().get {
                and{
                    rpt{
                        eq("id", reportId)
                    }
                    eq("orgId", "1")
                }
            }
        then: "验证结果"
            assert grantTo
            assert grantTo.rpt.id == reportId
            assert grantTo.orgId == "1"
            assert grantTo.roles == "11"
            assert grantTo.manage == 1
    }

    void "新增报表授权-测试2"(){
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def param = [rptId:reportId, orgId:"2", roles: "11", manage: 1]
        accessParam(controller.params, param)
        when:"执行"
        controller.addReportGrantTo()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "[报表授权]类的属性[orgId]的值[2]必须是唯一的;"
    }

    void "新增报表授权-测试3"(){
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def param = [rptId:reportId, orgId:"1", roles: "11", manage: 3]
        accessParam(controller.params, param)
        when:"执行"
        controller.addReportGrantTo()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "[报表授权]类的属性[manage]的值[3]不在列表的取值范围内;"
    }

    void "新增报表授权-测试4"(){
        given:"参数"
        def reportId = null
        def param = [rptId:reportId, orgId:"1", roles: "11", manage: 1]
        accessParam(controller.params, param)
        when:"执行"
        controller.addReportGrantTo()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "报表标识不能为空"
    }

    void "新增报表授权-测试5"(){
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def param = [rptId:reportId, orgId:"1", roles: "", manage: 1]
        accessParam(controller.params, param)
        when:"执行"
        controller.addReportGrantTo()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "授权角色不能为空"
    }

    void "新增报表授权-测试6"(){
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def param = [rptId:reportId, orgId:"", roles: "11", manage: 1]
        accessParam(controller.params, param)
        when:"执行"
        controller.addReportGrantTo()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "[报表授权]类的属性[orgId]不能为null;"
    }

    void "新增报表授权-测试7"(){
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def param = [rptId:reportId, orgId:"1", roles: "01;02", manage: 1]
        accessParam(controller.params, param)
        when:"执行"
        controller.addReportGrantTo()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "只有业务管理可以设置管理权"
    }

    void "新增报表授权-测试8"(){
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def param = [rptId:reportId]
        accessParam(controller.params, param)
        when:"执行"
        controller.addReportGrantTo()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "授权角色不能为空"
    }

    void "删除报表授权-测试1"(){
        given:"参数"
            Report report = Report.findByCode("KZRZB")
            def reportId = report.id
            def orgId = "2"
        when:"执行"
            controller.deleteReportGrantTo(reportId, orgId)
        then:"结果"
            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
        when: "验证"
            ReportGrantTo grantTo = ReportGrantTo.createCriteria().get {
                and{
                    rpt{
                        eq("id", reportId)
                    }
                    eq("orgId", orgId)
                }
            }
        then: "验证结果"
            assert grantTo == null
    }

    void "删除报表授权-测试2"(){
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def orgId = ""
        when:"执行"
        controller.deleteReportGrantTo(reportId, orgId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "报表和组织机构标识不能为空"
    }

    void "删除报表授权-测试3"(){
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = null
        def orgId = "2"
        when:"执行"
        controller.deleteReportGrantTo(reportId, orgId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "报表和组织机构标识不能为空"
    }

    void "删除报表授权-测试4"(){
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def orgId = "1"
        when:"执行"
        controller.deleteReportGrantTo(reportId, orgId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "授权记录不存在"
    }
}

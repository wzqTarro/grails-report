package com.zcareze.report.integrationtest

import com.zcareze.report.Report
import com.zcareze.report.ReportGrantTo
import com.zcareze.report.ReportGroups
import com.zcareze.report.ReportInputs
import com.zcareze.report.ReportOpenTo
import com.zcareze.report.ReportOpenToController
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
class ReportOpenToControllerSpec extends Specification implements ControllerUnitTest<ReportOpenToController> {
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

        new ReportOpenTo(rpt: report1, orgTreeId: "1", orgTreeLayer: 1, roles: "01").save()
        new ReportOpenTo(rpt: report2, orgTreeId: "0", orgTreeLayer: 1, roles: "01;02").save()
        new ReportOpenTo(rpt: report2, orgTreeId: "1", orgTreeLayer: 1, roles: "11").save()
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
    void "保存开放记录-测试1"() {
        given:"参数"
            Report report = Report.findByCode("KZRZB")
            ReportOpenTo openTo = new ReportOpenTo(rpt: report, orgTreeId: "1", orgTreeLayer: 2, roles: "01")
        when:"执行"
            controller.saveReportOpenTo(openTo)
        then:"结果"
            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData instanceof JSONElement
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
        when:"验证"
            ReportOpenTo actualOpenTo = ReportOpenTo.createCriteria().get {
                and{
                    rpt{
                        eq("id", report.id)
                    }
                    eq("orgTreeId", "1")
                    eq("orgTreeLayer", 2)
                }
            }
        then:"验证结果"
            assert actualOpenTo
            assert actualOpenTo.rpt.id == report.id
            assert actualOpenTo.orgTreeId == "1"
            assert actualOpenTo.orgTreeLayer == 2
            assert actualOpenTo.roles == "01"
    }
    void "保存开放记录-测试2"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        ReportOpenTo openTo = new ReportOpenTo(rpt: report, orgTreeId: "1", orgTreeLayer: 2, roles: "")
        when:"执行"
        controller.saveReportOpenTo(openTo)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "[开放记录]类的属性[roles]不能为null;"
    }
    void "保存开放记录-测试3"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        ReportOpenTo openTo = new ReportOpenTo(rpt: report, orgTreeId: "1", orgTreeLayer: -1, roles: "01")
        when:"执行"
        controller.saveReportOpenTo(openTo)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "[开放记录]类的属性[orgTreeLayer]的值[-1]比最小值 [0]还小;"
    }
    void "保存开放记录-测试4"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        ReportOpenTo openTo = new ReportOpenTo(rpt: report, orgTreeId: "1", orgTreeLayer: null, roles: "01")
        when:"执行"
        controller.saveReportOpenTo(openTo)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "[开放记录]类的属性[orgTreeLayer]不能为null;[开放记录]类的属性[orgTreeId]的值[1]必须是唯一的;"
    }
    void "保存开放记录-测试5"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        ReportOpenTo openTo = new ReportOpenTo(rpt: report, orgTreeId: "1", orgTreeLayer: 1, roles: "01")
        when:"执行"
        controller.saveReportOpenTo(openTo)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "[开放记录]类的属性[orgTreeId]的值[1]必须是唯一的;"
    }
    void "保存开放记录-测试6"() {
        given:"参数"
        ReportOpenTo openTo = new ReportOpenTo(rpt: null, orgTreeId: "1", orgTreeLayer: 0, roles: "01")
        when:"执行"
        controller.saveReportOpenTo(openTo)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "[开放记录]类的属性[rpt]不能为null;"
    }
    void "保存开放记录-测试7"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        report.id = null
        ReportOpenTo openTo = new ReportOpenTo(rpt: report, orgTreeId: "1", orgTreeLayer: 0, roles: "01")
        when:"执行"
        controller.saveReportOpenTo(openTo)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }
    void "保存开放记录-测试8"() {
        given:"参数"
        ReportOpenTo openTo = null
        when:"执行"
        controller.saveReportOpenTo(openTo)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "数据不能为空"
    }

    void "保存开放记录-测试9"() {
        given:"参数"
        Report report = new Report()
        ReportOpenTo openTo = new ReportOpenTo(rpt: report, orgTreeId: "1", orgTreeLayer: 0, roles: "01")
        when:"执行"
        controller.saveReportOpenTo(openTo)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "保存开放记录-测试10"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        ReportOpenTo openTo = new ReportOpenTo(rpt: report, orgTreeId: null, orgTreeLayer: 0, roles: "01")
        when:"执行"
        controller.saveReportOpenTo(openTo)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "未指定组织，只能从第1级开始授权"
    }

    void "删除开放记录-测试1"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def orgTreeId = "1"
        def orgTreeLayer = 1
        ReportOpenTo openTo = ReportOpenTo.createCriteria().get {
            and{
                rpt{
                    eq("id", reportId)
                }
                eq("orgTreeId", "1")
                eq("orgTreeLayer", 1)
            }
        }
        when:"执行"
        controller.deleteReportOpenTo(openTo.id)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        ReportOpenTo actualOpenTo = ReportOpenTo.createCriteria().get {
            and{
                rpt{
                    eq("id", reportId)
                }
                eq("orgTreeId", "1")
                eq("orgTreeLayer", 1)
            }
        }
        then:"验证结果"
        assert actualOpenTo == null
    }

    void "删除开放记录-测试2"() {
        given:"参数"
            def openId = null
        when:"执行"
        controller.deleteReportOpenTo(openId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "参数不能为空"
    }

    void "删除开放记录-测试3"() {
        given:"参数"
        def openId = "1"
        when:"执行"
        controller.deleteReportOpenTo(openId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData instanceof JSONElement
        assert jsonData.code == 3
        assert jsonData.message == "开放记录不存在"
    }
}

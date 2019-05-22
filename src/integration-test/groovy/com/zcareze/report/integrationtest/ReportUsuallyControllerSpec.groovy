package com.zcareze.report.integrationtest

import com.zcareze.report.Report
import com.zcareze.report.ReportGrantTo
import com.zcareze.report.ReportGroups
import com.zcareze.report.ReportInputs
import com.zcareze.report.ReportOpenTo
import com.zcareze.report.ReportStyle
import com.zcareze.report.ReportUsually
import com.zcareze.report.ReportUsuallyController
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

@Integration
@Rollback
//@Ignore
class ReportUsuallyControllerSpec extends Specification implements ControllerUnitTest<ReportUsuallyController> {
    def setup() {
        def group1 = new ReportGroups(code:"01", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表");
        def group2 = new ReportGroups(code:"02", name:"服务管理", comment:"有关服务工作开展情况和开展内容等信息的呈现");
        def group3 = new ReportGroups(code:"99", name:"监控大屏", comment:"内置专门存放监控大屏报表的分组");
        group1.save();
        group2.save();
        group3.save();

        def report1 = new Report(code: "KZRZB", name: "科主任周报", grpCode: group1.code, runway: 1, cloudId: "1");
        def report2 = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: group2.code, runway: 2, cloudId: "2");
        def report3 = new Report(code: "TZYB", name: "医生团队月报", grpCode: group1.code, runway: 1, cloudId: "1");
        report1.save()
        report2.save()
        report3.save(flush:true)

        new ReportUsually(staffId: "1", rpt: report1, seqNum: 1, cloudId: "1").save()
        new ReportUsually(staffId: "1", rpt: report2, seqNum: 3, cloudId: "2").save();

        new ReportUsually(staffId: "2", rpt: report1, seqNum: 1, cloudId: "1").save();
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
    void "添加我的常用报表-测试1"() {
        given:"参数"
            Report report = Report.findByCode("TZYB")
            def reportId = report.id

            def cloudId = "1"
        when:"执行"
            controller.cloudId = cloudId
            controller.addMyUsuallyReport(reportId)
        then:"结果"
            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
        when:"验证"
            ReportUsually usually = ReportUsually.createCriteria().get {
                and {
                    rpt{
                        eq("id", reportId)
                    }
                    eq("staffId", "1")
                }
            }
        then:"验证结果"
            assert usually
            assert usually.rpt.id == reportId
            assert usually.staffId == "1"
            assert usually.seqNum == 2
            assert usually.readTime
            assert usually.cloudId == "1"
    }
    void "添加我的常用报表-测试1-1"() {
        given:"参数"
        Report report = Report.findByCode("TZYB")
        def reportId = report.id
        def cloudId = "2"
        when:"执行"
        controller.cloudId = cloudId
        controller.addMyUsuallyReport(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        ReportUsually usually = ReportUsually.createCriteria().get {
            and {
                rpt{
                    eq("id", reportId)
                }
                eq("staffId", "1")
            }
        }
        then:"验证结果"
        assert usually
        assert usually.rpt.id == reportId
        assert usually.staffId == "1"
        assert usually.seqNum == 4
        assert usually.readTime
        assert usually.cloudId == "2"
    }
    void "添加我的常用报表-测试2"() {
        given:"参数"
        def reportId = null
        when:"执行"
        controller.addMyUsuallyReport(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "参数不能为空"
    }
    void "添加我的常用报表-测试3"() {
        given:"参数"
        def reportId = "1"
        when:"执行"
        controller.addMyUsuallyReport(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }
    void "添加我的常用报表-测试4"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def cloudId = "1"
        when:"执行"
        controller.cloudId = cloudId
        controller.addMyUsuallyReport(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[我的常用报表]类的属性[staffId]的值[1]必须是唯一的;"
    }
    void "取消我的常用报表-测试1"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        when:"执行"
        controller.cancelMyUsuallyReport(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        ReportUsually usually = ReportUsually.createCriteria().get {
            and {
                rpt{
                    eq("id", reportId)
                }
                eq("staffId", "1")
            }
        }
        then:"验证结果"
        assert usually == null
    }
    void "取消我的常用报表-测试2"() {
        given:"参数"
        def reportId = null
        when:"执行"
        controller.cancelMyUsuallyReport(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "参数不能为空"
    }
    void "取消我的常用报表-测试3"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = "1"
        when:"执行"
        controller.cancelMyUsuallyReport(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "常用报表不存在"
    }
    void "阅览我的常用报表-测试1"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        ReportUsually usually = ReportUsually.createCriteria().get {
            and {
                rpt{
                    eq("id", reportId)
                }
                eq("staffId", "1")
            }
        }
        def readTime = usually.readTime
        when:"执行"
        controller.readMyUsuallyReport(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        ReportUsually actualUsually = ReportUsually.createCriteria().get {
            and {
                rpt{
                    eq("id", reportId)
                }
                eq("staffId", "1")
            }
        }
        def actualReadTime = actualUsually.readTime
        then:"验证结果"
        assert actualReadTime
        assert actualReadTime.after(readTime)
    }
    void "阅览我的常用报表-测试2"() {
        given:"参数"
        def reportId = null
        when:"执行"
        controller.readMyUsuallyReport(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "参数不能为空"
    }
    void "阅览我的常用报表-测试3"() {
        given:"参数"
        def reportId = "1"
        when:"执行"
        controller.readMyUsuallyReport(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "常用报表不存在"
    }
    void "api阅览我的常用报表-测试1"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        ReportUsually usually = ReportUsually.createCriteria().get {
            and {
                rpt{
                    eq("id", reportId)
                }
                eq("staffId", "1")
            }
        }
        def readTime = usually.readTime
        when:"执行"
        controller.readMyUsuallyReportOld(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.errcode == 0

        when:"验证"
        ReportUsually actualUsually = ReportUsually.createCriteria().get {
            and {
                rpt{
                    eq("id", reportId)
                }
                eq("staffId", "1")
            }
        }
        def actualReadTime = actualUsually.readTime
        then:"验证结果"
        assert actualReadTime
        assert actualReadTime.after(readTime)
    }
    void "api阅览我的常用报表-测试2"() {
        given:"参数"
        def reportId = null
        when:"执行"
        controller.readMyUsuallyReportOld(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.errcode == 3
        assert jsonData.errmsg == "参数不能为空"
    }
    void "api阅览我的常用报表-测试3"() {
        given:"参数"
        def reportId = "1"
        when:"执行"
        controller.readMyUsuallyReportOld(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.errcode == 3
        assert jsonData.errmsg == "常用报表不存在"
    }
}

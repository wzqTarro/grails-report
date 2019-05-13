package com.zcareze.report.integrationtest

import com.zcareze.report.Report
import com.zcareze.report.ReportGrantTo
import com.zcareze.report.ReportGroups
import com.zcareze.report.ReportInputs
import com.zcareze.report.ReportOpenTo
import com.zcareze.report.ReportStyle
import com.zcareze.report.ReportStyleController
import com.zcareze.report.ReportTables
import com.zcareze.report.ReportUsually
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Rollback
@Ignore
class ReportStyleControllerSpec extends Specification implements ControllerUnitTest<ReportStyleController>{
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

        new ReportStyle(rpt: report1, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt").save()

        new ReportStyle(rpt: report2, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt").save()
        new ReportStyle(rpt: report2, scene: 1, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/6a04cb4e3ed9420aa1f1c881650325ff.xslt").save()
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
        params["rptId"] = param.rpt
        params["scene"] = param.scene
        params["fileContent"] = param.fileContent
    }

    void "获取指定报表的样式列表-测试1"() {
        given:"参数"
            Report report = Report.findByCode("JCYCD")
            def rptId = report.id
        when:"执行"
            controller.getReportStyleByRptId(rptId)
        then:"结果"
        new ReportStyle(rpt: report2, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt").save()
        new ReportStyle(rpt: report2, scene: 1, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/6a04cb4e3ed9420aa1f1c881650325ff.xslt").save()
            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 2

            def data = list.get(0)
            assert data.rptId == rptId
            assert data.scene == 0
            assert data.fileUrl == "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt"

    }
}

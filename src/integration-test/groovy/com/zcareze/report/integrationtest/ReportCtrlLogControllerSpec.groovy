package com.zcareze.report.integrationtest

import com.report.enst.CtrlKindEnum
import com.report.enst.CtrlStatusEnum
import com.zcareze.report.Report
import com.zcareze.report.ReportCtrlLog
import com.zcareze.report.ReportCtrlLogController
import com.zcareze.report.ReportGroups
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

@Integration
@Rollback
class ReportCtrlLogControllerSpec extends Specification implements ControllerUnitTest<ReportCtrlLogController> {
    void setup() {
        // 分组
        def group1 = new ReportGroups(code:"01", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表");
        group1.save();

        //报表
        def report1 = new Report(code: "KZRZB", name: "科主任周报", grpCode: group1.code, runway: 1, ctrlStatus: CtrlStatusEnum.BZZCY.code)
        report1.save()
        def report2 = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: group1.code, runway: 2, cloudId: "1", ctrlStatus: CtrlStatusEnum.BZZCY.code)
        report2.save()
        def report3 = new Report(code: "TZYB", name: "医生团队月报", grpCode: group1.code, runway: 1, ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code);
        report3.save()
        def report4 = new Report(code: "YYYCD", name: "用药依从性统计", grpCode: group1.code, runway: 1, cloudId: "1", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code)
        report4.save()

        def report5 = new Report(code: "YPFX", name: "药品分析", grpCode: group1.code, runway: 1, cloudId: "1", ctrlStatus: CtrlStatusEnum.STOP_USING.code)
        report5.save()
        def report5CtrlLog = new ReportCtrlLog(report: report5, cloudId: "1", preStatus: CtrlStatusEnum.BZZCY.code, newStatus: CtrlStatusEnum.SUBMIT_AUDIT.code, ctrlKind: CtrlKindEnum.AUDIT.kind, logTime: new Date()-2, accountId: "1", accountName: "王")
        report5CtrlLog.save()
        def report5CtrlLog2 = new ReportCtrlLog(report: report5, cloudId: "1", preStatus: CtrlStatusEnum.SUBMIT_AUDIT.code, newStatus: CtrlStatusEnum.STOP_USING.code, ctrlKind: CtrlKindEnum.STOP_USING.kind, logTime: new Date()-1, accountId: "1", accountName: "王")
        report5CtrlLog2.save()


        def report6 = new Report(code: "QYRS", name: "区域人数", grpCode: group1.code, runway: 1, cloudId: "1", ctrlStatus: CtrlStatusEnum.STOP_USING.code)
        report6.save()
    }
    void cleanup() {
        ReportCtrlLog.executeUpdate("delete ReportCtrlLog")
        ReportGroups.executeUpdate("delete ReportGroups")
        Report.executeUpdate("delete Report")
    }
    void "提交审核-测试1"() {
        given:"参数"
            Report rpt = Report.findByCode("KZRZB")
            String reportId = rpt.id
        when:"执行"
            controller.submit(reportId)
        then:"结果"
            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
        when:"验证"
            Report actualReport = Report.findByCode("KZRZB")
            List<ReportCtrlLog> logList = ReportCtrlLog.createCriteria().list {
                report{
                    eq("id", reportId)
                }
                order "logTime", "desc"
            }
            ReportCtrlLog log = logList.get(0)
        then:"验证结果"
            assert actualReport.ctrlStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
            assert log
            assert log.cloudId == null
            assert log.ctrlKind == CtrlKindEnum.SUBMIT.kind
            assert log.preStatus == CtrlStatusEnum.BZZCY.code
            assert log.newStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
    }
    void "提交审核-测试2"() {
        given:"参数"
        Report rpt = Report.findByCode("JCYCD")
        String reportId = rpt.id
        when:"执行"
        controller.submit(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCode("JCYCD")
        List<ReportCtrlLog> logList = ReportCtrlLog.createCriteria().list {
            report{
                eq("id", reportId)
            }
            order "logTime", "desc"
        }
        ReportCtrlLog log = logList.get(0)
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log
        assert log.cloudId == "1"
        assert log.ctrlKind == CtrlKindEnum.SUBMIT.kind
        assert log.preStatus == CtrlStatusEnum.BZZCY.code
        assert log.newStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
    }
    void "提交审核-测试3"() {
        given:"参数"
        String reportId = null
        when:"执行"
        controller.submit(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表标识不能为空"
    }
    void "提交审核-测试4"() {
        given:"参数"
        String reportId = "1"
        when:"执行"
        controller.submit(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }
    void "提交审核-测试5"() {
        given:"参数"
        Report rpt = Report.findByCode("TZYB")
        String reportId = rpt.id
        when:"执行"
        controller.submit(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "当前报表不是草稿状态"
    }
    void "停用-测试1"() {
        given:"参数"
        Report rpt = Report.findByCode("TZYB")
        String reportId = rpt.id
        String adscript = "停用理由"
        when:"执行"
        controller.stop(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCode("TZYB")
        List<ReportCtrlLog> logList = ReportCtrlLog.createCriteria().list {
            report{
                eq("id", reportId)
            }
            order "logTime", "desc"
        }
        ReportCtrlLog log = logList.get(0)
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.STOP_USING.code
        assert log
        assert log.cloudId == null
        assert log.ctrlKind == CtrlKindEnum.STOP_USING.kind
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.STOP_USING.code
        assert log.adscript == adscript
    }
    void "停用-测试2"() {
        given:"参数"
        Report rpt = Report.findByCode("YYYCD")
        String reportId = rpt.id
        String adscript = "停用理由"
        when:"执行"
        controller.stop(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCode("YYYCD")
        List<ReportCtrlLog> logList = ReportCtrlLog.createCriteria().list {
            report{
                eq("id", reportId)
            }
            order "logTime", "desc"
        }
        ReportCtrlLog log = logList.get(0)
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.STOP_USING.code
        assert log
        assert log.cloudId == "1"
        assert log.ctrlKind == CtrlKindEnum.STOP_USING.kind
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.STOP_USING.code
        assert log.adscript == adscript
    }
    void "停用-测试3"() {
        given:"参数"
        String reportId = null
        String adscript = "停用理由"
        when:"执行"
        controller.stop(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表标识不能为空"
    }
    void "停用-测试4"() {
        given:"参数"
        String reportId = "1"
        String adscript = "停用理由"
        when:"执行"
        controller.stop(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }
    void "审核通过-测试1"() {
        given:"参数"
        Report rpt = Report.findByCode("TZYB")
        String reportId = rpt.id
        String adscript = "审核通过理由"
        when:"执行"
        controller.auditSuccess(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCode("TZYB")
        List<ReportCtrlLog> logList = ReportCtrlLog.createCriteria().list {
            report{
                eq("id", reportId)
            }
            order "logTime", "desc"
        }
        ReportCtrlLog log = logList.get(0)
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.AUDIT_SUCCESS.code
        assert log
        assert log.cloudId == null
        assert log.ctrlKind == CtrlKindEnum.AUDIT.kind
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.AUDIT_SUCCESS.code
        assert log.adscript == adscript
    }
    void "审核通过-测试2"() {
        given:"参数"
        Report rpt = Report.findByCode("YYYCD")
        String reportId = rpt.id
        String adscript = "审核通过理由"
        when:"执行"
        controller.auditSuccess(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCode("YYYCD")
        List<ReportCtrlLog> logList = ReportCtrlLog.createCriteria().list {
            report{
                eq("id", reportId)
            }
            order "logTime", "desc"
        }
        ReportCtrlLog log = logList.get(0)
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.AUDIT_SUCCESS.code
        assert log
        assert log.cloudId == "1"
        assert log.ctrlKind == CtrlKindEnum.AUDIT.kind
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.AUDIT_SUCCESS.code
        assert log.adscript == adscript
    }
    void "审核通过-测试3"() {
        given:"参数"
        String reportId = null
        String adscript = "审核通过理由"
        when:"执行"
        controller.auditSuccess(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表标识不能为空"
    }
    void "审核通过-测试4"() {
        given:"参数"
        String reportId = "1"
        String adscript = "审核通过理由"
        when:"执行"
        controller.auditSuccess(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }
    void "审核未通过-测试1"() {
        given:"参数"
        Report rpt = Report.findByCode("TZYB")
        String reportId = rpt.id
        String adscript = "审核未通过理由"
        when:"执行"
        controller.auditRollback(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCode("TZYB")
        List<ReportCtrlLog> logList = ReportCtrlLog.createCriteria().list {
            report{
                eq("id", reportId)
            }
            order "logTime", "desc"
        }
        ReportCtrlLog log = logList.get(0)
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.AUDIT_ERROR.code
        assert log
        assert log.cloudId == null
        assert log.ctrlKind == CtrlKindEnum.AUDIT.kind
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.AUDIT_ERROR.code
        assert log.adscript == adscript
    }
    void "审核未通过-测试2"() {
        given:"参数"
        Report rpt = Report.findByCode("YYYCD")
        String reportId = rpt.id
        String adscript = "审核未通过理由"
        when:"执行"
        controller.auditRollback(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCode("YYYCD")
        List<ReportCtrlLog> logList = ReportCtrlLog.createCriteria().list {
            report{
                eq("id", reportId)
            }
            order "logTime", "desc"
        }
        ReportCtrlLog log = logList.get(0)
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.AUDIT_ERROR.code
        assert log
        assert log.cloudId == "1"
        assert log.ctrlKind == CtrlKindEnum.AUDIT.kind
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.AUDIT_ERROR.code
        assert log.adscript == adscript
    }
    void "审核未通过-测试3"() {
        given:"参数"
        String reportId = null
        String adscript = "审核未通过理由"
        when:"执行"
        controller.auditRollback(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表标识不能为空"
    }
    void "审核未通过-测试4"() {
        given:"参数"
        String reportId = "1"
        String adscript = "审核未通过理由"
        when:"执行"
        controller.auditRollback(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }
    void "重启用-测试1"() {
        given:"参数"
        Report rpt = Report.findByCode("YPFX")
        String reportId = rpt.id
        String adscript = "重启用理由"
        when:"执行"
        controller.reboot(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCode("YPFX")
        List<ReportCtrlLog> logList = ReportCtrlLog.createCriteria().list {
            report{
                eq("id", reportId)
            }
            order "logTime", "desc"
        }
        ReportCtrlLog log = logList.get(0)
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log
        assert log.cloudId == "1"
        assert log.ctrlKind == CtrlKindEnum.AUDIT.kind
        assert log.preStatus == CtrlStatusEnum.STOP_USING.code
        assert log.newStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.adscript == adscript
    }
    void "重启用-测试2"() {
        given:"参数"
        Report rpt = Report.findByCode("YYYCD")
        String reportId = rpt.id
        String adscript = "重启用理由"
        when:"执行"
        controller.reboot(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表未停用"
    }
    void "重启用-测试3"() {
        given:"参数"
        String reportId = null
        String adscript = "重启用理由"
        when:"执行"
        controller.reboot(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表标识不能为空"
    }
    void "重启用-测试4"() {
        given:"参数"
        String reportId = "1"
        String adscript = "重启用理由"
        when:"执行"
        controller.reboot(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }
    void "重启用-测试5"() {
        given:"参数"
        Report rpt = Report.findByCode("QYRS")
        String reportId = rpt.id
        String adscript = "重启用理由"
        when:"执行"
        controller.reboot(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "停用记录为空，数据有误"
    }
    void "区域转中心-测试1"() {
        given:"参数"
        Report rpt = Report.findByCode("YYYCD")
        String cloudId = rpt.cloudId
        String reportId = rpt.id
        String adscript = "区域转中心理由"
        when:"执行"
        controller.toCenterReport(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCode("YYYCD")
        List<ReportCtrlLog> logList = ReportCtrlLog.createCriteria().list {
            report{
                eq("id", reportId)
            }
            order "logTime", "desc"
        }
        ReportCtrlLog log = logList.get(0)
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert actualReport.cloudId == null
        assert log
        assert log.cloudId == cloudId
        assert log.ctrlKind == CtrlKindEnum.TO_CENTER.kind
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.adscript == adscript
    }
    void "区域转中心-测试2"() {
        given:"参数"
        String reportId = null
        String adscript = "区域转中心理由"
        when:"执行"
        controller.toCenterReport(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表标识不能为空"
    }
    void "区域转中心-测试3"() {
        given:"参数"
        String reportId = "1"
        String adscript = "区域转中心理由"
        when:"执行"
        controller.toCenterReport(reportId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }
    void "取消中心共享-测试1"() {
        given:"参数"
        Report rpt = Report.findByCode("TZYB")
        String reportId = rpt.id
        String cloudId = "2"
        String adscript = "取消中心共享理由"
        when:"执行"
        controller.cancelCenterReport(reportId, cloudId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCode("TZYB")
        List<ReportCtrlLog> logList = ReportCtrlLog.createCriteria().list {
            report{
                eq("id", reportId)
            }
            order "logTime", "desc"
        }
        ReportCtrlLog log = logList.get(0)
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert actualReport.cloudId == cloudId
        assert log
        assert log.cloudId == cloudId
        assert log.ctrlKind == CtrlKindEnum.CANCEL_CENTER.kind
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.adscript == adscript
    }
    void "取消中心共享-测试2"() {
        given:"参数"
        String reportId = null
        String cloudId = "2"
        String adscript = "取消中心共享理由"
        when:"执行"
        controller.cancelCenterReport(reportId, cloudId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表标识不能为空"
    }
    void "中心转区域-测试3"() {
        given:"参数"
        String reportId = "1"
        String cloudId = "2"
        String adscript = "取消中心共享理由"
        when:"执行"
        controller.cancelCenterReport(reportId, cloudId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }
    void "中心转区域-测试4"() {
        given:"参数"
        String reportId = "1"
        String cloudId = null
        String adscript = "取消中心共享理由"
        when:"执行"
        controller.cancelCenterReport(reportId, cloudId, adscript)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "区域云标识不能为空"
    }
    void "查询管控记录-测试1"() {
        given:"参数"
            Report report = Report.findByName("药品分析")
            String reportName = null
            String ctrlKind = null
            Integer pageNow = null
            Integer pageSize = null
        when:"执行"
            controller.getByCondition(reportName, ctrlKind, pageNow, pageSize)
        then:"结果"
            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData
            assert jsonData.code == 1
            def list = jsonData.list
            assert list?.size() == 2

            def log = list.get(0)
            assert log.report.id == report.id
            assert log.cloudId == "1"
            assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
            assert log.newStatus == CtrlStatusEnum.STOP_USING.code
            assert log.ctrlKind == CtrlKindEnum.STOP_USING.kind

            def log1 = list.get(1)
            assert log1.report.id == report.id
            assert log1.cloudId == "1"
            assert log1.preStatus == CtrlStatusEnum.BZZCY.code
            assert log1.newStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
            assert log1.ctrlKind == CtrlKindEnum.AUDIT.kind
    }
    void "查询管控记录-测试2"() {
        given:"参数"
        Report report = Report.findByName("药品分析")
        String reportName = "药品"
        String ctrlKind = CtrlKindEnum.STOP_USING.kind
        Integer pageNow = 0
        Integer pageSize = 1
        when:"执行"
        controller.getByCondition(reportName, ctrlKind, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def log = list.get(0)
        assert log.report.id == report.id
        assert log.cloudId == "1"
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.STOP_USING.code
        assert log.ctrlKind == CtrlKindEnum.STOP_USING.kind
    }
    void "查询管控记录-测试3"() {
        given:"参数"
        Report report = Report.findByName("药品分析")
        String reportName = "药品"
        String ctrlKind = CtrlKindEnum.TO_CENTER.kind
        Integer pageNow = 0
        Integer pageSize = 1
        when:"执行"
        controller.getByCondition(reportName, ctrlKind, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 0
    }
    void "查询管控记录-测试4"() {
        given:"参数"
        Report report = Report.findByName("药品分析")
        String reportName = "药品"
        String ctrlKind = null
        Integer pageNow = 0
        Integer pageSize = 1
        when:"执行"
        controller.getByCondition(reportName, ctrlKind, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 2

        def log = list.get(0)
        assert log.report.id == report.id
        assert log.cloudId == "1"
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.STOP_USING.code
        assert log.ctrlKind == CtrlKindEnum.STOP_USING.kind
    }
    void "查询管控记录-测试5"() {
        given:"参数"
        Report report = Report.findByName("药品分析")
        String reportName = null
        String ctrlKind = CtrlKindEnum.STOP_USING.kind
        Integer pageNow = 0
        Integer pageSize = 1
        when:"执行"
        controller.getByCondition(reportName, ctrlKind, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def log = list.get(0)
        assert log.report.id == report.id
        assert log.cloudId == "1"
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.STOP_USING.code
        assert log.ctrlKind == CtrlKindEnum.STOP_USING.kind
    }
}

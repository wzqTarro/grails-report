package com.zcareze.report.integrationtest

import com.report.enst.CtrlStatusEnum
import com.zcareze.report.*
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

@Integration//(applicationClass = statreports.Application.class)
@Rollback
//@Ignore
class ReportControllerSpec extends Specification implements ControllerUnitTest<ReportController> {

    def setupSpec() {
        grails.converters.JSON.registerObjectMarshaller(Date.class) {
            it.format("YYYY-MM-dd HH:mm:ss")
        }
    }

    def setup() {
        Report.withNewSession {
            def datasource = new ReportDatasource(code: "00", name: "中心数据源", config: '{"kind":1}')
            datasource.save()

            def group1 = new ReportGroups(code: "01", name: "运营简报", comment: "提现整体经营服务规模效果效益等内容的报表")
            def group2 = new ReportGroups(code: "02", name: "服务管理", comment: "有关服务工作开展情况和开展内容等信息的呈现")
            def group3 = new ReportGroups(code: "99", name: "监控大屏", comment: "内置专门存放监控大屏报表的分组")
            group1.save()
            group2.save()
            group3.save()

            def report = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: group1.code, runway: 2, cloudId: "1", ctrlStatus: CtrlStatusEnum.BZZCY.code)
            def report1 = new Report(code: "TZYB", name: "医生团队月报", grpCode: group2.code, runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.AUDIT_SUCCESS.code)
            // 大屏
            def report2 = new Report(code: "CS", name: "测试大屏", grpCode: group3.code, runway: 1, cloudId: "1")
            def report3 = new Report(code: "ZS", name: "正式大屏", grpCode: group3.code, runway: 2, cloudId: "2")
            report.save()
            report1.save()
            report3.save()
            report2.save(flush: true)

            new ReportInputs(rpt: report1, name: "orgid", caption: "机构", seqNum: 0, dataType: "31", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构").save()
            new ReportStyle(rpt: report1, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt").save()
            new ReportTables(dataSource: datasource, rpt: report1, name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1).save()
            new ReportGrantTo(rpt: report1, orgId: "1", roles: "11", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report1, orgId: "2", roles: "030411", manage: 1, granter: "王").save()
            new ReportGrantTo(rpt: report1, orgId: "3", roles: "010211", manage: 1, granter: "王").save()
            new ReportOpenTo(rpt: report1, orgTreeId: 1, orgTreeLayer: 1, roles: "01").save()

            new ReportUsually(staffId: "1", rpt: report1, seqNum: 3).save()
            new ReportUsually(staffId: "2", rpt: report1, seqNum: 2).save()

            def group = new ReportGroups(code: "06", name: "pc报表样式")
            group.save()
            def report4 = new Report(code: "0315", name: "建档情况分析表", runway: 1, grpCode: group.code, cloudId: "1", ctrlStatus: CtrlStatusEnum.BZZCY.code)
            report4.save(flush: true)

            new ReportStyle(rpt: report4, scene: 0, fileUrl: "asd", chart: "<chart name=\\\"chart1\\\" theme=\\\"walden\\\" tab=\\\"建档情况\\\"><type>bar</type><title>机构建档情况</title><x_col>机构名称</x_col><y_col><field>建档数</field><name>建档数</name></y_col></chart>").save()
            new ReportTables(dataSource: datasource, name: "建档情况", rpt: report4, seqNum: 1, sqlText: "select ol.`name` 机构名称, COUNT(rl.`id`) 建档数 FROM org_list ol INNER JOIN `resident_list` rl on rl.`org_id`= ol.`id` where ol.`id` = [orgID] and rl.`commit_time` BETWEEN '2019-03-01' and '2019-03-30' GROUP BY ol.`id`").save()
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
            ReportDatasource.executeUpdate("delete ReportDatasource")
        }
    }

    void "新增报表-测试1"() {
        given: "参数"
        Report report = new Report(code: "KZRZB", name: "科主任周报", runway: 1, comment: "123")
        when: "执行新增方法"
        controller.addReportList(report)
        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        //this.messageSource.getMessage("default.x.x", [Report, ""], Locale.SIMPLIFIED_CHINESE)
        assert jsonData.message == "执行成功"
        when: "检测是否新增成功"
        Report testReport = Report.get(report.id)
        then: "返回结果"
        assert testReport
        assert testReport.code == "KZRZB"
        assert testReport.name == "科主任周报"
        assert testReport.runway == 1
        assert testReport.comment == "123"
        assert testReport.ctrlStatus == CtrlStatusEnum.BZZCY.code
    }

    void "新增报表-测试2"() {
        given: "参数"
//            StaticMessageSource msgSrc = this.messageSource
//            msgSrc.addMessage("report.code.nullable", Locale.CHINA, "编码不能为空")
        Report report = new Report(code: "", name: "科主任周报", runway: 1, comment: "")

        when: "执行新增方法"
        controller.addReportList(report)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        //assert jsonData.message == this.messageSource.getMessage("report.code.nullable", ["报表", "code"] as Object[], Locale.CHINA)+""
        assert jsonData.message == "[报表]类的属性[code]不能为null;"
    }

    void "新增报表-测试3"() {
        given: "参数"
//            StaticMessageSource msgSrc = this.messageSource
//            msgSrc.addMessage("report.code.nullable", Locale.CHINA, "编码不能为空")
        Report report = new Report(code: "KZRZB", name: "", runway: 1, comment: "")

        when: "执行新增方法"
        controller.addReportList(report)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        //assert jsonData.message == this.messageSource.getMessage("report.code.nullable", ["报表", "code"] as Object[], Locale.CHINA)+""
        assert jsonData.message == "[报表]类的属性[name]不能为null;"
    }

    void "新增报表-测试4"() {
        given: "参数"
//            StaticMessageSource msgSrc = this.messageSource
//            msgSrc.addMessage("report.code.nullable", Locale.CHINA, "编码不能为空")
        Report report = new Report(code: "KZRZB", name: "监测依从度统计", grpCode: "", runway: 2, comment: "")

        when: "执行新增方法"
        controller.cloudId = 1
        controller.addReportList(report)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        //assert jsonData.message == this.messageSource.getMessage("report.code.nullable", ["报表", "code"] as Object[], Locale.CHINA)+""
        assert jsonData.message == "[报表]类的属性[name]的值[监测依从度统计]必须是唯一的;"
    }

    void "新增报表-测试5"() {
        given: "参数"
//            StaticMessageSource msgSrc = this.messageSource
//            msgSrc.addMessage("report.code.nullable", Locale.CHINA, "编码不能为空")
        Report report = new Report(code: "JCYCD", name: "科主任周报", grpCode: "", runway: 2, comment: "")

        when: "执行新增方法"
        controller.cloudId = 1
        controller.addReportList(report)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        //assert jsonData.message == this.messageSource.getMessage("report.code.nullable", ["报表", "code"] as Object[], Locale.CHINA)+""
        assert jsonData.message == "[报表]类的属性[code]的值[JCYCD]必须是唯一的;"
    }

    void "新增报表-测试6"() {
        given: "参数"
        Report report = new Report(code: "KZRZB", name: "科主任周报", runway: 1, comment: "123", cloudId: "1")

        when: "执行新增方法"
        controller.addReportList(report)
        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        //this.messageSource.getMessage("default.x.x", [Report, ""], Locale.SIMPLIFIED_CHINESE)
        assert jsonData.message == "执行成功"
        when: "检测是否新增成功"
        Report testReport = Report.get(report.id)
        then: "返回结果"
        assert testReport
        assert testReport.code == "KZRZB"
        assert testReport.name == "科主任周报"
        assert testReport.runway == 1
        assert testReport.comment == "123"
        assert testReport.cloudId == "1"
    }

    void "新增报表-测试7"() {
        given: "参数"
//            StaticMessageSource msgSrc = this.messageSource
//            msgSrc.addMessage("report.code.nullable", Locale.CHINA, "编码不能为空")
        Report report = new Report(code: "KZRZB", name: "监测依从度统计", grpCode: "", runway: 2, comment: "", cloudId: "2")

        when: "执行新增方法"
        controller.addReportList(report)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        //this.messageSource.getMessage("default.x.x", [Report, ""], Locale.SIMPLIFIED_CHINESE)
        assert jsonData.message == "执行成功"
        when: "检测是否新增成功"
        Report testReport = Report.get(report.id)
        then: "返回结果"
        assert testReport
        assert testReport.code == "KZRZB"
        assert testReport.name == "监测依从度统计"
        assert testReport.runway == 2
        assert testReport.comment == null
        assert testReport.cloudId == "2"
    }

    void "新增报表-测试8"() {
        given: "参数"
//            StaticMessageSource msgSrc = this.messageSource
//            msgSrc.addMessage("report.code.nullable", Locale.CHINA, "编码不能为空")
        Report report = new Report(code: "JCYCD", name: "科主任周报", grpCode: "", runway: 2, comment: "", cloudId: "2")

        when: "执行新增方法"
        controller.addReportList(report)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        //this.messageSource.getMessage("default.x.x", [Report, ""], Locale.SIMPLIFIED_CHINESE)
        assert jsonData.message == "执行成功"
        when: "检测是否新增成功"
        Report testReport = Report.get(report.id)
        then: "返回结果"
        assert testReport
        assert testReport.code == "JCYCD"
        assert testReport.name == "科主任周报"
        assert testReport.runway == 2
        assert testReport.comment == null
        assert testReport.cloudId == "2"
    }

    void "修改报表-测试1"() {
        given: "参数"
//            StaticMessageSource msgSrc = this.messageSource
//            msgSrc.addMessage("report.code.nullable", Locale.CHINA, "编码不能为空")
        Report report = Report.findByName("监测依从度统计")

        String reportId = report.id
        Report editReport = new Report(code: "KZRZB", name: "科主任周报", grpCode: "", runway: 2, comment: "", cloudId: "1")
        editReport.id = reportId

        when: "执行修改方法"
        controller.editReportList(editReport)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        //assert jsonData.message == this.messageSource.getMessage("report.code.nullable", ["报表", "code"] as Object[], Locale.CHINA)+""
        assert jsonData.message == "执行成功"

        when: "验证报表是否修改"
        Report testReport = Report.withTenant("1") {
            Report.findByName("科主任周报")
        }
        Report orginReport = Report.withTenant("1") {
            Report.findByName("监测依从度统计")
        }
        then: "验证结果"
        assert orginReport == null
        assert testReport
        assert testReport.code == "KZRZB"
        assert testReport.name == "科主任周报"
        assert testReport.grpCode == null
        assert testReport.runway == 2
        assert testReport.comment == null
    }

    void "修改报表-测试2"() {
        given: "参数"
//            StaticMessageSource msgSrc = this.messageSource
//            msgSrc.addMessage("report.code.nullable", Locale.CHINA, "编码不能为空")
        Report report = Report.findByName("监测依从度统计")

        String reportId = report.id
        Report editReport = new Report(code: "TZYB", name: "科主任周报", grpCode: "", runway: 2, comment: "")
        editReport.id = reportId

        def cloudId = "2"
        when: "执行修改方法"
        controller.cloudId = cloudId
        controller.editReportList(editReport)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        //assert jsonData.message == this.messageSource.getMessage("report.code.nullable", ["报表", "code"] as Object[], Locale.CHINA)+""
        assert jsonData.message == "报表已存在"
    }

    void "修改报表-测试2-1"() {
        given: "参数"
//            StaticMessageSource msgSrc = this.messageSource
//            msgSrc.addMessage("report.code.nullable", Locale.CHINA, "编码不能为空")
        Report report = Report.findByName("监测依从度统计")

        String reportId = report.id
        Report editReport = new Report(code: "TZYB", name: "科主任周报", grpCode: "", runway: 2, comment: "", cloudId: "1")
        editReport.id = reportId

        when: "执行修改方法"
        controller.editReportList(editReport)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        //assert jsonData.message == this.messageSource.getMessage("report.code.nullable", ["报表", "code"] as Object[], Locale.CHINA)+""
        assert jsonData.message == "执行成功"

        when: "验证报表是否修改"
        Report testReport = Report.withTenant("1") {
            Report.findByName("科主任周报")
        }
        Report orginReport = Report.withTenant("1") {
            Report.findByName("监测依从度统计")
        }
        then: "验证结果"
        assert orginReport == null
        assert testReport
        assert testReport.code == "TZYB"
        assert testReport.name == "科主任周报"
        assert testReport.grpCode == null
        assert testReport.runway == 2
        assert testReport.comment == null
    }

    void "修改报表-测试3"() {
        given: "参数"
//            StaticMessageSource msgSrc = this.messageSource
//            msgSrc.addMessage("report.code.nullable", Locale.CHINA, "编码不能为空")
        Report report = Report.findByName("监测依从度统计")

        String reportId = report.id
        Report editReport = new Report(code: "KZRZB", name: "医生团队月报", grpCode: "", runway: 2, comment: "")
        editReport.id = reportId

        when: "执行修改方法"
        controller.editReportList(editReport)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        //assert jsonData.message == this.messageSource.getMessage("report.code.nullable", ["报表", "code"] as Object[], Locale.CHINA)+""
        assert jsonData.message == "报表已存在"
    }

    void "修改报表-测试3-1"() {
        given: "参数"
//            StaticMessageSource msgSrc = this.messageSource
//            msgSrc.addMessage("report.code.nullable", Locale.CHINA, "编码不能为空")
        Report report = Report.findByName("监测依从度统计")

        String reportId = report.id
        Report editReport = new Report(code: "KZRZB", name: "医生团队月报", grpCode: "", runway: 2, comment: "", cloudId: "1")
        editReport.id = reportId

        when: "执行修改方法"

        controller.editReportList(editReport)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        //assert jsonData.message == this.messageSource.getMessage("report.code.nullable", ["报表", "code"] as Object[], Locale.CHINA)+""
        assert jsonData.message == "执行成功"

        when: "验证报表是否修改"
        Report testReport = Report.withTenant("1") {
            Report.findByName("医生团队月报")
        }
        Report orginReport = Report.withTenant("1") {
            Report.findByName("监测依从度统计")
        }
        then: "验证结果"
        assert orginReport == null
        assert testReport
        assert testReport.code == "KZRZB"
        assert testReport.name == "医生团队月报"
        assert testReport.grpCode == null
        assert testReport.runway == 2
        assert testReport.comment == null
    }

    void "修改报表-测试4"() {
        given: "参数"
//            StaticMessageSource msgSrc = this.messageSource
//            msgSrc.addMessage("report.code.nullable", Locale.CHINA, "编码不能为空")
        Report report = Report.findByName("监测依从度统计")

        String reportId = report.id
        Report editReport = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: "", runway: 2, comment: "")
        editReport.id = reportId

        def cloudId = "2"
        when: "执行修改方法"
        controller.cloudId = cloudId
        controller.editReportList(editReport)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        //assert jsonData.message == this.messageSource.getMessage("report.code.nullable", ["报表", "code"] as Object[], Locale.CHINA)+""
        assert jsonData.message == "执行成功"

        when: "验证报表是否修改"
        Report testReport = Report.findByName("监测依从度统计")
        then: "验证结果"
        assert testReport
        assert testReport.code == "JCYCD"
        assert testReport.name == "监测依从度统计"
        assert testReport.grpCode == null
        assert testReport.runway == 2
        assert testReport.comment == null
    }

    void "修改报表-测试5"() {
        given: "参数"
        Report editReport = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: "", runway: 2, comment: "")
        when: "执行修改方法"
        controller.editReportList(editReport)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        //assert jsonData.message == this.messageSource.getMessage("report.code.nullable", ["报表", "code"] as Object[], Locale.CHINA)+""
        assert jsonData.message == "报表标识不能为空"
    }

    void "修改报表-测试6"() {
        given: "参数"
        Report report = Report.findByName("监测依从度统计")
        String reportId = report.id
        Report editReport = new Report(code: "JCYCD", name: "", grpCode: "", runway: 2, comment: "")
        editReport.id = reportId
        when: "执行修改方法"
        controller.editReportList(editReport)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表名称不能为空"
    }

    void "修改报表-测试7"() {
        given: "参数"
        Report report = Report.findByName("监测依从度统计")

        String reportId = report.id
        Report editReport = new Report(code: "", name: "监测依从度统计", grpCode: "", runway: 2, comment: "")
        editReport.id = reportId
        when: "执行修改方法"
        controller.editReportList(editReport)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表编码不能为空"
    }

    void "修改报表-测试8"() {
        given: "参数"
        Report editReport = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: "", runway: 2, comment: "")
        editReport.id = "1"
        when: "执行修改方法"
        controller.editReportList(editReport)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "删除报表-测试1"() {
        given: "参数"
        def cloudId = "1"
        Report report = Report.withTenant(cloudId) {
            Report.findByName("监测依从度统计")
        }
        String reportId = report.id
        when: "执行修改方法"
        controller.deleteReportList(reportId)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"

        when: "验证是否删除成功"
        Report actualReport = Report.get(reportId)
        List<ReportUsually> usuallyList = ReportUsually.createCriteria().list {
            rpt {
                eq("id", reportId)
            }
        }
        List<ReportInputs> inputsList = ReportInputs.createCriteria().list {
            rpt {
                eq("id", reportId)
            }
        }
        List<ReportTables> tableList = ReportTables.createCriteria().list {
            rpt {
                eq("id", reportId)
            }
        }
        List<ReportStyle> styleList = ReportStyle.createCriteria().list {
            rpt {
                eq("id", reportId)
            }
        }
        List<ReportGrantTo> grantToList = ReportGrantTo.createCriteria().list {
            rpt {
                eq("id", reportId)
            }
        }
        List<ReportOpenTo> openToList = ReportOpenTo.createCriteria().list {
            rpt {
                eq("id", reportId)
            }
        }
        then: "验证结果"
        assert actualReport == null
        assert usuallyList == null || usuallyList.size() == 0
        assert inputsList == null || inputsList.size() == 0
        assert tableList == null || tableList.size() == 0
        assert styleList == null || styleList.size() == 0
        assert grantToList == null || grantToList.size() == 0
        assert openToList == null || openToList.size() == 0
    }

    void "删除报表-测试2"() {
        given: "参数"
        String reportId = "1"
        when: "执行修改方法"
        controller.deleteReportList(reportId)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "删除报表-测试3"() {
        given: "参数"
        String reportId = null
        when: "执行修改方法"
        controller.deleteReportList(reportId)

        then: "返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表标识不能为空"
    }

    void "删除报表-测试4"() {
        given:"参数"
            Report report = Report.findByName("医生团队月报")
            String reportId = report.id
        when:"执行"
            controller.deleteReportList(reportId)
        then:"返回结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "只有草稿状态的报表才能删除"
    }

    void "获取指定报表信息-测试1"() {
        given: "参数"
        Report report = Report.findByName("监测依从度统计")
        def id = report.id
        when: "执行方法"
        controller.getReportListById(id)
        then: "结果"
        assert response.status == 200
        def data = response.json
        assert data.code == 1
        def one = data.one
        assert one
        assert one.code == "JCYCD"
        assert one.name == "监测依从度统计"
        assert one.grpCode == "01"
        assert one.runway == 2
        assert one.comment == null
    }

    void "获取指定报表信息-测试2"() {
        given: "参数"
        def id = "1"
        when: "执行方法"
        controller.getReportListById(id)
        then: "结果"
        assert response.status == 200
        def data = response.json
        assert data.code == 1
        def one = data.one
        assert one == null
    }

    void "获取指定报表信息-测试3"() {
        given: "参数"
        def id = ""
        when: "执行方法"
        controller.getReportListById(id)
        then: "结果"
        assert response.status == 200
        def data = response.json
        assert data.code == 3
        assert data.message == "报表标识不能为空"
    }

    void "获取报表清单-测试1"() {
        given: "参数"
        def name = ""
        def groupCode = ""
        def pageNow = null
        def pageSize = null
        def cloudId = "1"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getReportList(name, groupCode, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 2

        def data0 = list.get(0)
        assert data0.code == "0315"
        assert data0.name == "建档情况分析表"
        assert data0.grpCode == "06"
        assert data0.runway == 1
        assert data0.comment == null

        def data = list.get(1)
        assert data.code == "JCYCD"
        assert data.name == "监测依从度统计"
        assert data.grpCode == "01"
        assert data.runway == 2
        assert data.comment == null
    }

    void "获取报表清单-测试1-1"() {
        given: "参数"
        def name = ""
        def groupCode = ""
        def pageNow = null
        def pageSize = null
        def cloudId = "2"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getReportList(name, groupCode, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def data1 = list.get(0)
        assert data1.code == "TZYB"
        assert data1.name == "医生团队月报"
        assert data1.grpCode == "02"
        assert data1.runway == 1
        assert data1.comment == null
    }

    void "获取报表清单-测试2"() {
        given: "参数"
        def name = ""
        def groupCode = ""
        def pageNow = 0
        def pageSize = 1
        def cloudId = "1"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getReportList(name, groupCode, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def data = list.get(0)
        assert data.code == "0315"
        assert data.name == "建档情况分析表"
        assert data.grpCode == "06"
        assert data.runway == 1
        assert data.comment == null
    }

    void "获取报表清单-测试2-1"() {
        given: "参数"
        def name = ""
        def groupCode = ""
        def pageNow = 0
        def pageSize = 1
        def cloudId = "2"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getReportList(name, groupCode, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def data1 = list.get(0)
        assert data1.code == "TZYB"
        assert data1.name == "医生团队月报"
        assert data1.grpCode == "02"
        assert data1.runway == 1
        assert data1.comment == null
    }

    void "获取报表清单-测试3"() {
        given: "参数"
        def name = "生"
        def groupCode = ""
        def pageNow = null
        def pageSize = null
        def cloudId = "2"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getReportList(name, groupCode, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def data1 = list.get(0)
        assert data1.code == "TZYB"
        assert data1.name == "医生团队月报"
        assert data1.grpCode == "02"
        assert data1.runway == 1
        assert data1.comment == null
    }

    void "获取报表清单-测试3-1"() {
        given: "参数"
        def name = "生"
        def groupCode = ""
        def pageNow = null
        def pageSize = null
        def cloudId = "1"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getReportList(name, groupCode, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 0
    }

    void "获取报表清单-测试4"() {
        given: "参数"
        def name = ""
        def groupCode = "99"
        def pageNow = null
        def pageSize = null
        def cloudId = "1"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getReportList(name, groupCode, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def data = list.get(0)
        assert data.code == "CS"
        assert data.name == "测试大屏"
        assert data.grpCode == "99"
        assert data.runway == 1
        assert data.comment == null
    }

    void "获取报表清单-测试4-1"() {
        given: "参数"
        def name = ""
        def groupCode = "99"
        def pageNow = null
        def pageSize = null
        def cloudId = "2"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getReportList(name, groupCode, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def data1 = list.get(0)
        assert data1.code == "ZS"
        assert data1.name == "正式大屏"
        assert data1.grpCode == "99"
        assert data1.runway == 2
        assert data1.comment == null
    }

    void "获取报表清单-测试5"() {
        given: "参数"
        def name = "依从"
        def groupCode = "01"
        def pageNow = 0
        def pageSize = 1
        def cloudId = "1"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getReportList(name, groupCode, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def data = list.get(0)
        assert data.code == "JCYCD"
        assert data.name == "监测依从度统计"
        assert data.grpCode == "01"
        assert data.runway == 2
        assert data.comment == null
    }

    void "获取报表清单-测试5-1"() {
        given: "参数"
        def name = "依从"
        def groupCode = "01"
        def pageNow = 0
        def pageSize = 1
        def cloudId = "2"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getReportList(name, groupCode, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 0
    }

    void "获取监控大屏报表清单-测试1"() {
        given: "参数"
        def name = "测"
        def cloudId = "1"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getScreenReportList(name)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size() == 1

        def data = list.get(0)
        assert data.code == "CS"
        assert data.name == "测试大屏"
        assert data.grpCode == "99"
        assert data.runway == 1
        assert data.comment == null
    }

    void "获取监控大屏报表清单-测试1-1"() {
        given: "参数"
        def name = "测"
        def cloudId = "2"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getScreenReportList(name)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size() == 0
    }

    void "获取监控大屏报表清单-测试2"() {
        given: "参数"
        def name = ""
        def cloudId = "1"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getScreenReportList(name)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size() == 1

        def data = list.get(0)
        assert data.code == "CS"
        assert data.name == "测试大屏"
        assert data.grpCode == "99"
        assert data.runway == 1
        assert data.comment == null
    }

    void "获取监控大屏报表清单-测试2-1"() {
        given: "参数"
        def name = ""
        def cloudId = "2"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getScreenReportList(name)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size() == 1

        def data1 = list.get(0)
        assert data1.code == "ZS"
        assert data1.name == "正式大屏"
        assert data1.grpCode == "99"
        assert data1.runway == 2
        assert data1.comment == null
    }

    void "获取监控大屏报表清单-测试3"() {
        given: "参数"
        def name = "1"
        when: "执行方法"
        controller.getScreenReportList(name)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size() == 0
    }

    void "api获取监控大屏报表清单-测试1"() {
        given: "参数"
        def name = "测"
        def cloudId = "1"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getScreenReportListOld(name)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.errcode == 0

        def bizData = jsonData.bizData
        assert bizData
        def list = bizData.reportListVOs
        assert list?.size() == 1

        def data = list.get(0)
        assert data.code == "CS"
        assert data.name == "测试大屏"
        assert data.grpCode == "99"
        assert data.runway == 1
        assert data.comment == null
    }

    void "api获取监控大屏报表清单-测试1-1"() {
        given: "参数"
        def name = "测"
        def cloudId = "2"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getScreenReportListOld(name)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.errcode == 0

        def bizData = jsonData.bizData
        assert bizData
        def list = bizData.reportListVOs
        assert list?.size() == 0
    }

    void "api获取监控大屏报表清单-测试2"() {
        given: "参数"
        def name = ""
        def cloudId = "1"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getScreenReportListOld(name)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.errcode == 0

        def bizData = jsonData.bizData
        assert bizData
        def list = bizData.reportListVOs
        assert list?.size() == 1

        def data = list.get(0)
        assert data.code == "CS"
        assert data.name == "测试大屏"
        assert data.grpCode == "99"
        assert data.runway == 1
        assert data.comment == null
    }

    void "api获取监控大屏报表清单-测试2-1"() {
        given: "参数"
        def name = ""
        def cloudId = "2"
        when: "执行方法"
        controller.cloudId = cloudId
        controller.getScreenReportListOld(name)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.errcode == 0

        def bizData = jsonData.bizData
        assert bizData
        def list = bizData.reportListVOs
        assert list?.size() == 1

        def data1 = list.get(0)
        assert data1.code == "ZS"
        assert data1.name == "正式大屏"
        assert data1.grpCode == "99"
        assert data1.runway == 2
        assert data1.comment == null
    }

    void "api获取监控大屏报表清单-测试3"() {
        given: "参数"
        def name = "1"
        when: "执行方法"
        controller.getScreenReportListOld(name)
        then: "结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.errcode == 0

        def bizData = jsonData.bizData
        assert bizData
        def list = bizData.reportListVOs
        assert list?.size() == 0
    }

//    void "获取指定报表最后修改时间"() {
//        given: "参数"
//        Report report = Report.findByCode("JCYCD")
//        String reportId = report.id
//        when: "执行方法"
//        controller.getReportListUpdateTime(reportId)
//        then: "结果"
//        assert response.status == 200
//        def jsonData = response.json
//        assert jsonData.code == 1
//
//        def one = jsonData.one
//        assert one
//        assert Date.parse("yyyy-MM-dd HH:mm:ss", one) == Date.parse("yyyy-MM-dd HH:mm:ss", "2018-04-18 16:00:00")
//    }
//
//    void "获取指定报表最后修改时间-测试2"() {
//        given: "参数"
//        String reportId = ""
//        when: "执行方法"
//        controller.getReportListUpdateTime(reportId)
//        then: "结果"
//        assert response.status == 200
//        def jsonData = response.json
//        assert jsonData.code == 3
//        assert jsonData.message == "标识不能为空"
//    }
//
//    void "获取指定报表最后修改时间-测试3"() {
//        given: "参数"
//        String reportId = "1"
//        when: "执行方法"
//        controller.getReportListUpdateTime(reportId)
//        then: "结果"
//        assert response.status == 200
//        def jsonData = response.json
//        assert jsonData.code == 3
//        assert jsonData.message == "报表不存在"
//    }

    void "条件查询报表清单-测试1"() {
        given:"参数"
            String groupCode = ""
            String reportName = ""
            String reportCloudId = ""
            Integer ctrlStatus = null
            Integer pageNow = null
            Integer pageSize = null
        when:"执行"
            controller.getByCondition(groupCode, reportName, reportCloudId, ctrlStatus, pageNow, pageSize)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 1
            def list = jsonData.list
            assert list?.size() == 3

            def data0 = list.get(0)
            assert data0.code == "0315"
            assert data0.name == "建档情况分析表"
            assert data0.grpCode == "06"
            assert data0.runway == 1
            assert data0.comment == null
            assert data0.cloudId == "1"
            assert data0.ctrlStatus == CtrlStatusEnum.BZZCY.code

            def data = list.get(1)
            assert data.code == "JCYCD"
            assert data.name == "监测依从度统计"
            assert data.grpCode == "01"
            assert data.runway == 2
            assert data.comment == null
            assert data.cloudId == "1"
            assert data.ctrlStatus == CtrlStatusEnum.BZZCY.code

            def data1 = list.get(2)
            assert data1.code == "TZYB"
            assert data1.name == "医生团队月报"
            assert data1.grpCode == "02"
            assert data1.runway == 1
            assert data1.comment == null
            assert data1.cloudId == "2"
            assert data1.ctrlStatus == CtrlStatusEnum.AUDIT_SUCCESS.code

    }

    void "条件查询报表清单-测试2"() {
        given:"参数"
        String groupCode = "01"
        String reportName = ""
        String reportCloudId = ""
        Integer ctrlStatus = null
        Integer pageNow = null
        Integer pageSize = null
        when:"执行"
        controller.getByCondition(groupCode, reportName, reportCloudId, ctrlStatus, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def data = list.get(0)
        assert data.code == "JCYCD"
        assert data.name == "监测依从度统计"
        assert data.grpCode == "01"
        assert data.runway == 2
        assert data.comment == null
        assert data.cloudId == "1"
        assert data.ctrlStatus == CtrlStatusEnum.BZZCY.code
    }

    void "条件查询报表清单-测试3"() {
        given:"参数"
        String groupCode = ""
        String reportName = "建档"
        String reportCloudId = ""
        Integer ctrlStatus = null
        Integer pageNow = null
        Integer pageSize = null
        when:"执行"
        controller.getByCondition(groupCode, reportName, reportCloudId, ctrlStatus, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def data0 = list.get(0)
        assert data0.code == "0315"
        assert data0.name == "建档情况分析表"
        assert data0.grpCode == "06"
        assert data0.runway == 1
        assert data0.comment == null
        assert data0.cloudId == "1"
        assert data0.ctrlStatus == CtrlStatusEnum.BZZCY.code

    }

    void "条件查询报表清单-测试4"() {
        given:"参数"
        String groupCode = ""
        String reportName = ""
        String reportCloudId = "1"
        Integer ctrlStatus = null
        Integer pageNow = null
        Integer pageSize = null
        when:"执行"
        controller.getByCondition(groupCode, reportName, reportCloudId, ctrlStatus, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 2

        def data0 = list.get(0)
        assert data0.code == "0315"
        assert data0.name == "建档情况分析表"
        assert data0.grpCode == "06"
        assert data0.runway == 1
        assert data0.comment == null
        assert data0.cloudId == "1"
        assert data0.ctrlStatus == CtrlStatusEnum.BZZCY.code

        def data = list.get(1)
        assert data.code == "JCYCD"
        assert data.name == "监测依从度统计"
        assert data.grpCode == "01"
        assert data.runway == 2
        assert data.comment == null
        assert data.cloudId == "1"
        assert data.ctrlStatus == CtrlStatusEnum.BZZCY.code
    }

    void "条件查询报表清单-测试4-1"() {
        given:"参数"
        String groupCode = ""
        String reportName = ""
        String reportCloudId = "1"
        Integer ctrlStatus = null
        Integer pageNow = 0
        Integer pageSize = 1
        when:"执行"
        controller.getByCondition(groupCode, reportName, reportCloudId, ctrlStatus, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def data0 = list.get(0)
        assert data0.code == "0315"
        assert data0.name == "建档情况分析表"
        assert data0.grpCode == "06"
        assert data0.runway == 1
        assert data0.comment == null
        assert data0.cloudId == "1"
        assert data0.ctrlStatus == CtrlStatusEnum.BZZCY.code
    }

    void "条件查询报表清单-测试5"() {
        given:"参数"
        String groupCode = ""
        String reportName = ""
        String reportCloudId = ""
        Integer ctrlStatus = CtrlStatusEnum.AUDIT_SUCCESS.code
        Integer pageNow = null
        Integer pageSize = null
        when:"执行"
        controller.getByCondition(groupCode, reportName, reportCloudId, ctrlStatus, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def data1 = list.get(0)
        assert data1.code == "TZYB"
        assert data1.name == "医生团队月报"
        assert data1.grpCode == "02"
        assert data1.runway == 1
        assert data1.comment == null
        assert data1.cloudId == "2"
        assert data1.ctrlStatus == CtrlStatusEnum.AUDIT_SUCCESS.code
    }

    void "条件查询报表清单-测试6"() {
        given:"参数"
        String groupCode = "01"
        String reportName = "监测"
        String reportCloudId = ""
        Integer ctrlStatus = null
        Integer pageNow = null
        Integer pageSize = null
        when:"执行"
        controller.getByCondition(groupCode, reportName, reportCloudId, ctrlStatus, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def data = list.get(0)
        assert data.code == "JCYCD"
        assert data.name == "监测依从度统计"
        assert data.grpCode == "01"
        assert data.runway == 2
        assert data.comment == null
        assert data.cloudId == "1"
        assert data.ctrlStatus == CtrlStatusEnum.BZZCY.code
    }

    void "条件查询报表清单-测试6-1"() {
        given:"参数"
        String groupCode = "02"
        String reportName = "监测"
        String reportCloudId = ""
        Integer ctrlStatus = null
        Integer pageNow = null
        Integer pageSize = null
        when:"执行"
        controller.getByCondition(groupCode, reportName, reportCloudId, ctrlStatus, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 0
    }

    void "条件查询报表清单-测试7"() {
        given:"参数"
        String groupCode = "01"
        String reportName = "监测"
        String reportCloudId = "2"
        Integer ctrlStatus = null
        Integer pageNow = null
        Integer pageSize = null
        when:"执行"
        controller.getByCondition(groupCode, reportName, reportCloudId, ctrlStatus, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 0

    }

    void "条件查询报表清单-测试8"() {
        given:"参数"
        String groupCode = "02"
        String reportName = "医生"
        String reportCloudId = "2"
        Integer ctrlStatus = CtrlStatusEnum.AUDIT_SUCCESS.code
        Integer pageNow = null
        Integer pageSize = null
        when:"执行"
        controller.getByCondition(groupCode, reportName, reportCloudId, ctrlStatus, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 1

        def data1 = list.get(0)
        assert data1.code == "TZYB"
        assert data1.name == "医生团队月报"
        assert data1.grpCode == "02"
        assert data1.runway == 1
        assert data1.comment == null
        assert data1.cloudId == "2"
        assert data1.ctrlStatus == CtrlStatusEnum.AUDIT_SUCCESS.code
    }

    void "条件查询报表清单-测试8-1"() {
        given:"参数"
        String groupCode = "02"
        String reportName = "医生"
        String reportCloudId = "2"
        Integer ctrlStatus = CtrlStatusEnum.BZZCY.code
        Integer pageNow = null
        Integer pageSize = null
        when:"执行"
        controller.getByCondition(groupCode, reportName, reportCloudId, ctrlStatus, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        def list = jsonData.list
        assert list?.size() == 0
    }

}

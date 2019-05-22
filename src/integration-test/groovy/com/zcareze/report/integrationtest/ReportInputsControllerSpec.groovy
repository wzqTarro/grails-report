package com.zcareze.report.integrationtest

import com.report.common.CommonValue
import com.zcareze.report.Report
import com.zcareze.report.ReportDatasource
import com.zcareze.report.ReportGrantTo
import com.zcareze.report.ReportGroups
import com.zcareze.report.ReportInputs
import com.zcareze.report.ReportInputsController
import com.zcareze.report.ReportOpenTo
import com.zcareze.report.ReportStyle
import com.zcareze.report.ReportTables
import com.zcareze.report.ReportUsually
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import groovy.sql.Sql
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Rollback
//@Ignore
class ReportInputsControllerSpec extends Specification implements ControllerUnitTest<ReportInputsController>{
    def setupSpec() {
        Sql db = Sql.newInstance("jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE",
                "sa", "",
                "org.h2.Driver" )
        db.execute("DROP TABLE IF EXISTS org_list;\n" +
                "CREATE TABLE org_list(ID CHAR(32) PRIMARY KEY, NAME VARCHAR(50), KIND VARCHAR(1));\n" +
                "INSERT INTO `org_list`(`id`,  `name`, `kind`) VALUES ('ef325711521b11e6bbd8d017c2939671',  '众康云医院', 'H');\n" +
                "INSERT INTO `org_list`(`id`,  `name`, `kind`) VALUES ('3bb0a1735bbb4f03af816720d6fcf537','外联机构', 'H');\n" +
                "INSERT INTO `org_list`(`id`, `name`, `kind`) VALUES ('3b8d8669f0cf459d91cdd0aea81ed65b', '松树桥卫生服务中心', 'H');")
    }

    def setup(){
        def datasource = new ReportDatasource(code: "00", name: "中心数据源", config: '{"kind":1}')
        datasource.save()
        def group1 = new ReportGroups(code:"01", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表");
        def group2 = new ReportGroups(code:"02", name:"服务管理", comment:"有关服务工作开展情况和开展内容等信息的呈现");
        def group3 = new ReportGroups(code:"99", name:"监控大屏", comment:"内置专门存放监控大屏报表的分组");
        group1.save();
        group2.save();
        group3.save();

        def report1 = new Report(code: "KZRZB", name: "科主任周报", grpCode: group1.code, runway: 1);
        def report2 = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: group2.code, runway: 2);
        report1.save();
        report2.save(flush: true);

        new ReportInputs(rpt: report1, name: "orgid", caption: "机构", seqNum: 0, dataType: "31", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构" ).save()

        new ReportInputs(rpt: report2, name: "endtime", caption: "结束时间", seqNum: 3, dataType: "23", inputType: 1, defType: "今天" ).save()
        new ReportInputs(rpt: report2, name: "month", caption: "月份", seqNum: 0, dataType: "12", inputType: 2, optionList: "1;2;3;4;5;6;7;8;9;10;11;12", defValue: "12" ).save()
    }
    def cleanup(){
        Report.withNewSession {
            ReportUsually.executeUpdate("delete ReportUsually")
            ReportTables.executeUpdate("delete ReportTables")
            ReportStyle.executeUpdate("delete ReportStyle")
            ReportGrantTo.executeUpdate("delete ReportGrantTo")
            ReportOpenTo.executeUpdate("delete ReportOpenTo")
            ReportInputs.executeUpdate("delete ReportInputs")
            ReportGroups.executeUpdate("delete ReportGroups")
            ReportDatasource.executeUpdate("delete ReportDatasource")
            Report.executeUpdate("delete Report")

        }
    }
    void "新增报表输入参数-测试1"() {
        given:"参数"
            Report report = Report.findByCode("JCYCD")
            ReportDatasource datasource = ReportDatasource.findByCode("00")
            ReportInputs reportInputs = new ReportInputs(rpt: report, name: "name", caption: "医生", seqNum: 2, dataType: "11", inputType: 1, dataSource: datasource)
        when:"执行"
            controller.addReportInput(reportInputs)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
        when:"验证"
            ReportInputs actualInputs = ReportInputs.createCriteria().get {
                and{
                    rpt{
                        eq("id", report.id)
                    }
                    eq("name", "name")
                }
            }
        then:"验证结果"
            assert actualInputs
            assert actualInputs.caption == "医生"
            assert actualInputs.seqNum == 2
            assert actualInputs.dataType == "11"
            assert actualInputs.inputType == 1
    }

    void "新增报表输入参数-测试2"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        ReportInputs reportInputs = new ReportInputs(rpt: report, name: "name", caption: "", seqNum: 2, dataType: "11", inputType: 1, dataSource: datasource)
        when:"执行"
        controller.addReportInput(reportInputs)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[报表输入参数]类的属性[caption]不能为null;"
    }

    void "新增报表输入参数-测试3"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        ReportInputs reportInputs = new ReportInputs(rpt: report, name: "", caption: "医生", seqNum: 2, dataType: "11", inputType: 1, dataSource: datasource)
        when:"执行"
        controller.addReportInput(reportInputs)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[报表输入参数]类的属性[name]不能为null;"
    }

    void "新增报表输入参数-测试4"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        ReportInputs reportInputs = new ReportInputs(rpt: report, name: "name", caption: "医生", seqNum: 2, dataType: "", inputType: 1, dataSource: datasource)
        when:"执行"
        controller.addReportInput(reportInputs)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[报表输入参数]类的属性[dataType]不能为null;"
    }

    void "新增报表输入参数-测试5"() {
        given:"参数"
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        ReportInputs reportInputs = new ReportInputs(rpt: null, name: "name", caption: "医生", seqNum: 2, dataType: "11", inputType: 1, dataSource: datasource)
        when:"执行"
        controller.addReportInput(reportInputs)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[报表输入参数]类的属性[rpt]不能为null;"
    }

    void "新增报表输入参数-测试6"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        report.id = null
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        ReportInputs reportInputs = new ReportInputs(rpt: report, name: "name", caption: "医生", seqNum: 2, dataType: "11", inputType: 1, dataSource: datasource)
        when:"执行"
        controller.addReportInput(reportInputs)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "新增报表输入参数-测试7"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        ReportInputs reportInputs = new ReportInputs(rpt: report, name: "my_staff_id", caption: "医生", seqNum: 2, dataType: "11", inputType: 1, dataSource: datasource)
        when:"执行"
        controller.addReportInput(reportInputs)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "系统内置参数名称"
    }

    void "新增报表输入参数-测试8"() {
        given:"参数"
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        ReportInputs reportInputs = new ReportInputs(rpt: new Report(), name: "name", caption: "医生", seqNum: 2, dataType: "11", inputType: 1, dataSource: datasource)
        when:"执行"
        controller.addReportInput(reportInputs)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "新增报表输入参数-测试9"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        ReportInputs reportInputs = new ReportInputs(rpt: report, name: "endtime", caption: "结束时间", seqNum: 2, dataType: "11", inputType: 1, dataSource: datasource)
        when:"执行"
        controller.addReportInput(reportInputs)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[报表输入参数]类的属性[name]的值[endtime]必须是唯一的;"
    }

    void "新增报表输入参数-测试10"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        ReportInputs reportInputs = new ReportInputs(rpt: report, name: "endtime1", caption: "结束时间", seqNum: 2, dataType: "11", inputType: 1)
        when:"执行"
        controller.addReportInput(reportInputs)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "数据源不能为空"
    }

    void accessParams(params, param) {
        params["rptId"]=param.rptId
        params["name"]=param.name
        params["caption"]=param.caption
        params["seqNum"]=param.seqNum
        params["dataType"]=param.dataType
        params["inputType"]=param.inputType
        params["defType"]=param.defType
        params["dataSource"]=param.dataSource
    }
    void "修改报表输入参数-测试1"() {
        given:"参数"
            Report report = Report.findByCode("JCYCD")
            def rptId = report.id
            def name = "endtime"
            def param = [rptId: rptId, name: name, caption: "结束时间", seqNum: 3, dataType: "23", inputType: 1, defType: "今天" ]
        when:"执行"
            accessParams(controller.params, param)
            controller.editReportInput()
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
        when:"验证"
            ReportInputs actualInputs = ReportInputs.createCriteria().get {
                and{
                    rpt{
                        eq("id", rptId)
                    }
                    eq("name", name)
                }
            }
        then:"验证结果"
            assert actualInputs
            assert actualInputs.caption == "结束时间"
            assert actualInputs.seqNum == 3
            assert actualInputs.dataType == "23"
            assert actualInputs.inputType == 1
            assert actualInputs.defType == "今天"
    }

    void "修改报表输入参数-测试2"() {
        given:"参数"
        def rptId = ""
        def name = "endtime"
        def param = [rptId: rptId, name: name, caption: "结束时间", seqNum: 3, dataType: "23", inputType: 1, defType: "今天" ]
        when:"执行"
        accessParams(controller.params, param)
        controller.editReportInput()
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表标识和输入参数名称不能为空"
    }

    void "修改报表输入参数-测试3"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = report.id
        def name = ""
        def param = [rptId: rptId, name: name, caption: "结束时间", seqNum: 3, dataType: "23", inputType: 1, defType: "今天" ]
        when:"执行"
        accessParams(controller.params, param)
        controller.editReportInput()
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表标识和输入参数名称不能为空"
    }

    void "修改报表输入参数-测试4"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = report.id
        def name = "endtime"
        def param = [rptId: rptId, name: name, caption: "结束", seqNum: 2, dataType: "23", inputType: 2, defType: "今" ]
        when:"执行"
        accessParams(controller.params, param)
        controller.editReportInput()
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        ReportInputs actualInputs = ReportInputs.createCriteria().get {
            and{
                rpt{
                    eq("id", rptId)
                }
                eq("name", name)
            }
        }
        then:"验证结果"
        assert actualInputs
        assert actualInputs.caption == "结束"
        assert actualInputs.seqNum == 2
        assert actualInputs.dataType == "23"
        assert actualInputs.inputType == 2
        assert actualInputs.defType == "今"
    }

    void "修改报表输入参数-测试5"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = report.id
        def name = "ed"
        def param = [rptId: rptId, name: name, caption: "结束时间", seqNum: 3, dataType: "23", inputType: 1, defType: "今天" ]
        when:"执行"
        accessParams(controller.params, param)
        controller.editReportInput()
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "输入参数不存在"
    }

    void "修改报表输入参数-测试6"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = report.id
        def name = "my_staff_id"
        def param = [rptId: rptId, name: name, caption: "结束时间", seqNum: 3, dataType: "23", inputType: 1, defType: "今天" ]
        when:"执行"
        accessParams(controller.params, param)
        controller.editReportInput()
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "系统内置参数名称"
    }

    void "修改报表输入参数-测试7"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = report.id
        def name = "endtime"
        def param = [rptId: rptId, name: name, caption: "结束时间", seqNum: 3, dataType: "2", inputType: 1, defType: "今天" ]
        when:"执行"
        accessParams(controller.params, param)
        controller.editReportInput()
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[报表输入参数]类的属性[dataType]的值[2]不在列表的取值范围内;"
    }

    void "修改报表输入参数-测试8"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = report.id
        def name = "endtime"
        def param = [rptId: rptId, name: name, caption: "结束时间", seqNum: 3, dataType: "23", inputType: 4, defType: "今天" ]
        when:"执行"
        accessParams(controller.params, param)
        controller.editReportInput()
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[报表输入参数]类的属性[inputType]的值[4]不在列表的取值范围内;"
    }

    void "修改报表输入参数-测试9"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = report.id
        def name = "endtime"
        def param = [rptId: rptId, name: name, caption: "结束时间", seqNum: 3, dataType: "23", inputType: 3, defType: "今天", dataSource: "01"]
        when:"执行"
        accessParams(controller.params, param)
        controller.editReportInput()
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "数据源有误"
    }

    void "删除报表输入参数-测试1"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = report.id
        def name = "endtime"
        when:"执行"
        controller.deleteReportInput(rptId, name)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCode("JCYCD")
        ReportInputs actualInputs = ReportInputs.createCriteria().get {
            and{
                rpt{
                    eq("id", rptId)
                }
                eq("name", name)
            }
        }
        then:"验证结果"
        assert actualInputs == null
    }

    void "删除报表输入参数-测试2"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = report.id
        def name = "end"
        when:"执行"
        controller.deleteReportInput(rptId, name)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "输入参数不存在"
    }

    void "删除报表输入参数-测试3"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = ""
        def name = ""
        when:"执行"
        controller.deleteReportInput(rptId, name)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表标识和名称都不能为空"
    }

    void "删除报表输入参数-测试4"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = null
        def name = null
        when:"执行"
        controller.deleteReportInput(rptId, name)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表标识和名称都不能为空"
    }

    void "获取指定报表的参数列表-测试1"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = report.id
        when:"执行"
        controller.getPeportInputsByRptId(rptId)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"

        def list = jsonData.list
        assert list?.size() == 2

        def data = list.get(0)
        assert data.rptId == rptId
        assert data.name == "month"
        assert data.caption == "月份"
        assert data.seqNum == 0
        assert data.dataType == "12"
        assert data.inputType == 2
        assert data.optionList == "1;2;3;4;5;6;7;8;9;10;11;12"
        assert data.defValue == "12"
        assert data.defType == null
        assert data.system == false

        def data1 = list.get(1)
        assert data1.rptId == rptId
        assert data1.name == "endtime"
        assert data1.caption == "结束时间"
        assert data1.seqNum == 3
        assert data1.dataType == "23"
        assert data1.inputType == 1
        assert data1.optionList == null
        assert data1.defType == "今天"
        assert data1.defValue == null
        assert data.system == false
    }

    void "获取指定报表的参数列表-测试2"() {
        given:"参数"
        def rptId = ""
        when:"执行"
        controller.getPeportInputsByRptId(rptId)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "参数不能为空"

        def list = jsonData.list
        assert list == null
    }

    void "获取指定报表的参数列表-测试3"() {
        given:"参数"
        def rptId = null
        when:"执行"
        controller.getPeportInputsByRptId(rptId)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "参数不能为空"

        def list = jsonData.list
        assert list == null
    }

    void "获取指定报表的参数列表-测试4"() {
        given:"参数"
        def rptId = "12"
        when:"执行"
        controller.getPeportInputsByRptId(rptId)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"

        def list = jsonData.list
        assert list?.size() == 0
    }

    void "获取指定的报表参数-测试1"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = report.id
        def name = "month"
        when:"执行"
        controller.getReportInput(rptId, name)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"

        def data = jsonData.one
        assert data
        assert data.rptId == rptId
        assert data.name == "month"
        assert data.caption == "月份"
        assert data.seqNum == 0
        assert data.dataType == "12"
        assert data.inputType == 2
        assert data.optionList == "1;2;3;4;5;6;7;8;9;10;11;12"
        assert data.defValue == "12"
        assert data.defType == null
        assert data.system == false
    }

    void "获取指定的报表参数-测试2"() {
        given:"参数"
        def rptId = null
        def name = null
        when:"执行"
        controller.getReportInput(rptId, name)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "参数不能为空"
    }

    void "获取指定的报表参数-测试3"() {
        given:"参数"
        Report report = Report.findByCode("JCYCD")
        def rptId = report.id
        def name = "123"
        when:"执行"
        controller.getReportInput(rptId, name)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        def data = jsonData.one
        assert data == null
    }

    void "获取动态查询参数的参数值信息-测试1"() {
        given:
            Report report = Report.findByCode("KZRZB")
            def reportId = report.id
            def name = "orgid"
        when:
            controller.getReportQueryInputValue(reportId, name)
        then:
            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"

            def list = jsonData.list
            assert list?.size() == 3
            assert list.any { org ->
                org.colValue == "ef325711521b11e6bbd8d017c2939671" && org.colTitle == "众康云医院"
            }
            assert list.any { org ->
                org.colValue == "3bb0a1735bbb4f03af816720d6fcf537" && org.colTitle == "外联机构"
            }
            assert list.any { org ->
                org.colValue == "3b8d8669f0cf459d91cdd0aea81ed65b" && org.colTitle == "松树桥卫生服务中心"
            }
    }

    void "获取动态查询参数的参数值信息-测试2"() {
        given:
        def reportId = null
        def name = null
        when:
        controller.getReportQueryInputValue(reportId, name)
        then:
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "参数不能为空"
    }

    void "api获取动态查询参数的参数值信息-测试1"() {
        given:
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def name = "orgid"
        when:
        controller.getReportQueryInputValueOld(reportId, name)
        then:
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.errcode == 0

        def bizData = jsonData.bizData
        assert bizData
        def list = bizData.queryInputValueVOs
        assert list?.size() == 3
        assert list.any { org ->
            org.colValue == "ef325711521b11e6bbd8d017c2939671" && org.colTitle == "众康云医院"
        }
        assert list.any { org ->
            org.colValue == "3bb0a1735bbb4f03af816720d6fcf537" && org.colTitle == "外联机构"
        }
        assert list.any { org ->
            org.colValue == "3b8d8669f0cf459d91cdd0aea81ed65b" && org.colTitle == "松树桥卫生服务中心"
        }
    }

    void "api获取动态查询参数的参数值信息-测试2"() {
        given:
        def reportId = null
        def name = null
        when:
        controller.getReportQueryInputValueOld(reportId, name)
        then:
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.errcode == 3
        assert jsonData.errmsg == "参数不能为空"
    }
}

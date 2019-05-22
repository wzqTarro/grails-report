package com.zcareze.report.integrationtest

import com.report.dto.ReportParamValue
import com.report.param.ReportViewParam
import com.report.param.ScreenReportParam
import com.report.util.XmlUtil
import com.zcareze.report.DataFacadeService
import com.zcareze.report.Report
import com.zcareze.report.ReportDatasource
import com.zcareze.report.ReportGrantTo
import com.zcareze.report.ReportGroups
import com.zcareze.report.ReportInputs
import com.zcareze.report.ReportOpenTo
import com.zcareze.report.ReportStyle
import com.zcareze.report.ReportTables
import com.zcareze.report.ReportTablesController
import com.zcareze.report.ReportUsually
import grails.converters.JSON
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import groovy.sql.Sql
import spock.lang.Ignore
import spock.lang.Specification

import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Validator

@Integration(applicationClass = statreports.Application)
@Rollback
//@Ignore
class ReportTablesControllerSpec extends Specification implements ControllerUnitTest<ReportTablesController> {
    def setupSpec() {
        def url = grailsApplication.config.getProperty("select.dataSource.url")
        def user = grailsApplication.config.getProperty("select.dataSource.user")
        def pwd = grailsApplication.config.getProperty("select.dataSource.pwd")
        def driverClassName = grailsApplication.config.getProperty("select.dataSource.driverClassName")
        Sql db = Sql.newInstance(url,
                user, pwd,
                driverClassName )
        db.execute("DROP TABLE IF EXISTS org_list;\n" +
                "CREATE TABLE org_list(ID CHAR(32) PRIMARY KEY, NAME VARCHAR(50), KIND VARCHAR(1));\n" +
                "INSERT INTO `org_list`(`id`,  `name`, `kind`) VALUES ('ef325711521b11e6bbd8d017c2939671',  '众康云医院', 'H');\n" +
                "INSERT INTO `org_list`(`id`,  `name`, `kind`) VALUES ('3bb0a1735bbb4f03af816720d6fcf537','外联机构', 'H');\n" +
                "INSERT INTO `org_list`(`id`, `name`, `kind`) VALUES ('3b8d8669f0cf459d91cdd0aea81ed65b', '松树桥卫生服务中心', 'H');")
        db.execute("DROP TABLE IF EXISTS resident_list;\n" +
                "CREATE TABLE RESIDENT_LIST(ID CHAR(32) PRIMARY KEY, commit_time DATE, ORG_ID CHAR(32));\n" +
                "INSERT INTO `resident_list`(`id`,  `commit_time`, `ORG_ID`) VALUES ('ef325711521b11e6bbd8d017c2939671',  '2019-03-02', 'ef325711521b11e6bbd8d017c2939671');\n" +
                "INSERT INTO `resident_list`(`id`,  `commit_time`, `ORG_ID`) VALUES ('3bb0a1735bbb4f03af816720d6fcf537','2018-01-01', 'ef325711521b11e6bbd8d017c2939671');\n" +
                "INSERT INTO `resident_list`(`id`, `commit_time`, `ORG_ID`) VALUES ('3b8d8669f0cf459d91cdd0aea81ed65b', '2019-03-16', 'ef325711521b11e6bbd8d017c2939671');")
    }
    def setup() {
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
        def report3 = new Report(code: "TZYB", name: "医生团队月报", grpCode: group1.code, runway: 1)
        report1.save();
        report3.save()
        report2.save(flush: true);

        new ReportTables(dataSource: datasource, rpt: report1, name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1).save()
        new ReportTables(dataSource: datasource, rpt: report1, name: "tb0", sqlText: "select name,abbr,gender,birthday,archive_address from resident_list LIMIT 30", seqNum: 2).save()
        new ReportTables(dataSource: datasource, rpt: report2, name: "tb0", sqlText: "select A.*,B.name as residentName from rsdt_contract_list as A,resident_list as B where A.resident_id=B.id AND service_months=[month] AND accept_time\n" +
                "BETWEEN date_format([year], '%Y%m%d')\n" +
                "   and date_format([endtime], '%Y%m%d')\n" +
                "  AND doctor_name like  CONCAT('%',[name],'%')  LIMIT 30", seqNum: 1).save()

        def group = new ReportGroups(code:"06", name:"pc报表样式")
        group.save()
        def report4 = new Report(code: "0315", name:"建档情况分析表", runway: 1, grpCode: group.code)
        report4.save(flush: true)

        new ReportStyle(rpt: report4, scene:0, fileUrl:"asd",chart: "<chart name=\\\"chart1\\\" theme=\\\"walden\\\" tab=\\\"建档情况\\\"><type>bar</type><title>机构建档情况</title><x_col>机构名称</x_col><y_col><field>建档数</field><name>建档数</name></y_col></chart>").save()
        new ReportTables(dataSource: datasource, name:"createRecord", rpt: report4, seqNum: 1, sqlText: "select ol.`id` as name, COUNT(rl.`id`) as count FROM org_list ol INNER JOIN `resident_list` rl on rl.`org_id`= ol.`id` where ol.`id` = [orgID] and rl.`commit_time` BETWEEN '2019-03-01' and '2019-03-30' GROUP BY ol.`id`").save()
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
    void "添加报表数据表-测试1"() {
        given: "参数"
            Report report = Report.findByCode("TZYB")
            ReportDatasource datasource = ReportDatasource.findByCode("00")
            ReportTables table = new ReportTables(rpt: report, name: "tb0",
                    sqlText: "select name,abbr,gender,birthday,archive_address from resident_list LIMIT 30", seqNum: 1, dataSource: datasource)
        when: "执行"
            controller.addReportTable(table)
        then: "结果"
            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
        when:"验证"
            ReportTables actualTable = ReportTables.createCriteria().get {
                and{
                    rpt{
                        eq("id", report.id)
                    }
                    eq("name", "tb0")
                }
            }
        then:"验证结果"
            assert actualTable
            assert actualTable.rpt.id == report.id
            assert actualTable.name == "tb0"
            assert actualTable.sqlText == "select name,abbr,gender,birthday,archive_address from resident_list LIMIT 30"
            assert actualTable.seqNum == 1
            assert actualTable.dataSource.code == "00"
    }

    void "添加报表数据表-测试2"() {
        given: "参数"
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        Report report = Report.findByCode("TZYB")
        ReportTables table = new ReportTables(rpt: report, name: "", sqlText: "select name,abbr,gender,birthday,archive_address from resident_list LIMIT 30", seqNum: 1, dataSource: datasource)
        when: "执行"
        controller.addReportTable(table)
        then: "结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[报表数据表]类的属性[name]不能为null;"
    }

    void "添加报表数据表-测试3"() {
        given: "参数"
        Report report = Report.findByCode("TZYB")
        ReportTables table = new ReportTables(rpt: report, name: "tb0", sqlText: "", seqNum: 1)
        when: "执行"
        controller.addReportTable(table)
        then: "结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "数据源不能为空"
    }

    void "添加报表数据表-测试4"() {
        given: "参数"
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        Report report = Report.findByCode("TZYB")
        ReportTables table = new ReportTables(rpt: new Report(), name: "tb0", sqlText: "select", seqNum: 1, dataSource: datasource)
        when: "执行"
        controller.addReportTable(table)
        then: "结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表不能为空"
    }

    void "添加报表数据表-测试5"() {
        given: "参数"
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        Report report = Report.findByCode("TZYB")
        ReportTables table = new ReportTables(rpt: report, name: "tb0", sqlText: "select", seqNum: -1, dataSource: datasource)
        when: "执行"
        controller.addReportTable(table)
        then: "结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[报表数据表]类的属性[seqNum]的值[-1]比最小值 [0]还小;"
    }

    void "添加报表数据表-测试6"() {
        given: "参数"
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        Report report = Report.findByCode("TZYB")
        ReportTables table = new ReportTables(rpt: report, name: "tb0", sqlText: "select", seqNum: -1, dataSource: datasource)
        when: "执行"
        controller.addReportTable(null)
        then: "结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "数据表不能为空"
    }

    void "添加报表数据表-测试7"() {
        given: "参数"
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        Report report = Report.findByCode("TZYB")
        ReportTables table = new ReportTables(rpt: null, name: "tb0", sqlText: "select", seqNum: 1, dataSource: datasource)
        when: "执行"
        controller.addReportTable(table)
        then: "结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[报表数据表]类的属性[rpt]不能为null;[报表数据表]类的属性[name]的值[tb0]必须是唯一的;"
    }

    void "添加报表数据表-测试8"() {
        given: "参数"
        ReportDatasource datasource = ReportDatasource.findByCode("00")
        Report report = Report.findByCode("JCYCD")
        ReportTables table = new ReportTables(rpt: report, name: "tb0", sqlText: "select", seqNum: 1, dataSource: datasource)
        when: "执行"
        controller.addReportTable(table)
        then: "结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[报表数据表]类的属性[name]的值[tb0]必须是唯一的;"
    }

    void accessParams(params, param) {
        params.rptId=param.rptId
        params.name=param.name
        params.sqlText=param.sqlText
        params.seqNum=param.seqNum
        params.dataSource=param.dataSource
    }
    void "修改报表数据表-测试1"() {
        given:"参数"
            Report report = Report.findByCode("KZRZB")
            def param = [rptId: report.id, name: "table",
                         sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1,
                        dataSource: "00"]
            accessParams(controller.params, param)
        when:"执行"
            controller.editReportTable()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        ReportTables actualTable = ReportTables.createCriteria().get {
            and{
                rpt{
                    eq("id", report.id)
                }
                eq("name", "table")
            }
        }
        then:"验证结果"
        assert actualTable
        assert actualTable.rpt.id == report.id
        assert actualTable.name == "table"
        assert actualTable.sqlText == "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')"
        assert actualTable.seqNum == 1
        assert actualTable.dataSource.code == "00"

    }
    void "修改报表数据表-测试2"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def param = [rptId: report.id, name: "table", sqlText: "select", seqNum: 2, dataSource: "00"]
        accessParams(controller.params, param)
        when:"执行"
        controller.editReportTable()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        ReportTables actualTable = ReportTables.createCriteria().get {
            and{
                rpt{
                    eq("id", report.id)
                }
                eq("name", "table")
            }
        }
        then:"验证结果"
        assert actualTable
        assert actualTable.rpt.id == report.id
        assert actualTable.name == "table"
        assert actualTable.sqlText == "select"
        assert actualTable.seqNum == 2
        assert actualTable.dataSource.code == "00"
    }
    void "修改报表数据表-测试3-1"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def param = [rptId: report.id, name: "table", sqlText: "", seqNum: 2, dataSource: "01"]
        accessParams(controller.params, param)
        when:"执行"
        controller.editReportTable()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "数据源编码有误"
    }
    void "修改报表数据表-测试4"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def param = [rptId: report.id, name: "", sqlText: "select", seqNum: 2, dataSource: "00"]
        accessParams(controller.params, param)
        when:"执行"
        controller.editReportTable()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表标识和数据表名称不能为空"
    }
    void "修改报表数据表-测试5"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def param = [rptId: report.id, name: "table", sqlText: "select", seqNum: -1, dataSource: "00"]
        accessParams(controller.params, param)
        when:"执行"
        controller.editReportTable()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[报表数据表]类的属性[seqNum]的值[-1]比最小值 [0]还小;"
    }
    void "修改报表数据表-测试6"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def param = [rptId: report.id, name: "tab", sqlText: "select", seqNum: 2, dataSource: "00"]
        accessParams(controller.params, param)
        when:"执行"
        controller.editReportTable()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "数据表不存在"
    }
    void "修改报表数据表-测试7"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def param = [rptId: null, name: "tab", sqlText: "select", seqNum: 2, dataSource: "00"]
        accessParams(controller.params, param)
        when:"执行"
        controller.editReportTable()
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表标识和数据表名称不能为空"
    }
    void "删除表数据表-测试1"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def name = "table"
        when:"执行"
        controller.deleteReportTable(reportId, name)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.get(reportId)
        ReportTables actualTable = ReportTables.createCriteria().get {
            and{
                rpt{
                    eq("id", report.id)
                }
                eq("name", name)
            }
        }
        then:"验证结果"
        assert actualTable == null
    }
    void "删除表数据表-测试2"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def name = ""
        when:"执行"
        controller.deleteReportTable(reportId, name)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表标识和数据表名称不能为空"
    }
    void "删除表数据表-测试3"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = null
        def name = "table"
        when:"执行"
        controller.deleteReportTable(reportId, name)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表标识和数据表名称不能为空"
    }
    void "删除表数据表-测试4"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        def name = "tabl1e"
        when:"执行"
        controller.deleteReportTable(reportId, name)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "数据表不存在"
    }
    void "获取指定报表的数据表-测试1"() {
        given:"参数"
        Report report = Report.findByCode("KZRZB")
        def reportId = report.id
        when:"执行"
        controller.getReportTableByRptId(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"

        def list = jsonData.list
        assert list?.size()==2

        def data = list.get(0)
        assert data.name == "table"
        assert data.sqlText == "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')"
        assert data.seqNum == 1
        assert data.rptId == reportId
        assert data.dataSource == "00"

        def data1 = list.get(1)
        assert data1.name == "tb0"
        assert data1.sqlText == "select name,abbr,gender,birthday,archive_address from resident_list LIMIT 30"
        assert data1.seqNum == 2
        assert data1.rptId == reportId
        assert data.dataSource == "00"
    }

    void "获取指定报表的数据表-测试2"() {
        given:"参数"
        def reportId = ""
        when:"执行"
        controller.getReportTableByRptId(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表标识不能为空"
    }

    void "获取指定报表的数据表-测试3"() {
        given:"参数"
        def reportId = "1"
        when:"执行"
        controller.getReportTableByRptId(reportId)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"

        def list = jsonData.list
        assert list?.size() == 0
    }

    void "获取指定报表的数据-测试1"() {
        given:"参数"
            Report report = Report.findByCode("0315")
            def reportId = report.id
            ReportViewParam reportParam = new ReportViewParam()
            reportParam.reportId = reportId

            ReportParamValue paramValue = new ReportParamValue()
            paramValue.name = "orgID"
            paramValue.value = "ef325711521b11e6bbd8d017c2939671"
            paramValue.title = "zky"
            List<ReportParamValue> paramValues = new ArrayList<>()
            paramValues.add(paramValue)
            reportParam.paramValues = paramValues
        when:"执行"
            DataFacadeService dataFacadeService = new DataFacadeService()
            controller.dataFacadeService = dataFacadeService
            controller.getReportData(reportParam)
        then:"结果"
            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"

            def one = jsonData.one
            assert one
            def xml = one.xmlData
            assert xml

            Validator xmlValidate = XmlUtil.validateReport()
            assert xmlValidate.validate( new StreamSource( new ByteArrayInputStream(xml.bytes))) == null

            def langs = new XmlParser().parseText(xml)

            // 根节点
            def rootName = langs.name()
            assert rootName == "report"

            def varSet = langs.var_set
            assert varSet
            assert varSet.size() == 1

            def varRecord = varSet.var_record
            assert varRecord
            assert varRecord.size() == 1
            assert varRecord[0].name() == "var_record"

            def varName = varRecord.var_name
            assert varName
            assert varName.size() == 1
            assert varName[0].name() == "var_name"
            assert varName[0].value()[0] == "orgID"

            def varValue = varRecord.var_value
            assert varValue
            assert varValue.size() == 1
            assert varValue[0].name() == "var_value"
            assert varValue[0].value()[0] == "ef325711521b11e6bbd8d017c2939671"

            def varTitle = varRecord.var_title
            assert varTitle
            assert varTitle.size() == 1
            assert varTitle[0].name() == "var_title"
            assert varTitle[0].value()[0] == "zky"

            def dataTable = langs.data_table
            assert dataTable
            assert dataTable.size() == 1

            def dataTable0 = dataTable[0]
            assert dataTable0.name() == "data_table"

            def dataTableAttribute = dataTable0.attributes()
            assert dataTableAttribute.name == "createRecord"
            assert dataTableAttribute.seq_num == "1"

            def record = dataTable0.record
            assert record.size() == 1
            assert record[0].name() == "record"
            assert record[0].value()[0].name() == "NAME"
            assert record[0].value()[0].value()[0] == "ef325711521b11e6bbd8d017c2939671"
            assert record[0].value()[1].name() == "COUNT"
            assert record[0].value()[1].value()[0] == "2"
    }

    void "获取指定报表的数据-测试2"() {
        given:"参数"
        def reportParam = null
        when:"执行"
        controller.getReportData(reportParam)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "参数为空"
    }

    void "获取指定报表的数据-测试3"() {
        given:"参数"
        def reportId = "1"
        ReportViewParam reportParam = new ReportViewParam()
        reportParam.reportId = reportId
        when:"执行"
        controller.getReportData(reportParam)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "报表信息为空"
    }

    void "api获取指定报表的数据-测试1"() {
        given:"参数"
        Report report = Report.findByCode("0315")
        def reportId = report.id
        ReportViewParam reportParam = new ReportViewParam()
        reportParam.reportId = reportId

        ReportParamValue paramValue = new ReportParamValue()
        paramValue.name = "orgID"
        paramValue.value = "ef325711521b11e6bbd8d017c2939671"
        paramValue.title = "zky"
        List<ReportParamValue> paramValues = new ArrayList<>()
        paramValues.add(paramValue)
        reportParam.paramValues = paramValues
        when:"执行"
        DataFacadeService dataFacadeService = new DataFacadeService()
        controller.dataFacadeService = dataFacadeService
        controller.getReportDataOld((reportParam as JSON) as String)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.errcode == 0

        def bizData = jsonData.bizData
        assert bizData

        def one = bizData.reportDataVO
        assert one
        def xml = one.xmlData
        assert xml

        Validator xmlValidate = XmlUtil.validateReport()
        assert xmlValidate.validate( new StreamSource( new ByteArrayInputStream(xml.bytes))) == null

        def langs = new XmlParser().parseText(xml)

        // 根节点
        def rootName = langs.name()
        assert rootName == "report"

        def varSet = langs.var_set
        assert varSet
        assert varSet.size() == 1

        def varRecord = varSet.var_record
        assert varRecord
        assert varRecord.size() == 1
        assert varRecord[0].name() == "var_record"

        def varName = varRecord.var_name
        assert varName
        assert varName.size() == 1
        assert varName[0].name() == "var_name"
        assert varName[0].value()[0] == "orgID"

        def varValue = varRecord.var_value
        assert varValue
        assert varValue.size() == 1
        assert varValue[0].name() == "var_value"
        assert varValue[0].value()[0] == "ef325711521b11e6bbd8d017c2939671"

        def varTitle = varRecord.var_title
        assert varTitle
        assert varTitle.size() == 1
        assert varTitle[0].name() == "var_title"
        assert varTitle[0].value()[0] == "zky"

        def dataTable = langs.data_table
        assert dataTable
        assert dataTable.size() == 1

        def dataTable0 = dataTable[0]
        assert dataTable0.name() == "data_table"

        def dataTableAttribute = dataTable0.attributes()
        assert dataTableAttribute.name == "createRecord"
        assert dataTableAttribute.seq_num == "1"

        def record = dataTable0.record
        assert record.size() == 1
        assert record[0].name() == "record"
        assert record[0].value()[0].name() == "NAME"
        assert record[0].value()[0].value()[0] == "ef325711521b11e6bbd8d017c2939671"
        assert record[0].value()[1].name() == "COUNT"
        assert record[0].value()[1].value()[0] == "2"
    }

    void "api获取指定报表的数据-测试2"() {
        given:"参数"
        def reportParam = null
        when:"执行"
        controller.getReportDataOld(reportParam)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.errcode == 3
        assert jsonData.errmsg == "参数为空"
    }

    void "api获取指定报表的数据-测试3"() {
        given:"参数"
        def reportId = "1"
        ReportViewParam reportParam = new ReportViewParam()
        reportParam.reportId = reportId
        when:"执行"
        controller.getReportDataOld((reportParam as JSON) as String)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.errcode == 3
        assert jsonData.errmsg == "报表信息为空"
    }

    void "api获取指定报表的数据-测试4"() {
        given:"参数"
        def reportId = "1"
        ReportViewParam reportParam = new ReportViewParam()
        reportParam.reportId = reportId
        when:"执行"
        controller.getReportDataOld("123")
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData.errcode == 3
        assert jsonData.errmsg == "报表信息为空"
    }

    void "获取指定大屏的所有数据表的结构-测试1"() {
        given:
            Report report = Report.findByCode("0315")
            def reportId = report.id
        when:
            DataFacadeService dataFacadeService = new DataFacadeService()
            controller.dataFacadeService = dataFacadeService
            controller.getScreenReportStruct(reportId)
        then:
            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

            def jsonData = response.json
            assert jsonData
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"

            def one = jsonData.one
            assert one
            def xml = one.xmlData
            assert xml

            assert XmlUtil.validateScreen().validate( new StreamSource( new ByteArrayInputStream(xml.bytes))) == null

            def langs = new XmlParser().parseText(xml)

            // 根节点
            def rootName = langs.name()
            assert rootName == "report"

            def varSet = langs.var_set
            assert varSet
            assert varSet.size() == 1

            def varRecord = varSet.var_record
            assert varRecord
            assert varRecord.size() == 1
            assert varRecord[0].name() == "var_record"

            def varName = varRecord.var_name
            assert varName
            assert varName.size() == 1
            assert varName[0].name() == "var_name"
            assert varName[0].value()[0] == null

            def varValue = varRecord.var_value
            assert varValue
            assert varValue.size() == 1
            assert varValue[0].name() == "var_value"
            assert varValue[0].value()[0] == "ef325711521b11e6bbd8d017c2939671"

            def varTitle = varRecord.var_title
            assert varTitle
            assert varTitle.size() == 1
            assert varTitle[0].name() == "var_title"
            assert varTitle[0].value()[0] == "my_org_name"

            def dataTable = langs.data_table
            assert dataTable
            assert dataTable.size() == 1

            def dataTable0 = dataTable[0]
            assert dataTable0.name() == "data_table"

            def dataTableName = dataTable0.value()[0]
            assert dataTableName.name() == "name"
            assert dataTableName.value()[0] == "createRecord"

            def columns = dataTable0.value()[1]
            assert columns.name() == "columns"
            assert columns.value().size() == 2
            def column = columns.value()[0]
            assert column.name() == "column"
            assert column.value()[0] == "ID"
            def column1 = columns.value()[1]
            assert column1.name() == "column"
            assert column.value()[0] == "ID"
    }

    void "获取指定大屏的所有数据表的结构-测试2"() {
        given:
        def reportId = ""
        when:
        controller.getScreenReportStruct(reportId)
        then:
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表信息为空"
    }

    void "api获取指定大屏的所有数据表的结构-测试1"() {
        given:
        Report report = Report.findByCode("0315")
        def reportId = report.id
        when:
        DataFacadeService dataFacadeService = new DataFacadeService()
        controller.dataFacadeService = dataFacadeService
        controller.getScreenReportStructOld(reportId)
        then:
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData
        assert jsonData.errcode == 0

        def bizData = jsonData.bizData
        assert bizData

        def one = bizData.reportDataVO
        assert one
        def xml = one.xmlData
        assert xml
        assert XmlUtil.validateScreen().validate( new StreamSource( new ByteArrayInputStream(xml.bytes))) == null

        def langs = new XmlParser().parseText(xml)

        // 根节点
        def rootName = langs.name()
        assert rootName == "report"

        def varSet = langs.var_set
        assert varSet
        assert varSet.size() == 1

        def varRecord = varSet.var_record
        assert varRecord
        assert varRecord.size() == 1
        assert varRecord[0].name() == "var_record"

        def varName = varRecord.var_name
        assert varName
        assert varName.size() == 1
        assert varName[0].name() == "var_name"
        assert varName[0].value()[0] == null

        def varValue = varRecord.var_value
        assert varValue
        assert varValue.size() == 1
        assert varValue[0].name() == "var_value"
        assert varValue[0].value()[0] == "ef325711521b11e6bbd8d017c2939671"

        def varTitle = varRecord.var_title
        assert varTitle
        assert varTitle.size() == 1
        assert varTitle[0].name() == "var_title"
        assert varTitle[0].value()[0] == "my_org_name"

        def dataTable = langs.data_table
        assert dataTable
        assert dataTable.size() == 1

        def dataTable0 = dataTable[0]
        assert dataTable0.name() == "data_table"

        def dataTableName = dataTable0.value()[0]
        assert dataTableName.name() == "name"
        assert dataTableName.value()[0] == "createRecord"

        def columns = dataTable0.value()[1]
        assert columns.name() == "columns"
        assert columns.value().size() == 2
        def column = columns.value()[0]
        assert column.name() == "column"
        assert column.value()[0] == "ID"
        def column1 = columns.value()[1]
        assert column1.name() == "column"
        assert column.value()[0] == "ID"
    }

    void "api获取指定大屏的所有数据表的结构-测试2"() {
        given:
        def reportId = ""
        when:
        controller.getScreenReportStructOld(reportId)
        then:
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData
        assert jsonData.errcode == 3
        assert jsonData.errmsg == "报表信息为空"
    }

    void "获取大屏指定表数据-测试1"() {
        given:
        Report report = Report.findByCode("0315")
        def reportId = report.id
        ScreenReportParam param = new ScreenReportParam()
        List<String> tables = new ArrayList<>()
        tables.add("createRecord")
        param.reportId = reportId
        param.tableNames = tables
        when:
        DataFacadeService dataFacadeService = new DataFacadeService()
        controller.dataFacadeService = dataFacadeService
        controller.getScreenReportData(param)
        then:
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"

        def one = jsonData.one
        assert one
        def xml = one.xmlData
        assert xml

        assert XmlUtil.validateReport().validate( new StreamSource( new ByteArrayInputStream(xml.bytes))) == null

        def langs = new XmlParser().parseText(xml)

        // 根节点
        def rootName = langs.name()
        assert rootName == "report"

        def varSet = langs.var_set
        assert varSet
        assert varSet.size() == 1

        def varRecord = varSet.var_record
        assert varRecord
        assert varRecord.size() == 1
        assert varRecord[0].name() == "var_record"

        def varName = varRecord.var_name
        assert varName
        assert varName.size() == 1
        assert varName[0].name() == "var_name"
        assert varName[0].value()[0] == null

        def varValue = varRecord.var_value
        assert varValue
        assert varValue.size() == 1
        assert varValue[0].name() == "var_value"
        assert varValue[0].value()[0] == "ef325711521b11e6bbd8d017c2939671"

        def varTitle = varRecord.var_title
        assert varTitle
        assert varTitle.size() == 1
        assert varTitle[0].name() == "var_title"
        assert varTitle[0].value()[0] == "my_org_name"

        def dataTable = langs.data_table
        assert dataTable
        assert dataTable.size() == 1

        def dataTable0 = dataTable[0]
        assert dataTable0.name() == "data_table"

        def dataTableAttribute = dataTable0.attributes()
        assert dataTableAttribute.name == "createRecord"
        assert dataTableAttribute.seq_num == "1"

        def record = dataTable0.record
        assert record.size() == 1
        assert record[0].name() == "record"
        assert record[0].value()[0].name() == "NAME"
        assert record[0].value()[0].value()[0] == "ef325711521b11e6bbd8d017c2939671"
        assert record[0].value()[1].name() == "COUNT"
        assert record[0].value()[1].value()[0] == "2"
    }

    void "获取大屏指定表数据-测试2"() {
        given:
        ScreenReportParam param = null
        when:
        controller.getScreenReportData(param)
        then:
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "参数为空"
    }

    void "获取大屏指定表数据-测试3"() {
        given:
        Report report = Report.findByCode("0315")
        def reportId = report.id
        ScreenReportParam param = new ScreenReportParam()
        param.reportId = reportId
        when:
        controller.getScreenReportData(param)
        then:
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "数据表名不能为空"
    }

    void "api获取大屏指定表数据-测试1"() {
        given:
        Report report = Report.findByCode("0315")
        def reportId = report.id
        ScreenReportParam param = new ScreenReportParam()
        List<String> tables = new ArrayList<>()
        tables.add("createRecord")
        param.reportId = reportId
        param.tableNames = tables
        when:
        DataFacadeService dataFacadeService = new DataFacadeService()
        controller.dataFacadeService = dataFacadeService
        controller.getScreenReportDataOld((param as JSON) as String)
        then:
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData
        assert jsonData.errcode == 0

        def bizData = jsonData.bizData
        def one = bizData.reportDataVO
        assert one
        def xml = one.xmlData
        assert xml
        assert XmlUtil.validateReport().validate( new StreamSource( new ByteArrayInputStream(xml.bytes))) == null

        def langs = new XmlParser().parseText(xml)

        // 根节点
        def rootName = langs.name()
        assert rootName == "report"

        def varSet = langs.var_set
        assert varSet
        assert varSet.size() == 1

        def varRecord = varSet.var_record
        assert varRecord
        assert varRecord.size() == 1
        assert varRecord[0].name() == "var_record"

        def varName = varRecord.var_name
        assert varName
        assert varName.size() == 1
        assert varName[0].name() == "var_name"
        assert varName[0].value()[0] == null

        def varValue = varRecord.var_value
        assert varValue
        assert varValue.size() == 1
        assert varValue[0].name() == "var_value"
        assert varValue[0].value()[0] == "ef325711521b11e6bbd8d017c2939671"

        def varTitle = varRecord.var_title
        assert varTitle
        assert varTitle.size() == 1
        assert varTitle[0].name() == "var_title"
        assert varTitle[0].value()[0] == "my_org_name"

        def dataTable = langs.data_table
        assert dataTable
        assert dataTable.size() == 1

        def dataTable0 = dataTable[0]
        assert dataTable0.name() == "data_table"

        def dataTableAttribute = dataTable0.attributes()
        assert dataTableAttribute.name == "createRecord"
        assert dataTableAttribute.seq_num == "1"

        def record = dataTable0.record
        assert record.size() == 1
        assert record[0].name() == "record"
        assert record[0].value()[0].name() == "NAME"
        assert record[0].value()[0].value()[0] == "ef325711521b11e6bbd8d017c2939671"
        assert record[0].value()[1].name() == "COUNT"
        assert record[0].value()[1].value()[0] == "2"
    }

    void "api获取大屏指定表数据-测试2"() {
        given:
        when:
        controller.getScreenReportDataOld("")
        then:
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData
        assert jsonData.errcode == 3
        assert jsonData.errmsg == "参数为空"
    }

    void "api获取大屏指定表数据-测试3"() {
        given:
        Report report = Report.findByCode("0315")
        def reportId = report.id
        ScreenReportParam param = new ScreenReportParam()
        param.reportId = reportId
        when:
        controller.getScreenReportDataOld((param as JSON) as String)
        then:
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = response.json
        assert jsonData
        assert jsonData.errcode == 3
        assert jsonData.errmsg == "数据表名不能为空"
    }
}

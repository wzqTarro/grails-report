package com.zcareze.report.integrationtest

import com.zcareze.report.Report
import com.zcareze.report.ReportGrantTo
import com.zcareze.report.ReportGroups
import com.zcareze.report.ReportGroupsController
import com.zcareze.report.ReportInputs
import com.zcareze.report.ReportOpenTo
import com.zcareze.report.ReportStyle
import com.zcareze.report.ReportTables
import com.zcareze.report.ReportUsually
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Rollback
//@Ignore
class ReportGroupsControllerSpec extends Specification implements ControllerUnitTest<ReportGroupsController>{
    def setup(){
        def group2 = new ReportGroups(code:"02", name:"服务管理", comment:"有关服务工作开展情况和开展内容等信息的呈现");
        def group1 = new ReportGroups(code:"03", name:"药品管理", comment:"有关药品等信息的呈现");
        def group3 = new ReportGroups(code:"99", name:"监控大屏", comment:"内置专门存放监控大屏报表的分组");
        group1.save()
        group2.save();
        group3.save(flush: true);
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
            Report.executeUpdate("delete Report")
        }
    }

    void "添加报表分组-测试1"() {
        given:"参数"
            def group = new ReportGroups(code:"01", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表");
        when:"执行方法"
            controller.addReportGroup(group)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 1
            //this.messageSource.getMessage("default.x.x", [Report, ""], Locale.SIMPLIFIED_CHINESE)
            assert jsonData.message == "执行成功"
        when:"检测是否新增成功"
            ReportGroups testReportGroups = ReportGroups.get(group.id)
        then:"返回结果"
            assert testReportGroups
            assert testReportGroups.code == "01"
            assert testReportGroups.name == "运营简报"
            assert testReportGroups.comment == "提现整体经营服务规模效果效益等内容的报表"
    }

    void "添加报表分组-测试2"() {
        given:"参数"
            def group = new ReportGroups(code:"", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表");
        when:"执行方法"
            controller.addReportGroup(group)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            //this.messageSource.getMessage("default.x.x", [Report, ""], Locale.SIMPLIFIED_CHINESE)
            assert jsonData.message == "[报表分组]类的属性[code]不能为null;"
    }

    void "添加报表分组-测试3"() {
        given:"参数"
            def group = new ReportGroups(code:"01", name:"", comment:"提现整体经营服务规模效果效益等内容的报表");
        when:"执行方法"
            controller.addReportGroup(group)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            //this.messageSource.getMessage("default.x.x", [Report, ""], Locale.SIMPLIFIED_CHINESE)
            assert jsonData.message == "[报表分组]类的属性[name]不能为null;"
    }

    void "添加报表分组-测试4"() {
        given:"参数"
            def group = new ReportGroups(code:"01", name:"服务管理", comment:"提现整体经营服务规模效果效益等内容的报表");
        when:"执行方法"
            controller.addReportGroup(group)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            //this.messageSource.getMessage("default.x.x", [Report, ""], Locale.SIMPLIFIED_CHINESE)
            assert jsonData.message == "[报表分组]类的属性[name]的值[服务管理]必须是唯一的;"
    }

    void "添加报表分组-测试5"() {
        given:"参数"
            def group = new ReportGroups(code:"02", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表");
        when:"执行方法"
            controller.addReportGroup(group)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            //this.messageSource.getMessage("default.x.x", [Report, ""], Locale.SIMPLIFIED_CHINESE)
            assert jsonData.message == "[报表分组]类的属性[code]的值[02]必须是唯一的;"
    }

    void accessParams(params, param) {
        params["code"] = param.code
        params["name"] = param.name
        params["comment"] = param.comment
    }

    void "编辑报表分组-测试1"() {
        given:"参数"
            def param = [code:"02", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表"];
            accessParams(controller.params, param)
        when:"执行方法"
            controller.editReportGroup()
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 1
            //this.messageSource.getMessage("default.x.x", [Report, ""], Locale.SIMPLIFIED_CHINESE)
            assert jsonData.message == "执行成功"
        when:"验证"
            def actualGroups = ReportGroups.findByCode("02")
        then:"验证结果"
            assert actualGroups
            assert actualGroups.code == "02"
            assert actualGroups.name == "运营简报"
            assert actualGroups.comment == "提现整体经营服务规模效果效益等内容的报表"
    }

    void "编辑报表分组-测试2"() {
        given:"参数"
            def param = [code:"02", name:"运营简", comment:"提现整体经营服务规模效果效益等内容的报"];
            accessParams(controller.params, param)
        when:"执行方法"
            controller.editReportGroup()
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 1
            //this.messageSource.getMessage("default.x.x", [Report, ""], Locale.SIMPLIFIED_CHINESE)
            assert jsonData.message == "执行成功"
        when:"验证"
            def actualGroups = ReportGroups.findByCode("02")
        then:"验证结果"
            assert actualGroups
            assert actualGroups.code == "02"
            assert actualGroups.name == "运营简"
            assert actualGroups.comment == "提现整体经营服务规模效果效益等内容的报"
    }

    void "编辑报表分组-测试3"() {
        given:"参数"
            def param = [code:"", name:"服务管理", comment:"提现整体经营服务规模效果效益等内容的报"];
            accessParams(controller.params, param)
        when:"执行方法"
            controller.editReportGroup()
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            //this.messageSource.getMessage("default.x.x", [Report, ""], Locale.SIMPLIFIED_CHINESE)
            assert jsonData.message == "编码不能为空"
    }

    void "编辑报表分组-测试4"() {
        given:"参数"
            def param = [code:"02", name:"", comment:"提现整体经营服务规模效果效益等内容的报"];
            accessParams(controller.params, param)
        when:"执行方法"
            controller.editReportGroup()
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            //this.messageSource.getMessage("default.x.x", [Report, ""], Locale.SIMPLIFIED_CHINESE)
            assert jsonData.message == "[报表分组]类的属性[name]不能为null;"
    }

    void "删除报表分组-测试1"() {
        given:"参数"
            def code = "02"
        when:"执行方法"
            controller.deleteReportGroup(code)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
        when:"验证"
            ReportGroups groups = ReportGroups.findByCode(code)
        then:"验证结果"
            assert groups == null
    }

    void "删除报表分组-测试2"() {
        given:"参数"
            def code = ""
        when:"执行方法"
            controller.deleteReportGroup(code)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "编码不能为空"
    }

    void "删除报表分组-测试3"() {
        given:"参数"
            def code = "1"
        when:"执行方法"
            controller.deleteReportGroup(code)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "报表分组不存在"
    }

    void "获取指定编码的报表分组信息-测试1"() {
        given:"参数"
            def code = "02"
        when:"执行方法"
            controller.getReportGroupByCode(code)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
    }

    void "获取指定编码的报表分组信息-测试2"() {
        given:"参数"
            def code = "1"
        when:"执行方法"
            controller.getReportGroupByCode(code)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "报表分组不存在"
    }

    void "获取指定编码的报表分组信息-测试3"() {
        given:"参数"
            def code = ""
        when:"执行方法"
            controller.getReportGroupByCode(code)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 3
            assert jsonData.message == "编码不能为空"
    }

    void "获取报表分组列表-测试1"() {
        given:"参数"
            def pageNow = null
            def pageSize = null
        when:"执行方法"
            controller.getReportGroupList(pageNow, pageSize)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
            assert jsonData.list == null
    }

    void "获取报表分组列表-测试2"() {
        given:"参数"
            def pageNow = 0
            def pageSize = 1
        when:"执行方法"
            controller.getReportGroupList(pageNow, pageSize)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 1
            def data = list.get(0)
            assert data.code == "02"
            assert data.name == "服务管理"
            assert data.comment == "有关服务工作开展情况和开展内容等信息的呈现"
    }

    void "根据名称或编码查询报表分组列表-测试1"() {
        given:"参数"
            String name = ""
            String code = ""
            Integer pageNow = null
            Integer pageSize = null
        when:"执行"
            controller.getByCondition(name, code, pageNow, pageSize)
        then:"结果"
            assert response.status == 200
            assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
            def jsonData = response.json
            assert jsonData
            assert jsonData.code == 1

            def list = jsonData.list
            assert list?.size() == 3

            def data = list.get(0)
            assert data.code == "02"
            assert data.name == "服务管理"
            assert data.comment == "有关服务工作开展情况和开展内容等信息的呈现"

            def data1 = list.get(1)
            assert data1.code == "03"
            assert data1.name == "药品管理"
            assert data1.comment == "有关药品等信息的呈现"

            def data2 = list.get(2)
            assert data2.code == "99"
            assert data2.name == "监控大屏"
            assert data2.comment == "内置专门存放监控大屏报表的分组"
    }

    void "根据名称或编码查询报表分组列表-测试2"() {
        given:"参数"
        String name = "服务管理"
        String code = ""
        Integer pageNow = null
        Integer pageSize = null
        when:"执行"
        controller.getByCondition(name, code, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size() == 1

        def data = list.get(0)
        assert data.code == "02"
        assert data.name == "服务管理"
        assert data.comment == "有关服务工作开展情况和开展内容等信息的呈现"
    }

    void "根据名称或编码查询报表分组列表-测试3"() {
        given:"参数"
        String name = ""
        String code = "03"
        Integer pageNow = null
        Integer pageSize = null
        when:"执行"
        controller.getByCondition(name, code, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size() == 1

        def data1 = list.get(0)
        assert data1.code == "03"
        assert data1.name == "药品管理"
        assert data1.comment == "有关药品等信息的呈现"
    }

    void "根据名称或编码查询报表分组列表-测试4"() {
        given:"参数"
        String name = ""
        String code = ""
        Integer pageNow = 0
        Integer pageSize = null
        when:"执行"
        controller.getByCondition(name, code, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size() == 3

        def data = list.get(0)
        assert data.code == "02"
        assert data.name == "服务管理"
        assert data.comment == "有关服务工作开展情况和开展内容等信息的呈现"

        def data1 = list.get(1)
        assert data1.code == "03"
        assert data1.name == "药品管理"
        assert data1.comment == "有关药品等信息的呈现"

        def data2 = list.get(2)
        assert data2.code == "99"
        assert data2.name == "监控大屏"
        assert data2.comment == "内置专门存放监控大屏报表的分组"
    }

    void "根据名称或编码查询报表分组列表-测试5"() {
        given:"参数"
        String name = ""
        String code = ""
        Integer pageNow = 0
        Integer pageSize = 2
        when:"执行"
        controller.getByCondition(name, code, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size() == 2

        def data = list.get(0)
        assert data.code == "02"
        assert data.name == "服务管理"
        assert data.comment == "有关服务工作开展情况和开展内容等信息的呈现"

        def data1 = list.get(1)
        assert data1.code == "03"
        assert data1.name == "药品管理"
        assert data1.comment == "有关药品等信息的呈现"
    }

    void "根据名称或编码查询报表分组列表-测试6"() {
        given:"参数"
        String name = "药品"
        String code = "03"
        Integer pageNow = 0
        Integer pageSize = 2
        when:"执行"
        controller.getByCondition(name, code, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size() == 1

        def data1 = list.get(0)
        assert data1.code == "03"
        assert data1.name == "药品管理"
        assert data1.comment == "有关药品等信息的呈现"
    }

    void "根据名称或编码查询报表分组列表-测试7"() {
        given:"参数"
        String name = "03"
        String code = "03"
        Integer pageNow = 0
        Integer pageSize = 1
        when:"执行"
        controller.getByCondition(name, code, pageNow, pageSize)
        then:"结果"
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = response.json
        assert jsonData
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size() == 0
    }
}

package com.zcareze.report.integrationtest

import com.zcareze.report.Report
import com.zcareze.report.ReportDatasource
import com.zcareze.report.ReportDatasourceController
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.web.controllers.ControllerUnitTest
import groovy.json.JsonSlurper
import spock.lang.Specification

@Integration
@Rollback
class ReportDatasourceControllerSpec extends Specification implements ControllerUnitTest<ReportDatasourceController> {
    void setupSpec() {

    }
    void setup() {
        new ReportDatasource(code: "00", name: "中心数据源", config: '{"kind":1}').save()
        new ReportDatasource(code: "01", name: "区域数据源", config: '{"kind":2}').save()
    }
    void cleanup() {
        Report.executeUpdate("delete ReportDatasource")
    }

    void accessParam(def controllerParams, def param) {
        controllerParams["code"] = param.code
        controllerParams["name"] = param.name
        controllerParams["config"] = param.config
    }
    void "保存数据源-测试1"() {
        given:"请求参数"
            String code = "99"
            String name = "数据中心"
            String config = '{"kind":1}'
            ReportDatasource datasource = new ReportDatasource(code: code, name: name, config: config)
        when:"执行"
            controller.request.method = "POST"
            controller.save(datasource)
        then:"结果"
            assert response.status == 200
            def jsonData = response.json
            assert jsonData.code == 1
            assert jsonData.message == "执行成功"
        when: "验证"
            ReportDatasource testDatasource = ReportDatasource.findByCodeAndName(code, name)
        then: "验证结果"
            assert testDatasource
            assert testDatasource.config == config
    }
    void "保存数据源-测试2"() {
        given:"请求参数"
        String code = "01"
        String name = "数据中心"
        String config = '{"kind":1}'
        ReportDatasource datasource = new ReportDatasource(code: code, name: name, config: config)
        when:"执行"
        controller.request.method = "POST"
        controller.save(datasource)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[数据源]类的属性[code]的值[01]必须是唯一的;"
    }
    void "保存数据源-测试3"() {
        given:"请求参数"
        String code = "99"
        String name = "中心数据源"
        String config = '{"kind":1}'
        ReportDatasource datasource = new ReportDatasource(code: code, name: name, config: config)
        when:"执行"
        controller.request.method = "POST"
        controller.save(datasource)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[数据源]类的属性[name]的值[中心数据源]必须是唯一的;"
    }
    void "保存数据源-测试4"() {
        given:"请求参数"
        String code = ""
        String name = "数据源"
        String config = '{"kind":1}'
        ReportDatasource datasource = new ReportDatasource(code: code, name: name, config: config)
        when:"执行"
        controller.request.method = "POST"
        controller.save(datasource)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[数据源]类的属性[code]不能为null;"
    }
    void "保存数据源-测试5"() {
        given:"请求参数"
        String code = "99"
        String name = ""
        String config = '{"kind":1}'
        ReportDatasource datasource = new ReportDatasource(code: code, name: name, config: config)
        when:"执行"
        controller.request.method = "POST"
        controller.save(datasource)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[数据源]类的属性[name]不能为null;"
    }
    void "保存数据源-测试6"() {
        given:"请求参数"
        String code = "99"
        String name = "数据源"
        String config = ''
        ReportDatasource datasource = new ReportDatasource(code: code, name: name, config: config)
        when:"执行"
        controller.request.method = "POST"
        controller.save(datasource)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "[数据源]类的属性[config]不能为null;"
    }
    void "保存数据源-测试7"() {
        given:"请求参数"
        String code = "99"
        String name = "数据中心"
        String config = '{"kind":0}'
        ReportDatasource datasource = new ReportDatasource(code: code, name: name, config: config)
        when:"执行"
        controller.request.method = "POST"
        controller.save(datasource)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "配置类型有误"
    }
    void "保存数据源-测试8"() {
        given:"请求参数"
        String code = "99"
        String name = "数据中心"
        String config = 'asd'
        ReportDatasource datasource = new ReportDatasource(code: code, name: name, config: config)
        when:"执行"
        controller.request.method = "POST"
        controller.save(datasource)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "配置格式有误"
    }
    void "保存数据源-测试9"() {
        given:"请求参数"
        String code = "99"
        String name = "数据中心"
        String config = '{"name":"s"}'
        ReportDatasource datasource = new ReportDatasource(code: code, name: name, config: config)
        when:"执行"
        controller.request.method = "POST"
        controller.save(datasource)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "配置格式有误"
    }
    void "保存数据源-测试10"() {
        given:"请求参数"
        String code = "99"
        String name = "数据中心"
        String config = '{"cloudId":"s"}'
        ReportDatasource datasource = new ReportDatasource(code: code, name: name, config: config)
        when:"执行"
        controller.request.method = "POST"
        controller.save(datasource)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "配置类型缺失"
    }
    void "保存数据源-测试11"() {
        given:"请求参数"
        String code = "99"
        String name = "数据中心"
        String config = '{"kind":3}'
        ReportDatasource datasource = new ReportDatasource(code: code, name: name, config: config)
        when:"执行"
        controller.request.method = "POST"
        controller.save(datasource)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 3
        assert jsonData.message == "特定区域数据源需要指定区域ID"
    }
    void "保存数据源-测试12"() {
        given:"请求参数"
        ReportDatasource existsDatasource = ReportDatasource.findByCode("00")
        String code = "00"
        String name = "数据中心"
        String config = '{"kind":1}'
        existsDatasource.code = code
        existsDatasource.name = name
        existsDatasource.config = config
        when:"执行"
        controller.request.method = "POST"
        controller.save(existsDatasource)
        then:"结果"
        assert response.status == 200
        def jsonData = response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when: "验证"
        ReportDatasource testDatasource = ReportDatasource.get(existsDatasource.id)
        then: "验证结果"
        assert testDatasource
        assert testDatasource.code == code
        assert testDatasource.name == name
        assert testDatasource.config == config
    }
    void "删除数据源-测试1"() {
        given: "参数"
        ReportDatasource existsDatasource = ReportDatasource.findByCode("00")
        Long id = existsDatasource.id
        when: "执行"
        controller.delete(id)
        then: "结果"
        assert response.status == 200
        def jsonData =  response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when: "验证"
        ReportDatasource test = ReportDatasource.get(id)
        then:
        assert test == null
    }
    void "删除数据源-测试2"() {
        given: "参数"
        Long id = 99
        when: "执行"
        controller.delete(id)
        then: "结果"
        assert response.status == 200
        def jsonData =  response.json
        assert jsonData.code == 3
        assert jsonData.message == "数据源不存在"
    }
    void "删除数据源-测试3"() {
        given: "参数"
        Long id = null
        when: "执行"
        controller.delete(id)
        then: "结果"
        assert response.status == 200
        def jsonData =  response.json
        assert jsonData.code == 3
        assert jsonData.message == "标识不能为空"
    }
    void "获取数据源-测试1"() {
        given: "参数"
        String code = "00"
        String name = ""
        Integer pageNow = 0
        Integer pageSize = 1
        when: "执行"
        controller.getByCondition(code, name, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData =  response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"

        def list = jsonData.list
        assert list
        assert list.size() == 1

        def item = list.get(0)
        assert item.code == "00"
        assert item.name == "中心数据源"
        assert item.config == '{"kind":1}'
    }
    void "获取数据源-测试2"() {
        given: "参数"
        String code = ""
        String name = "中心数据源"
        Integer pageNow = 0
        Integer pageSize = 1
        when: "执行"
        controller.getByCondition(code, name, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData =  response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"

        def list = jsonData.list
        assert list
        assert list.size() == 1

        def item = list.get(0)
        assert item.code == "00"
        assert item.name == "中心数据源"
        assert item.config == '{"kind":1}'
    }
    void "获取数据源-测试3"() {
        given: "参数"
        String code = "00"
        String name = "中心数据源"
        Integer pageNow = 0
        Integer pageSize = 1
        when: "执行"
        controller.getByCondition(code, name, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData =  response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"

        def list = jsonData.list
        assert list
        assert list.size() == 1

        def item = list.get(0)
        assert item.code == "00"
        assert item.name == "中心数据源"
        assert item.config == '{"kind":1}'
    }
    void "获取数据源-测试3-1"() {
        given: "参数"
        String code = "00"
        String name = "中心数据"
        Integer pageNow = 0
        Integer pageSize = 1
        when: "执行"
        controller.getByCondition(code, name, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData =  response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"

        def list = jsonData.list
        assert list == []
    }
    void "获取数据源-测试4"() {
        given: "参数"
        String code = ""
        String name = ""
        Integer pageNow = null
        Integer pageSize = null
        when: "执行"
        controller.getByCondition(code, name, pageNow, pageSize)
        then: "结果"
        assert response.status == 200
        def jsonData =  response.json
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"

        def list = jsonData.list
        assert list
        assert list.size() == 2

        def item = list.get(0)
        assert item.code == "00"
        assert item.name == "中心数据源"
        assert item.config == '{"kind":1}'

        def item1 = list.get(1)
        assert item1.code == "01"
        assert item1.name == "区域数据源"
        assert item1.config == '{"kind":2}'
    }
}

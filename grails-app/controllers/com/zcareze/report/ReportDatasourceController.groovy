package com.zcareze.report

import com.report.enst.DatasourceConfigKindEnum
import com.report.result.BaseResult
import com.report.result.Result
import com.report.vo.DatasourceConfigVO
import grails.converters.JSON
import grails.validation.ValidationException
import groovy.json.JsonSlurper

import static org.springframework.http.HttpStatus.*

class ReportDatasourceController {

    ReportDatasourceService reportDatasourceService

    /**
     * 获取
     */
    def getByCondition(String code, String name, Integer pageNow, Integer pageSize) {
        BaseResult<ReportDatasource> result = new BaseResult<>()
        result.list = ReportDatasource.createCriteria().list {
            if (code) {
                if (name) {
                    and {
                        eq("code", code)
                        eq("name", name)
                    }
                } else {
                    eq("code", code)
                }
            } else {
                if (name) {
                    eq("name", name)
                }
            }
            if (pageNow > -1 && pageSize > -1) {
                firstResult pageNow * pageSize
                maxResults pageSize
            }
            order "code", "asc"
        }
        render result as JSON
    }

    /**
     * 保存
     * @param reportDatasource
     * @return
     */
    def save(ReportDatasource reportDatasource) {
        if (!reportDatasource) {
            render Result.error("数据源不能为空") as JSON
            return
        }

        if (!reportDatasource.validate()) {
            def errorMessage = hasError(reportDatasource)
            render Result.error(errorMessage) as JSON
            return
        }

        // 配置
        String config = reportDatasource.config

        DatasourceConfigVO datasourceConfigVO = null
        try {
            def map = new JsonSlurper().parseText(config)
            if (!map) {
                render Result.error("配置格式有误") as JSON
                return
            }
            datasourceConfigVO = new DatasourceConfigVO(map)
        } catch (Exception) {
            render Result.error("配置格式有误") as JSON
            return
        }
        if (!datasourceConfigVO) {
            render Result.error("配置格式有误") as JSON
            return
        }

        // 配置类型
        Integer kind = datasourceConfigVO.kind
        if (kind < 0) {
            render Result.error("配置类型缺失") as JSON
            return
        }
        DatasourceConfigKindEnum datasourceConfigKindEnum = DatasourceConfigKindEnum.getEnumByKind(kind)
        if (!datasourceConfigKindEnum) {
            render Result.error("配置类型有误") as JSON
            return
        }

        // 配置区域ID
        String cloudId = datasourceConfigVO.cloudId
        // 配置类型为特定区域数据源时，需要特定的区域ID
        if (datasourceConfigKindEnum.equals(DatasourceConfigKindEnum.SPECIALLY)) {
            if (!cloudId) {
                render Result.error("特定区域数据源需要指定区域ID") as JSON
                return
            }
        }
        reportDatasource.save(flush:true)
        render Result.success() as JSON
    }

    /**
     * 删除
     * @param id
     * @return
     */
    def delete(Long id) {
        if (id == null) {
            render Result.error("标识不能为空") as JSON
            return
        }
        ReportDatasource dataSource = ReportDatasource.get(id)
        if (!dataSource) {
            render Result.error("数据源不存在") as JSON
            return
        }
        dataSource.delete(flush: true)
        render Result.success() as JSON
    }
}

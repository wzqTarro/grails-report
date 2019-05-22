package com.zcareze.report


import com.report.result.BaseResult
import com.report.result.Result
import com.report.service.IOrgListService
import com.report.vo.ReportOpenToVO
import grails.converters.JSON
import org.grails.web.json.JSONObject

import static org.springframework.http.HttpStatus.*

class ReportOpenToController {

    ReportOpenToService reportOpenToService

    IOrgListService orgListService

    String staffId = "1"
    String staffName = "王"

    def grailsDomainClassMappingContext

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    /**
     * 保存开放记录
     * @param reportOpenTo
     */
    def saveReportOpenTo(ReportOpenTo reportOpenTo) {
        if (reportOpenTo) {
            // 参数校验
            if (!reportOpenTo.validate()) {
                String errorMessage = hasError(reportOpenTo)
                render Result.error(errorMessage) as JSON
                return
            }

            // 组织树
            if (!reportOpenTo.orgTreeId) {
                reportOpenTo.orgTreeId = null
                Integer orgTreeLayer = reportOpenTo.orgTreeLayer
                if (!orgTreeLayer || orgTreeLayer <= 0) {
                    render Result.error("未指定组织，只能从第1级开始授权") as JSON
                    return
                }
            }

            // 外键校验
            if (!reportOpenTo.rpt) {
                render Result.error("报表不存在") as JSON
                return
            }
            if (!reportOpenTo.rpt.id) {
                render Result.error("报表不存在") as JSON
                return
            }
            reportOpenTo.granter = staffName

            reportOpenTo.save(flush: true)
            render Result.success() as JSON
            return
        }
        render Result.error("数据不能为空") as JSON
    }

    /**
     * 删除开放记录
     * @param id
     * @return
     */
    def deleteReportOpenTo(String id) {
        if (!id) {
            render Result.error("参数不能为空") as JSON
            return
        }
        ReportOpenTo openTo = ReportOpenTo.get(id)
        if (!openTo) {
            render Result.error("开放记录不存在") as JSON
            return
        }
        openTo.delete(flush: true)
        render Result.success() as JSON
    }

    /**
     * 获取指定报表的开放记录
     * @param rptId
     */
    def getReportOpenToList(String rptId) {
        BaseResult<ReportOpenToVO> result = new BaseResult<>()
        if (!rptId) {
            result.error = "报表标识不能为空"
            render result as JSON
            return
        }
        List<ReportOpenToVO> reportOpenToVOList = new ArrayList<>()
        List<ReportOpenTo> list = ReportOpenTo.createCriteria().list {
            rpt {
                eq("id", rptId)
            }
        }
        if (!list) {
            result.list = reportOpenToVOList
            render result as JSON
            return
        }
        Set<String> orgIdSet = new HashSet<>()
        list.each { openTo ->
            def orgTreeId = openTo.orgTreeId
            if (orgTreeId) {
                orgIdSet.add(openTo.orgTreeId)
            }
        }
        /**
         * TODO 组织机构树形菜单
         */
        def param = [isUpOrDown: true, list: orgIdSet]
        JSONObject parentOrgListJSONObj = orgListService.getOrgTreeList((param as JSON) as String)
        if (parentOrgListJSONObj.get("errcode") != 0) {
            result.error = "组织机构名称获取失败"
            render result as JSON
            return
        }
        Map<String, List<Map<String, Object>>> parentOrgListMap = parentOrgListJSONObj.get("map")
        list.each { openTo ->
            ReportOpenToVO reportOpenToVO = new ReportOpenToVO()
            bindData(reportOpenToVO, openTo)

            reportOpenToVO.rptId = openTo.rpt.id
            reportOpenToVO.id = openTo.id

            // 组织树名称
            String orgTreeName = ""
            def orgTreeId = openTo.orgTreeId
            if (orgTreeId) {
                List<Map<String, Object>> parentOrgList = parentOrgListMap.get(orgTreeId)
                orgTreeName = generateOrgTreeName(parentOrgList)
            }

            reportOpenToVO.orgTreeName = orgTreeName
            reportOpenToVOList.add(reportOpenToVO)
        }
        result.list = reportOpenToVOList
        render result as JSON
    }

    /**
     * 生成树形名称
     * @param nameList
     * @return
     */
    private String generateOrgTreeName(List<Map<String, Object>> orgMapList) {
        String orgName = ""
        if (orgMapList) {
            // 组成树形菜单 上级组织名称>下级组织名称>下下级组织名称
            for (int i = 0; i < orgMapList.size(); i++) {
                Map<String, Object> org = orgMapList.get(i)
                String name = org.get("orgName")
                if (i == (orgMapList.size() - 1)) {
                    orgName = orgName + name
                } else {
                    orgName = orgName + name + ">"
                }
            }
        }
        return orgName
    }
}

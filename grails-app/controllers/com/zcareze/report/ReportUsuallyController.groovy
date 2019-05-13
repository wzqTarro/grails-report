package com.zcareze.report


import com.report.enst.ResultEnum
import com.report.result.ApiWrapper
import com.report.result.BaseResult
import com.report.result.Result
import com.report.service.IOrgListService
import com.report.vo.ReportUsuallyVO
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONObject

import static org.springframework.http.HttpStatus.*

class ReportUsuallyController {

    ReportUsuallyService reportUsuallyService

    IOrgListService orgListService

    String staffId = "1"
    String staffName = "王"
    String cloudId = "1"
    boolean isCloudManage = false

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    /**
     * 添加我的常用报表
     * @param reportId
     */
    def addMyUsuallyReport(String reportId) {
        if (!reportId) {
            render Result.error("参数不能为空") as JSON
            return
        }

        Report report = Report.get(reportId)
        if (!report) {
            render Result.error("报表不存在") as JSON
            return
        }

        ReportUsually reportUsually = new ReportUsually()
        reportUsually.readTime = new Date()
        /**
         * TODO 职员信息
         */
        reportUsually.staffId = staffId
        reportUsually.rpt = report
        reportUsually.cloudId = cloudId

        // 当前最大排列号
        def seqNum = ReportUsually.withTenant(cloudId){
            ReportUsually.createCriteria().get {
                /**
                 * TODO 职员信息
                 */
                eq("staffId", staffId)
                projections {
                    max "seqNum"
                }
            }
        }

        reportUsually.seqNum  = seqNum > -1 ? seqNum + 1 : 0
        if (reportUsually.save(flush: true)) {
            render Result.success() as JSON
            return
        } else {
            String errorMessage = hasError(reportUsually)
            render Result.error(errorMessage) as JSON
        }

    }

    /**
     * 取消我的常用报表
     * @param reportId
     */
    def cancelMyUsuallyReport(String reportId) {
        if (!reportId) {
            render Result.error("参数不能为空") as JSON
            return
        }

        /**
         * TODO 职员信息
         */
        ReportUsually reportUsually = ReportUsually.createCriteria().get {
            and {
                rpt {
                    eq("id", reportId)
                }
                eq("staffId", staffId)
            }
        }
        if (!reportUsually) {
            render Result.error("常用报表不存在") as JSON
            return
        }
        reportUsually.delete(flush: true)
        render Result.success() as JSON
    }

    /**
     * api阅览我的常用报表
     * @param reportId
     * @return
     */
    def readMyUsuallyReportOld(String reportId) {
        ApiWrapper apiWrapper = new ApiWrapper()
        if (!reportId) {
            apiWrapper.errmsg = "参数不能为空"
            apiWrapper.errcode = ResultEnum.PARAM_ERROR.code
            render apiWrapper as JSON
            return
        }

        /**
         * TODO 职员信息
         */
        ReportUsually reportUsually = ReportUsually.createCriteria().get {
            and {
                rpt {
                    eq("id", reportId)
                }
                eq("staffId", staffId)
            }
        }
        if (!reportUsually) {
            apiWrapper.errmsg = "常用报表不存在"
            apiWrapper.errcode = ResultEnum.PARAM_ERROR.code
            render apiWrapper as JSON
            return
        }
        reportUsually.readTime = new Date()
        reportUsually.save(flush: true)
        render apiWrapper as JSON
    }

    /**
     * 阅览我的常用报表
     * @param reportId
     * @return
     */
    def readMyUsuallyReport(String reportId) {
        if (!reportId) {
            render Result.error("参数不能为空") as JSON
            return
        }

        /**
         * TODO 职员信息
         */
        ReportUsually reportUsually = ReportUsually.withTenant(cloudId){
            ReportUsually.createCriteria().get {
                and {
                    rpt {
                        eq("id", reportId)
                    }
                    eq("staffId", staffId)
                }
            }
        }
        if (!reportUsually) {
            render Result.error("常用报表不存在") as JSON
            return
        }
        reportUsually.readTime = new Date()
        reportUsually.save(flush: true)
        render Result.success() as JSON
    }
    /**
     * api-获取我的常用报表
     * @param scene
     */
    @Transactional(readOnly=true)
    def getMyUsuallyReportOld(Integer scene) {
        ApiWrapper wrapper = new ApiWrapper()
        List<ReportUsuallyVO> reportUsuallyVOList = new ArrayList<>()

        /**
         * TODO 职员信息
         */

        // 我的常用报表 关联表查询 还可以写HQL或者先查询主表再查询相应从表
        List<ReportUsually> reportUsuallyList = ReportUsually.withTenant(cloudId){
            ReportUsually.createCriteria().list {
                and {
                    rpt {
                        styleList {
                            eq("scene", scene)
                        }
                    }
                    eq("staffId", staffId)
                }
                order("seqNum", "asc")
                // 此写法仅支持sql库
                // sqlRestriction("rpt_id = (select rpt_id from report_style where scene = "+ scene +" )")
            }
        }
        if (!reportUsuallyList) {
            Map<String, Object> map = new HashMap<>()
            map.put("reportUsuallyVOs", reportUsuallyVOList)
            wrapper.bizData = map
            render wrapper as JSON
            return
        }

        // 如果是云管，查看所有我的常用报表，否则筛选
        if (isCloudManage) {
            reportUsuallyList.each { usually ->
                ReportUsuallyVO reportUsuallyVO = new ReportUsuallyVO()
                reportUsuallyVO.accessReportUsuallyVO(usually)
                reportUsuallyVOList.add(reportUsuallyVO)
            }
        } else {
            /**
             * TODO 调用组织机构接口-查询职员所属组织机构列表
             */
            JSONObject staffOrgJSONObj = orgListService.getOrgListByStaffId(staffId)
            List<Map<String, Object>> staffOrgList = staffOrgJSONObj.get("orgList")
            // 如果不属于任何组织，直接返回空
            if (!staffOrgList) {
                Map<String, Object> map = new HashMap<>()
                map.put("reportUsuallyVOs", reportUsuallyVOList)
                wrapper.bizData = map
                render wrapper as JSON
                return
            }

            // 报表对应开放记录
            Map<String, List<ReportOpenTo>> reportOpenToMap = new HashMap<>()
            // 开放记录的根组织和层级
            Map<String, Object> openToMap = new HashMap<>()
            // 遍历获取报表的开放记录
            reportUsuallyList.each { usually ->
                String rptId = usually.rpt.id

                // 报表开放记录
                List<ReportOpenTo> reportOpenToList = ReportOpenTo.createCriteria().list {
                    rpt {
                        eq("id", rptId)
                    }
                }
                if (!reportOpenToList) {
                    return
                }
                reportOpenToList.each { ReportOpenTo openTo ->
                    // 根组织ID
                    String orgTreeId = openTo.orgTreeId
                    // 组织层级
                    String orgTreeLayer = openTo.orgTreeLayer

                    openToMap.put(orgTreeId, orgTreeLayer)
                }

                reportOpenToMap.put(rptId, reportOpenToList)
            }
            /**
             * TODO 调用组织机构接口-查询组织和层级对应的职员的组织列表
             */
            JSONObject orgTreeJSONObj = orgListService.getOrgListByOrgTreeAndLayer((openToMap as JSON) as String)
            Map<String, List<Map<String, Object>>> orgTreeDTOMap = orgTreeJSONObj.get("map")

            // 遍历我的常用报表
            reportUsuallyList.each { usually ->
                // 报表
                Report report = usually.rpt

                // 报表ID
                String reportId = report.id

                // 如果是云管，查看所有我的常用报表，否则筛选
                if (!isCloudManage) {
                    /**
                     * TODO 筛选
                     */
                    // 角色是否有权限查看该报表
                    boolean policyFlag = false

                    // 根据授权列表判断职员是否有报表的权限
                    List<ReportGrantTo> reportGrantToList = ReportGrantTo.createCriteria().list {
                        rpt {
                            eq("id", reportId)
                        }
                    }

                    // 如果依旧没有查看的权限，就遍历开放记录判断是否有权限
                    if (!ReportGrantTo.checkReadPolicy(reportGrantToList, staffOrgList)) {
                        // 开放记录
                        List<ReportOpenTo> reportOpenToList = reportOpenToMap.get(reportId)
                        if (!ReportOpenTo.checkReadPolicy(reportOpenToList, orgTreeDTOMap)) {
                            return
                        }
                    }
                }

                // 角色有权限查看该报表
                ReportUsuallyVO reportUsuallyVO = new ReportUsuallyVO()
                reportUsuallyVO.accessReportUsuallyVO(usually)
                reportUsuallyVOList.add(reportUsuallyVO)
            }
        }
        Map<String, Object> map = new HashMap<>()
        map.put("reportUsuallyVOs", reportUsuallyVOList)
        wrapper.bizData = map
        render wrapper as JSON
    }
    /**
     * 获取我的常用报表
     * @param scene
     */
    @Transactional(readOnly = true)
    def getMyUsuallyReports(Integer scene) {
        BaseResult<ReportUsuallyVO> result = new BaseResult<>()
        List<ReportUsuallyVO> reportUsuallyVOList = new ArrayList<>()

        /**
         * TODO 职员信息
         */

        // 我的常用报表 关联表查询 还可以写HQL或者先查询主表再查询相应从表
        List<ReportUsually> reportUsuallyList = ReportUsually.withTenant(cloudId){
            ReportUsually.createCriteria().list {
                and {
                    rpt {
                        styleList {
                            eq("scene", scene)
                        }
                    }
                    eq("staffId", staffId)
                }
                order("seqNum", "asc")
                // 此写法仅支持sql库
                // sqlRestriction("rpt_id = (select rpt_id from report_style where scene = "+ scene +" )")
            }
        }
        if (!reportUsuallyList) {
            result.list = reportUsuallyVOList
            render result as JSON
            return
        }

        // 如果是云管，查看所有我的常用报表，否则筛选
        if (isCloudManage) {
            reportUsuallyList.each { usually ->
                ReportUsuallyVO reportUsuallyVO = new ReportUsuallyVO()
                reportUsuallyVO.accessReportUsuallyVO(usually)
                reportUsuallyVOList.add(reportUsuallyVO)
            }
        } else {
            /**
             * TODO 调用组织机构接口-查询职员所属组织机构列表
             */
            JSONObject staffOrgJSONObj = orgListService.getOrgListByStaffId(staffId)
            List<Map<String, Object>> staffOrgList = staffOrgJSONObj.get("orgList")
            // 如果不属于任何组织，直接返回空
            if (!staffOrgList) {
                result.list = reportUsuallyVOList
                render result as JSON
                return
            }

            // 报表对应开放记录
            Map<String, List<ReportOpenTo>> reportOpenToMap = new HashMap<>()
            // 开放记录的根组织和层级
            Map<String, Object> openToMap = new HashMap<>()
            // 遍历获取报表的开放记录
            reportUsuallyList.each { usually ->
                String rptId = usually.rpt.id

                // 报表开放记录
                List<ReportOpenTo> reportOpenToList = ReportOpenTo.createCriteria().list {
                    rpt {
                        eq("id", rptId)
                    }
                }
                if (!reportOpenToList) {
                    return
                }
                reportOpenToList.each { ReportOpenTo openTo ->
                    // 根组织ID
                    String orgTreeId = openTo.orgTreeId
                    // 组织层级
                    String orgTreeLayer = openTo.orgTreeLayer

                    openToMap.put(orgTreeId, orgTreeLayer)
                }

                reportOpenToMap.put(rptId, reportOpenToList)
            }
            /**
             * TODO 调用组织机构接口-查询组织和层级对应的职员的组织列表
             */
            JSONObject orgTreeJSONObj = orgListService.getOrgListByOrgTreeAndLayer((openToMap as JSON) as String)
            Map<String, List<Map<String, Object>>> orgTreeDTOMap = orgTreeJSONObj.get("map")

            // 遍历我的常用报表
            reportUsuallyList.each { usually ->
                // 报表
                Report report = usually.rpt

                // 报表ID
                String reportId = report.id

                // 如果是云管，查看所有我的常用报表，否则筛选
                if (!isCloudManage) {
                    /**
                     * TODO 筛选
                     */
                    // 角色是否有权限查看该报表
                    boolean policyFlag = false

                    // 根据授权列表判断职员是否有报表的权限
                    List<ReportGrantTo> reportGrantToList = ReportGrantTo.createCriteria().list {
                        rpt {
                            eq("id", reportId)
                        }
                    }

                    // 如果依旧没有查看的权限，就遍历开放记录判断是否有权限
                    if (!ReportGrantTo.checkReadPolicy(reportGrantToList, staffOrgList)) {
                        // 开放记录
                        List<ReportOpenTo> reportOpenToList = reportOpenToMap.get(reportId)
                        if (!ReportOpenTo.checkReadPolicy(reportOpenToList, orgTreeDTOMap)) {
                            return
                        }
                    }
                }

                // 角色有权限查看该报表
                ReportUsuallyVO reportUsuallyVO = new ReportUsuallyVO()
                reportUsuallyVO.accessReportUsuallyVO(usually)
                reportUsuallyVOList.add(reportUsuallyVO)
            }
        }
        result.list = reportUsuallyVOList
        render result as JSON
    }


    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'reportUsually.label', default: 'ReportUsually'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

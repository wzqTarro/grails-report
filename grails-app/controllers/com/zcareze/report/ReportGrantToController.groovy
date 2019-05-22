package com.zcareze.report

import com.report.common.CommonValue
import com.report.result.ApiWrapper
import com.report.result.BaseResult
import com.report.result.Result
import com.report.service.IOrgListService
import com.report.vo.ReportGrantToVO
import com.report.vo.ReportListVO
import com.report.vo.ReportManageVO
import com.report.vo.ReportUsuallyVO
import grails.converters.JSON

import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONObject

import static org.springframework.http.HttpStatus.*

@Transactional
class ReportGrantToController {

    ReportGrantToService reportGrantToService

    IOrgListService orgListService

    String currentStaffId = "1"
    String currentStaffName = "王"
    boolean isCloudManage = false
    String cloudId = "1"

    def grailsDomainClassMappingContext

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    /**
     * 新增报表授权
     * @param reportGrantTo
     */
    @Transactional
    def addReportGrantTo() {
        String rptId = params."rptId"
        if (!rptId) {
            render Result.error("报表标识不能为空") as JSON
            return
        }

        // 参数赋值
        ReportGrantTo grantTo = new ReportGrantTo()
        grantTo.properties = params

        // 报表标识列表
        String[] ids = rptId.split(";")
        if (ids) {
            for (int i = 0; i < ids.length; i++) {
                String id = ids[i]
                if (!id) {
                    continue
                }

                // 报表信息
                Report report = Report.get(id)
                if (!report) {
                    continue
                }
                grantTo.rpt = report

                /**
                 * TODO 授权人信息
                 */
                // 授权人信息
                grantTo.granter = currentStaffName
                grantTo.grantTime = new Date()

                // 授权角色
                String roles = grantTo.roles
                if (!roles) {
                    render Result.error("授权角色不能为空") as JSON
                    return
                }

                // 管理权默认为0
                if (!grantTo.manage) {
                    grantTo.manage = 0
                } else { // 只有业务管理可以设置管理权
                    def roleResult = roles.split(";").find {
                        it == "11"
                    }
                    if (!roleResult) {
                        render Result.error("只有业务管理可以设置管理权") as JSON
                        return
                    }
                }

                if (grantTo.save(flush:true)) {
                    render Result.success() as JSON
                    return
                } else {
                    String errorMessage = hasError(grantTo)
                    render Result.error(errorMessage) as JSON
                    return
                }
            }
        }
        render Result.success() as JSON
    }

    /**
     * 删除报表授权
     * @param reportId
     * @param orgId
     */
    def deleteReportGrantTo(String reportId, String orgId) {
        if (reportId && orgId) {
            // 指定报表授权记录
            ReportGrantTo grantTo = ReportGrantTo.createCriteria().get {
                and {
                    rpt {
                        eq("id", reportId)
                    }
                    eq("orgId", orgId)
                }
            }
            if (grantTo) {
                grantTo.delete(flush: true)
                render Result.success() as JSON
                return
            }
            render Result.error("授权记录不存在") as JSON
            return
        }
        render Result.error("报表和组织机构标识不能为空") as JSON
    }

    /**
     * 获取报表已有授权列表
     * @param reportId
     */
    @Transactional(readOnly=true)
    def getReportGrantToByRptId(String reportId) {
        BaseResult<ReportGrantToVO> result = new BaseResult<>()
        if (reportId) {
            List<ReportGrantToVO> reportGrantToVOList = new ArrayList<>()

            // 组织机构ID列表
            List<String> orgIdList = new ArrayList<>()
            List<ReportGrantTo> reportGrantToList = ReportGrantTo.createCriteria().list {
                rpt {
                    eq("id", reportId)
                }
            }
            if (!reportGrantToList) {
                result.list = reportGrantToVOList
                render result as JSON
                return
            }
            // 获取组织机构ID列表
            for (grantTo in reportGrantToList) {
                String orgId = grantTo.orgId
                orgIdList.add(orgId)
            }

            /**
             * TODO 调用组织机构接口-查询指定组织的所有上级组织名称
             */
            def param = [isUpOrDown: true, list: orgIdList]
            JSONObject orgListJSONObj = orgListService.getOrgTreeList((param as JSON) as String)
            Map<String, List<Map<String, Object>>> orgListMap = orgListJSONObj.get("map")

            for (grantTo in reportGrantToList) {
                String orgId = grantTo.orgId

                /**
                 * TODO 拼装上级组织树形名称
                 */
                List<Map<String, Object>> orgMapList = orgListMap.get(orgId)
                String orgName = generateOrgTreeName(orgMapList)

                ReportGrantToVO reportGrantToVO = new ReportGrantToVO()
                // 属性赋值
                bindData(reportGrantToVO, grantTo)
                // 报表ID
                reportGrantToVO.rptId = grantTo.rpt.id
                // 组织机构树形名称
                reportGrantToVO.orgName = orgName

                reportGrantToVOList.add(reportGrantToVO)
            }

            result.list = reportGrantToVOList
        } else {
            result.error = "参数不能为空"
        }
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
    /**
     * api 获取我有权限的报表
     * @param scene
     */
    @Transactional(readOnly = true)
    def getMyGrantToReportOld(Integer scene) {
        ApiWrapper wrapper = new ApiWrapper()
        List<ReportUsuallyVO> reportUsuallyVOs = new ArrayList<ReportUsuallyVO>();

        int pageStart = 0
        int pageSize = Integer.MAX_VALUE

        // 我的常用报表
        List<ReportUsually> reportUsuallyList = Report.withTenant(cloudId){
            ReportUsually.createCriteria().list {
                and{
                    rpt {
                        styleList {
                            eq("scene", scene)
                        }
                    }
                    eq("staffId", currentStaffId)
                }
            }
        }

        // 对应场景的非大屏的所有报表
        List<Report> reportList = Report.withTenant(cloudId){
            Report.createCriteria().list {
                and {
                    styleList {
                        eq("scene", scene)
                    }
                    ne("grpCode", "99")
                }
                order("code", "asc")
                firstResult pageStart
                maxResults pageSize
            }
        }
        if (!reportList) {
            Map<String, Object> map = new HashMap<>(1)
            map.put("reportList", reportUsuallyVOs)
            wrapper.bizData = map
            render wrapper as JSON
            return
        }

        // 如果是云管，可以看到所有报表
        if (isCloudManage) {

            reportList.eachWithIndex { report, index ->
                // 赋值
                ReportUsuallyVO reportUsuallyVO = accessReportUsuallyVO(report, reportUsuallyList, index + 1)

                reportUsuallyVOs.add(reportUsuallyVO)
            }
        } else {
            /**
             * TODO 调用组织机构接口-查询职员所属组织机构列表
             */
            List<Map<String, Object>> staffOrgList = getOrgListByStaffId(currentStaffId)

            reportUsuallyVOs = getPolicyReport(reportList, staffOrgList, reportUsuallyList, false)
        }
        Map<String, Object> map = new HashMap<>(1)
        map.put("reportList", reportUsuallyVOs)
        wrapper.bizData = map
        render wrapper as JSON
    }
    /**
     * 获取我有权限的报表
     * @param scene
     * @param pageNow
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    def getMyGrantToReports(Integer scene, Integer pageNow, Integer pageSize) {
        BaseResult<ReportUsuallyVO> result = new BaseResult<>()
        List<ReportUsuallyVO> reportUsuallyVOs = new ArrayList<ReportUsuallyVO>();

        int pageStart = 0
        if (pageNow > -1 && pageSize > -1) {
            pageStart = pageNow * pageSize
        }

        if (pageSize <= 0) {
            pageSize = Integer.MAX_VALUE
        }

        // 我的常用报表
        List<ReportUsually> reportUsuallyList = ReportUsually.withTenant(cloudId){
            ReportUsually.createCriteria().list {
                and{
                    rpt {
                        styleList {
                            eq("scene", scene)
                        }
                    }
                    eq("staffId", currentStaffId)
                }
            }
        }

        // 对应场景的非大屏的所有报表
        List<Report> reportList = Report.withTenant(cloudId){
            Report.createCriteria().list {
                and {
                    styleList {
                        eq("scene", scene)
                    }
                    ne("grpCode", "99")
                }
                order("code", "asc")
                firstResult pageStart
                maxResults pageSize
            }
        }
        if (!reportList) {
            result.list = reportUsuallyVOs
            render result as JSON
            return
        }

        // 如果是云管，可以看到所有报表
        if (isCloudManage) {

            reportList.eachWithIndex { report, index ->
                // 赋值
                ReportUsuallyVO reportUsuallyVO = accessReportUsuallyVO(report, reportUsuallyList, index + 1)

                reportUsuallyVOs.add(reportUsuallyVO)
            }
        } else {
            /**
             * TODO 调用组织机构接口-查询职员所属组织机构列表
             */
            List<Map<String, Object>> staffOrgList = getOrgListByStaffId(currentStaffId)

            reportUsuallyVOs = getPolicyReport(reportList, staffOrgList, reportUsuallyList, false)
        }
        result.list = reportUsuallyVOs
        render result as JSON
    }

    /**
     * 获取我有权限的监控大屏报表清单
     */
    @Transactional(readOnly = true)
    def getMyGrantScreenReportList() {
        BaseResult<ReportListVO> result = new BaseResult<>()
        List<ReportListVO> reportListVOList = new ArrayList<>()

        // 所有大屏报表
        List<Report> reportList = Report.withTenant(cloudId){
            Report.findAllByGrpCode("99", [sort: "code", order: "asc"])
        }
        if (!reportList) {
            result.list = reportListVOList
            render result as JSON
            return
        }

        // 如果是云管，可以看到所有报表
        if (isCloudManage) {

            reportList.eachWithIndex { report, index ->
                // 赋值
                ReportListVO reportListVO = new ReportListVO()
                bindData(reportListVO, report)

                reportListVOList.add(reportListVO)
            }
        } else {
            /**
             * TODO 调用组织机构接口-查询职员所属组织机构列表
             */
            List<Map<String, Object>> staffOrgList = getOrgListByStaffId(currentStaffId)

            reportListVOList = getPolicyReport(reportList, staffOrgList, null, true)
        }
        result.list = reportListVOList
        render result as JSON
    }

    /**
     * 获取有管理权限的报表（只有业务管理才有管理权）
     * @param orgId
     * @param pageNow
     * @param pageSize
     */
    @Transactional(readOnly = true)
    def getMyManageReports(String orgId, Integer pageNow, Integer pageSize) {
        BaseResult<ReportManageVO> result = new BaseResult<>()
        List<ReportManageVO> reportManageVOList = new ArrayList<>()

        int pageStart = 0
        if (pageNow > -1 && pageSize > -1) {
            pageStart = pageNow * pageSize
        }

        // 获取所有报表
        List<Report> reportList = Report.withTenant(cloudId){
            Report.createCriteria().list {
                ne("grpCode", "99")
                order("code", "asc")
                firstResult pageStart
                maxResults pageSize
            }
        }

        // 组织ID为空就是区域管理，需要获取所有报表
        if (!orgId) {
            // 授权组织ID列表
            Set<String> orgIdList = new HashSet<>()
            Map<String, ReportGrantTo> grantToMap = new HashMap<>()
            for (report in reportList) {
                String reportId = report.id
                Set<ReportGrantTo> grantToSet = report.grantToList

                grantToMap.put(reportId, grantToSet)

                if (!grantToSet) {
                    continue
                }

                grantToSet.each { grantTo ->
                    orgIdList.add(grantTo.orgId)
                }
            }

            /**
             * TODO 调用组织机构接口-查询指定组织的所有上级组织名称
             */
            def param = [isUpOrDown: true, list: orgIdList]
            JSONObject orgListJSONObj = orgListService.getOrgTreeList((param as JSON) as String)
            Map<String, List<Map<String, Object>>> orgListMap = orgListJSONObj.get("map")

            reportList.each { report ->
                ReportManageVO reportManageVO = new ReportManageVO()
                // 授权列表
                List<ReportGrantToVO> grantToVOList = new ArrayList<>()

                String orgName = ""
                Set<ReportGrantTo> grantToList = grantToMap.get(report.id)
                // 未授权
                if (!grantToList) {
                    orgName = CommonValue.ORG_NOGRANT
                } else {
                    grantToList.each { grantTo ->
                        /**
                         * TODO
                         */
                        // 组织机构上级组织名称列表
                        List<Map<String, Object>> orgMapList = orgListMap.get(grantTo.orgId)

                        // 树形名称
                        String orgTreeName = generateOrgTreeName(orgMapList)

                        ReportGrantToVO reportGrantToVO = new ReportGrantToVO()
                        bindData(reportGrantToVO, grantTo)
                        reportGrantToVO.orgName = orgTreeName
                        grantToVOList.add(reportGrantToVO)
                    }
                }

                reportManageVO.accessReport(report, orgId, orgName)
                reportManageVO.grantList = grantToVOList
                reportManageVOList.add(reportManageVO)
            }
        } else {
            if (reportList) {
                // 所有报表授权的组织ID
                Set<String> orgIdSet = new HashSet<>()
                Map<String, ReportGrantTo> grantToMap = new HashMap<>()
                for (report in reportList) {
                    // 授权列表
                    Set<ReportGrantTo> grantToSet = report.grantToList
                    grantToMap.put(report.id, grantToSet)

                    if (!grantToSet) {
                        continue
                    }
                    grantToSet.each { grantTo ->
                        String grantToOrgId = grantTo.orgId
                        orgIdSet.add(grantToOrgId)
                    }
                }

                /**
                 * TODO 调用组织机构接口-查询指定组织的所有下级组织名称
                 */
                def param = [isUpOrDown: false, list: orgIdSet]
                JSONObject childOrgListJSONObj = orgListService.getOrgTreeList((param as JSON) as String)
                Map<String, List<Map<String, Object>>> childOrgListMap = childOrgListJSONObj.get("map")


                /**
                 * TODO 调用组织机构接口-查询指定组织的所有上级组织名称
                 */
                param["isUpOrDown"] = true
                JSONObject parentOrgListJSONObj = orgListService.getOrgTreeList((param as JSON) as String)
                Map<String, List<Map<String, Object>>> parentOrgListMap = parentOrgListJSONObj.get("map")

                for (report in reportList) {
                    // 授权列表-每个报表对应每一个组织有且仅有一个授权信息
                    Set<ReportGrantTo> grantToSet = grantToMap.get(report.id)
                    // 未授权
                    if (!grantToSet) {
                        continue
                    }

                    // 报表ID
                    String reportId = report.id

                    List<ReportGrantToVO> grantList = new ArrayList<>()
                    grantToSet.each { grantTo ->
                        String grantToOrgId = grantTo.orgId
                        Integer parentManage = grantTo.manage

                        // 没有管理权并且没有授予对应的组织机构
                        if (grantToOrgId != orgId || parentManage != 1) {
                            return
                        }

                        // 上级组织列表
                        if (!parentOrgListMap.containsKey(grantToOrgId)) {
                            return
                        }
                        List<Map<String, Object>> parentOrgList = parentOrgListMap.get(grantToOrgId)
                        String orgTreeName = generateOrgTreeName(parentOrgList)

                        ReportGrantToVO reportGrantToVO = new ReportGrantToVO()
                        bindData(reportGrantToVO, grantTo.properties)
                        reportGrantToVO.orgName = orgTreeName
                        grantList.add(reportGrantToVO)

                        // 下级组织列表
                        if (!childOrgListMap.containsKey(grantToOrgId)) {
                            return
                        }
                        List<Map<String, Object>> childOrgList = childOrgListMap.get(grantToOrgId)
                        if (!childOrgList) {
                            return
                        }
                        // 如果下级组织授权了，不管有没有管理权，也需要显示
                        for (int i = 1; i < childOrgList.size(); i++) {
                            Map<String, Object> childOrgMap = childOrgList.get(i)
                            String childOrgId = childOrgMap.get("orgId")

                            // 没有授权
                            if (!orgIdSet.contains(childOrgId)) {
                                continue
                            }

                            // 下级组织授权记录
                            ReportGrantTo childGrantTo = grantToSet.find { child ->
                                child.rpt.id == reportId && child.orgId == childOrgId
                            }
                            if (!childGrantTo) {
                                continue
                            }

                            // 上级组织列表
                            if (!parentOrgListMap.containsKey(childOrgId)) {
                                continue
                            }
                            parentOrgList = parentOrgListMap.get(childOrgId)
                            String childOrgTreeName = generateOrgTreeName(parentOrgList)

                            ReportGrantToVO childReportGrantToVO = new ReportGrantToVO()
                            bindData(childReportGrantToVO, childGrantTo)
                            childReportGrantToVO.orgName = childOrgTreeName
                            grantList.add(childReportGrantToVO)
                        }
                    }

                    if (grantList) {
                        ReportManageVO reportManageVO = new ReportManageVO()
                        reportManageVO.accessReport(report, orgId, "")
                        reportManageVO.grantList = grantList

                        reportManageVOList.add(reportManageVO)
                    }
                }
            }
        }
        result.list = reportManageVOList
        render result as JSON
    }

    /**
     * 赋值
     * @param reportUsuallyVO 返回前端的格式
     * @param report 报表
     * @param reportUsuallyList 常用报表列表
     */
    private ReportUsuallyVO accessReportUsuallyVO(Report report, List<ReportUsually> reportUsuallyList, int seqNum) {
        ReportUsuallyVO reportUsuallyVO = new ReportUsuallyVO()
        reportUsuallyVO.seqNum = seqNum

        // 报表参数赋值
        reportUsuallyVO.accessReportVO(report)

        // 是否是常用报表
        reportUsuallyVO.usually = false

        // 我的常用报表
        if (reportUsuallyList) {
            // 是否为我的常用报表
            for (usually in reportUsuallyList) {
                if (report.id == usually.rpt.id) {
                    reportUsuallyVO.usually = true
                    break
                }
            }
        }
        return reportUsuallyVO
    }

    /**
     * 调用组织机构接口-查询职员所属组织机构列表
     * @param staffId
     * @return
     */
    private List<Map<String, Object>> getOrgListByStaffId(String staffId) {
        /**
         * TODO 调用组织机构接口-查询职员所属组织机构列表
         */
        JSONObject staffOrgJSONObj = orgListService.getOrgListByStaffId(staffId)
        List<Map<String, Object>> staffOrgList = staffOrgJSONObj.get("orgList")
        return staffOrgList
    }

    /**
     * 筛选有权限的报表
     * @param reportList 报表列表
     * @param staffOrgList 职员所属组织列表
     * @param reportUsuallyList 职员的常用报表，筛选非大屏报表时需要传递
     * @param isScreen 是否为大屏报表
     * @return
     */
    private def getPolicyReport(List<Report> reportList, List<Map<String, Object>> staffOrgList, List<ReportUsually> reportUsuallyList, boolean isScreen) {
        // 普通报表返回列表格式
        List<ReportUsuallyVO> reportUsuallyVOList = new ArrayList<>();
        // 大屏报表返回列表格式
        List<ReportListVO> reportListVOList = new ArrayList<>();

        // 报表对应开放记录
        Map<String, List<ReportOpenTo>> reportOpenToMap = new HashMap<>()
        // 开放记录的根组织和层级
        Map<String, Object> openToMap = new HashMap<>()
        // 报表开放记录
        List<ReportOpenTo> reportOpenToList = ReportOpenTo.findAll()
        if (reportOpenToList) {
            reportOpenToList.each { ReportOpenTo openTo ->
                // 报表ID
                String rptId = openTo.rpt.id
                // 根组织ID
                String orgTreeId = openTo.orgTreeId
                // 组织层级
                String orgTreeLayer = openTo.orgTreeLayer

                openToMap.put(orgTreeId, orgTreeLayer)

                if (reportOpenToMap.containsKey(rptId)) {
                    List<ReportOpenTo> list = reportOpenToMap.get(rptId)
                    list.add(openTo)
                    reportOpenToMap.put(rptId, list)
                } else {
                    List<ReportOpenTo> list = new ArrayList<>()
                    list.add(openTo)
                    reportOpenToMap.put(rptId, list)
                }
            }
        }
        /**
         * TODO 调用组织机构接口-查询组织和层级对应的组织列表
         */
        JSONObject staffOrgTreeJSONObj = orgListService.getOrgListByOrgTreeAndLayer((openToMap as JSON) as String)
        Map<String, List<Map<String, Object>>> staffOrgTreeMap = staffOrgTreeJSONObj.get("map")

        // 遍历获取报表的开放记录
        reportList.eachWithIndex { report, index ->
            String rptId = report.id

            // 根据授权列表判断职员是否有报表的权限
            List<ReportGrantTo> reportGrantToList = ReportGrantTo.createCriteria().list {
                rpt {
                    eq("id", rptId)
                }
            }

            // 如果依旧没有查看的权限，就遍历开放记录判断是否有权限
            if (!ReportGrantTo.checkReadPolicy(reportGrantToList, staffOrgList)) {
                // 开放记录
                List<ReportOpenTo> currentOpenToList = reportOpenToMap.get(rptId)
                if (!ReportOpenTo.checkReadPolicy(currentOpenToList, staffOrgTreeMap)) {
                    return
                }
            }

            // 角色有权限查看该报表
            if (isScreen) { // 大屏
                ReportListVO reportListVO = new ReportListVO()
//                reportListVO.accessReportProperties(grailsDomainClassMappingContext, report)
                bindData(reportListVO, report.properties)
                reportListVOList.add(reportListVO)
            } else { // 普通报表
                ReportUsuallyVO reportUsuallyVO = accessReportUsuallyVO(report, reportUsuallyList, index + 1)
                reportUsuallyVOList.add(reportUsuallyVO)
            }
        }
        if (isScreen) {
            return reportListVOList
        } else {
            return reportUsuallyVOList
        }
    }

}

package com.zcareze.report

import com.report.enst.CtrlKindEnum
import com.report.enst.CtrlStatusEnum
import com.report.result.BaseResult
import com.report.result.Result
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class ReportCtrlLogController {

    ReportCtrlLogService reportCtrlLogService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    /**
     * 提交审核
     * @param reportId
     */
    @Transactional
    def submit(String reportId) {
        if (!reportId) {
            render Result.error("报表标识不能为空") as JSON
            return
        }
        Report report = Report.get(reportId)
        if (!report) {
            render Result.error("报表不存在") as JSON
            return
        }
        // 报表提交审核，原状态必须为草稿状态
        if (CtrlStatusEnum.BZZCY.code != report.ctrlStatus) {
            render Result.error("当前报表不是草稿状态") as JSON
            return
        }
        ReportCtrlLog ctrlLog = new ReportCtrlLog()
        ctrlLog.report = report
        ctrlLog.ctrlKind = CtrlKindEnum.SUBMIT.kind
        // 原状态
        ctrlLog.preStatus = report.ctrlStatus
        // 管理状态
        report.ctrlStatus = CtrlStatusEnum.SUBMIT_AUDIT.code
        // 现状态
        ctrlLog.newStatus = CtrlStatusEnum.SUBMIT_AUDIT.code
        ctrlLog.cloudId = report.cloudId
        ctrlLog.logTime = new Date()
        ctrlLog.accountId = "1"
        ctrlLog.accountName = "王"
        ctrlLog.save(flush: true)
        render Result.success() as JSON
    }
    /**
     * 停用、审核
     * @param ctrlLogKind
     * @param reportCtrlStatus
     * @param reportId
     * @param adscript
     * @return
     */
    private def commonJudge(CtrlKindEnum ctrlLogKind, CtrlStatusEnum reportCtrlStatus, String reportId, String adscript) {
        if (!reportId) {
            return Result.error("报表标识不能为空") as JSON
        }
        Report report = Report.get(reportId)
        if (!report) {
            return Result.error("报表不存在") as JSON
        }
        // 审核状态
        if (CtrlKindEnum.AUDIT == ctrlLogKind) {
            if (CtrlStatusEnum.SUBMIT_AUDIT.code != report.ctrlStatus) {
                return Result.error("当前报表需要提交审核") as JSON
            }
        }
        ReportCtrlLog ctrlLog = new ReportCtrlLog()
        ctrlLog.report = report
        ctrlLog.ctrlKind = ctrlLogKind.kind
        // 原状态
        ctrlLog.preStatus = report.ctrlStatus
        // 管理状态
        report.ctrlStatus = reportCtrlStatus.code
        // 现状态
        ctrlLog.newStatus = reportCtrlStatus.code
        ctrlLog.cloudId = report.cloudId
        ctrlLog.logTime = new Date()
        ctrlLog.accountId = "1"
        ctrlLog.accountName = "王"
        ctrlLog.adscript = adscript
        ctrlLog.save(flush: true)
        return Result.success() as JSON
    }
    /**
     * 停用
     * @param reportId
     * @param cloudId
     * @param adscript 附加说明
     * @return
     */
    @Transactional
    def stop(String reportId, String adscript) {
        render commonJudge(CtrlKindEnum.STOP_USING, CtrlStatusEnum.STOP_USING, reportId, adscript)
    }
    /**
     * 审核通过
     * @param reportId
     * @param cloudId
     * @param adscript
     */
    @Transactional
    def auditSuccess(String reportId, String adscript) {
        render commonJudge(CtrlKindEnum.AUDIT, CtrlStatusEnum.AUDIT_SUCCESS, reportId, adscript)
    }
    /**
     * 审核退回
     * @param reportId
     * @param cloudId
     * @param adscript
     */
    @Transactional
    def auditRollback(String reportId, String adscript) {
        render commonJudge(CtrlKindEnum.AUDIT, CtrlStatusEnum.AUDIT_ERROR, reportId, adscript)
    }
    /**
     * 重启用
     * @param reportId
     * @param adscript
     * @return
     */
    def reboot(String reportId, String adscript) {
        if (!reportId) {
            render Result.error("报表标识不能为空") as JSON
            return
        }
        Report rpt = Report.get(reportId)
        if (!rpt) {
            render Result.error("报表不存在") as JSON
            return
        }
        // 管理状态
        def ctrlStatus = rpt.ctrlStatus
        if (ctrlStatus != CtrlStatusEnum.STOP_USING.code) {
            render Result.error("报表未停用") as JSON
            return
        }
        // 最新停用记录
        List<ReportCtrlLog> ctrlLogList = ReportCtrlLog.createCriteria().list {
            report{
                eq("id", reportId)
            }
            order "logTime", "desc"
        }
        if (!ctrlLogList || ctrlLogList.size() < 2) {
            render Result.error("停用记录为空，数据有误") as JSON
            return
        }
        ReportCtrlLog stopCtrlLog = ctrlLogList.get(0)
        ReportCtrlLog preStopCtrlLog = ctrlLogList.get(1)
        // 停用前报表状态状态
        Integer preStatus = stopCtrlLog.preStatus
        // 停用前管理日志状态
        String preCtrlKind = preStopCtrlLog.ctrlKind

        ReportCtrlLog ctrlLog = new ReportCtrlLog()
        ctrlLog.report = rpt
        ctrlLog.ctrlKind = preCtrlKind
        // 原状态
        ctrlLog.preStatus = rpt.ctrlStatus
        // 管理状态
        rpt.ctrlStatus = preStatus
        // 现状态
        ctrlLog.newStatus = preStatus
        ctrlLog.cloudId = rpt.cloudId
        ctrlLog.adscript = adscript
        ctrlLog.logTime = new Date()
        ctrlLog.accountId = "1"
        ctrlLog.accountName = "王"
        ctrlLog.save(flush: true)
        render Result.success() as JSON
    }
    /**
     * 将区域报表转为中心报表
     * @param reportId
     * @param adscript
     * @return
     */
    def toCenterReport(String reportId, String adscript) {
        if (!reportId) {
            render Result.error("报表标识不能为空") as JSON
            return
        }
        Report report = Report.get(reportId)
        if (!report) {
            render Result.error("报表不存在") as JSON
            return
        }

        ReportCtrlLog ctrlLog = new ReportCtrlLog()
        ctrlLog.report = report
        ctrlLog.ctrlKind = CtrlKindEnum.TO_CENTER.kind
        // 原状态
        ctrlLog.preStatus = report.ctrlStatus
        // 现状态
        ctrlLog.newStatus = report.ctrlStatus
        ctrlLog.cloudId = report.cloudId
        report.cloudId = null
        ctrlLog.adscript = adscript
        ctrlLog.logTime = new Date()
        ctrlLog.accountId = "1"
        ctrlLog.accountName = "王"
        ctrlLog.save(flush: true)
        render Result.success() as JSON
    }
    /**
     * 取消中心共享
     * @param reportId
     * @param cloudId
     * @param adscript
     * @return
     */
    def cancelCenterReport(String reportId, String cloudId, String adscript) {
        if (!reportId) {
            render Result.error("报表标识不能为空") as JSON
            return
        }
        if (!cloudId) {
            render Result.error("区域云标识不能为空") as JSON
            return
        }
        Report report = Report.get(reportId)
        if (!report) {
            render Result.error("报表不存在") as JSON
            return
        }

        ReportCtrlLog ctrlLog = new ReportCtrlLog()
        ctrlLog.report = report
        ctrlLog.ctrlKind = CtrlKindEnum.CANCEL_CENTER.kind
        // 原状态
        ctrlLog.preStatus = report.ctrlStatus
        // 现状态
        ctrlLog.newStatus = report.ctrlStatus
        ctrlLog.cloudId = cloudId
        report.cloudId = cloudId
        ctrlLog.adscript = adscript
        ctrlLog.logTime = new Date()
        ctrlLog.accountId = "1"
        ctrlLog.accountName = "王"
        ctrlLog.save(flush: true)
        render Result.success() as JSON
    }

    /**
     * 查询管控记录
     * @param reportName
     * @param ctrlKind
     * @param pageNow
     * @param pageSize
     * @return
     */
    def getByCondition(String reportName, String ctrlKind, Integer pageNow, Integer pageSize) {
        BaseResult<ReportCtrlLog> result = new BaseResult<>()
        result.list = ReportCtrlLog.createCriteria().list {
            if (reportName) {
                if (ctrlKind) {
                    and{
                        report{
                            ilike("name", "%"+reportName+"%")
                        }
                        eq("ctrlKind", ctrlKind)
                    }
                } else {
                    report{
                        ilike("name", "%"+reportName+"%")
                    }
                }
            } else {
                if (ctrlKind) {
                    eq("ctrlKind", ctrlKind)
                }
            }
            order "logTime", "desc"
            if (pageNow > -1 && pageSize > -1) {
                firstResult pageNow * pageSize
                maxResults pageSize
            }
        }
        render result as JSON
    }
}

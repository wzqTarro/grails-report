package com.report.vo

import com.report.enst.InputDataTypeEnum
import com.zcareze.report.Report
import com.zcareze.report.ReportGroups
import com.zcareze.report.ReportInputs
import com.zcareze.report.ReportUsually

/**
 * 报表基本信息 (用于界面分析首页和报表中心)
 */
class ReportUsuallyVO implements Serializable{
    // 报表ID
    String reportId
    // 报表名称
    String reportName
    // 报表分组编码
    String groupCode
    // 报表分组名称
    String groupName
    // 界面显示颜色
    String color
    // 顺序号
    Integer seqNum
    // 是否个人常用报表
    Boolean usually
    // 是否需要查阅
    Boolean noRead
    // 最后修改时间（时间戳）
    Long updateTime
    // 授权状态 0:有授权;1:无授权（授权被取消）
    Integer grantTo

    /**
     *
     * @param reportGroups
     * @param report
     * @return
     */
    void accessReportUsuallyVO(ReportUsually reportUsually) {

        this.usually = true
        this.seqNum = reportUsually.seqNum

        // 报表
        Report report = reportUsually.rpt

        // 是否需要查阅
        boolean read = isRead(report.inputList, reportUsually.readTime)

        this.noRead = read
        accessReportVO(report)
    }

    void accessReportVO(Report report) {
        if (report) {
            // 报表
            this.reportId = report.id
            this.reportName = report.name
            this.updateTime = report.editTime.getTime()

            // 报表分组
            ReportGroups reportGroups = ReportGroups.findByCode(report.grpCode)
            if (reportGroups) {
                this.groupName = reportGroups.name
                this.groupCode = reportGroups.code
                this.color = reportGroups.color
            }
        }
    }

    /**
     * 是否不需要提示查阅
     */
    private boolean isRead(Set<ReportInputs> reportInputsList, Date readTime) {
        if (!reportInputsList) {
            return false
        }
        ReportInputs reportInputs = null

        // 取输入参数的最小期间
        for (inputs in reportInputsList) {
            InputDataTypeEnum inputDataTypeEnum = InputDataTypeEnum.getEnumByDataType(inputs.dataType)
            if (!inputDataTypeEnum) {
                continue
            }
            if (inputDataTypeEnum == InputDataTypeEnum.TYPE_DAY) {
                reportInputs = inputs
                break
            }
            if (inputDataTypeEnum == InputDataTypeEnum.TYPE_MONTH) {
                reportInputs = inputs
                continue
            }
            if (inputDataTypeEnum == InputDataTypeEnum.TYPE_YEAR) {
                reportInputs = inputs
                continue
            }
        }

        Date calDate = null
        if (reportInputs) {
            Calendar calender = new Date().toCalendar()

            InputDataTypeEnum inputDataTypeEnum = InputDataTypeEnum.getEnumByDataType(reportInputs.dataType)
            switch (inputDataTypeEnum) {
                case InputDataTypeEnum.TYPE_DAY:
                    calDate = new Date() - 1
                    break
                case InputDataTypeEnum.TYPE_MONTH:
                    // 获取本月第一天
                    calender.set(Calendar.DAY_OF_MONTH, calender.getActualMinimum(Calendar.DAY_OF_MONTH));
                    calDate = calender.getTime()
                    break
                case InputDataTypeEnum.TYPE_YEAR:
                    calender.add(Calendar.MONTH, -cal.get(Calendar.MONTH));
                    calDate = calender.getTime();
                    break
                default:
                    break
            }
        }

        if (readTime == null || (calDate != null && calDate.before(readTime))) {
            return true
        } else {
            return false
        }
    }
}

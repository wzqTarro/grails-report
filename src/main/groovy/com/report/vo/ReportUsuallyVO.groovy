package com.report.vo

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
}

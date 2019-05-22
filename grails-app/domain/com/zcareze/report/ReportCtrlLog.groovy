package com.zcareze.report

/**
 * 报表管控记录
 */
class ReportCtrlLog {
    String id
    // 原状态
    Integer preStatus
    // 管控性质
    String ctrlKind
    // 附加说明
    String adscript
    // 区域云ID
    String cloudId
    // 现状态
    Integer newStatus
    // 记录时间
    Date logTime
    // 账户ID
    String accountId
    // 账户姓名
    String accountName

    static belongsTo = [report: Report]
    static constraints = {
        cloudId(nullable: true)
        adscript(nullable: true)
    }

    static mapping = {
        table "report_ctrl_log"

        id generator: 'uuid', column: 'id', sqlType: 'char', length: 32
        report column: 'report_id', sqlType: 'char', length: 32
        preStatus column: 'pre_status', sqlType: 'int', length: 1
        ctrlKind column: 'ctrl_kind', sqlType: 'varchar', length: 1
        adscript column: 'adscript', sqlType: 'varchar', length: 100
        cloudId column: 'cloud_id', sqlType: 'char', length: 32
        newStatus column: 'new_status', sqlType: 'int', length: 1
        accountId column: 'account_id', sqlType: 'char', length: 32
        accountName column: 'account_name', sqlType: 'varchar', length: 30
    }
}

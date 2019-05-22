package com.zcareze.report

/**
 * 数据源-报表可以使用的数据源配置，固定包括：区域数据源，动态代表当前云
 */
class ReportDatasource implements Serializable{
    // 编码
    String code
    // 名称
    String name
    // 配置
    String config

    static constraints = {
        code(unique: true)
        name(unique: true)
    }

    static mapping = {
        code(column: 'code', sqlType: 'varchar', length: 2, unique: true)
        name(column: 'name', sqlType: 'varchar', length: 10, unique: true)
        config(column: 'config', sqlType: 'varchar', length: 200)
    }
}

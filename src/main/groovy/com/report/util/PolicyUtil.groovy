package com.report.util

import com.report.dto.OrgDTO

class PolicyUtil {

    /**
     * 匹配工作种类
     * @param roles 已授权角色
     * @param classes 职员所属工作种类
     * @return
     */
    static boolean mateRoles(String roles, String classes) {
        if (roles && classes) {
            def result = classes.trim().split(";").find {
                roles.trim().indexOf(it) > -1 // roles.indexOf(it) 如果为-1的话返回的是true
            }
            if (result) {
                return true
            }
        }
        return false
    }
}

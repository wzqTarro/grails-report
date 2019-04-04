package com.report.util

import com.report.common.CommonValue
import com.report.dto.ReportParamValue
import com.report.enst.QueryInputDefTypeEnum
import com.report.enst.ReportSystemParamEnum
import com.zcareze.report.ReportInputs

import java.util.regex.Pattern

import static com.report.enst.ReportSystemParamEnum.*
import static com.report.enst.QueryInputDefTypeEnum.*

class CommonUtil {
    /**
     * 解析sql中的参数，参数包含[]
     * @param sql
     * @return
     */
    static LinkedList<String> analysisSql(String sql) {
        List<String> paramList = new LinkedList<>()
        if (sql) {
            // sql 中包含参数
            if (sql.contains(CommonValue.PARAM_PREFIX) && sql.contains(CommonValue.PARAM_SUFFIX)) {
                String[] sqlArray = sql.split(Pattern.quote(CommonValue.PARAM_PREFIX))
                for (int i = 1; i < sqlArray.length; i++) {
                    String param = sqlArray[i]
                    if (sqlArray[i].contains(CommonValue.PARAM_SUFFIX)) {
                        paramList.add(param[0..param.indexOf(CommonValue.PARAM_SUFFIX)] + CommonValue.PARAM_PREFIX)
                    }
                }
            }
        }
        return paramList
    }

    /**
     * 获取系统参数的值
     * @param paramName
     * @return
     */
    static ReportParamValue getSystemParamValue(String paramName) {
        if (!paramName) {
            return null
        }
        ReportParamValue paramValue = new ReportParamValue()
        paramValue.name = paramName
        ReportSystemParamEnum systemParamEnum = ReportSystemParamEnum.getEnumByName(paramName)
        if (systemParamEnum) {
            /**
             * TODO 系统参数值来源
             */
            switch (systemParamEnum) { // 枚举使用switch，需要引入枚举类的静态包import static com.report.enst.ReportSystemParamEnum.*
                case CURRENT_DEPART :
                    paramValue.setTitle("显示的科室名称");
                    paramValue.setValue("科室的标识");
                    break;
                case CURRENT_ORG :
                    paramValue.setTitle("显示的机构名称");
                    paramValue.setValue("机构的标识");
                    break;
                case CURRENT_TEAM :
                    paramValue.setTitle("显示的团队名称");
                    paramValue.setValue("团队的标识");
                    break;
                case CURRENT_USER :
                    paramValue.setTitle("显示的职员名称");
                    paramValue.setValue("职员的标识");
                    break;
            }
        }
    }

    public static void main(String[] args) {
        def now  = new Date().toCalendar()
     now.add(Calendar.YEAR, 1)
             println now.getTime()

    }

    /**
     * 获取输入参数值
     * @param inputs
     * @return
     */
    static ReportParamValue getInputParamValue(QueryInputDefTypeEnum queryInputDefTypeEnum) {
        ReportParamValue paramValue = new ReportParamValue()
        if (queryInputDefTypeEnum) {
            /**
             * TODO 数据来源
             */
            def nowCalendar = new Date().toCalendar()
            switch (queryInputDefTypeEnum) {
                case ORG_ALL :
                    paramValue.setTitle("所有机构");
                    paramValue.setValue("");
                    break;
                case ORG_MY :
                    /**
                     * TODO
                     */
                    paramValue.setTitle("我的机构的名称");
                    paramValue.setValue("我的机构的值");
                    break;
                case DEPAMENT_ALL :
                    paramValue.setTitle("所有科室");
                    paramValue.setValue("");
                    break;
                case DEPAMENT_MY :
                    /**
                     * TODO
                     */
                    paramValue.setTitle("我的科室的名称");
                    paramValue.setValue("我的科室的值");
                    break;
                case TEAM_ALL :
                    paramValue.setTitle("所有团队");
                    paramValue.setValue("");
                    break;
                case TEAM_MY :
                    /**
                     * TODO
                     */
                    paramValue.setTitle("我的团队的名称");
                    paramValue.setValue("我的团队的值");
                    break;
                case YEAR_LAST :
                    nowCalendar.add(Calendar.YEAR, -1)
                    paramValue.setValue(nowCalendar.getTime().format("yyyy"));
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy"));
                    break;
                case YEAR_NOW :
                    paramValue.setTitle(new Date().format("yyyy"));
                    paramValue.setValue(new Date().format("yyyy"));
                    break;
                case MONTH_LAST :
                    nowCalendar.add(Calendar.MONTH, -1);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMM"));
                    break;
                case MONTH_NEXT :
                    nowCalendar.add(Calendar.MONTH, 1);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMM"));
                    break;
                case MONTH_NOW :
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMM"));
                    break;
                case DATE_LAST :
                    nowCalendar.add(Calendar.DAY_OF_YEAR, -1);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
                case DATE_NEXT :
                    nowCalendar.add(Calendar.DAY_OF_YEAR, 1);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
                case DATE_LAST_7 :
                    nowCalendar.add(Calendar.DAY_OF_YEAR, -7);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
                case DATE_LAST_MONTH :
                    nowCalendar.add(Calendar.MONTH, -1);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
                case DATE_MONTH_FIRST :
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-01"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMM01"));
                    break;
                case DATE_MONTH_LAST :
                    nowCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    nowCalendar.add(Calendar.MONTH, 1);
                    nowCalendar.add(Calendar.DAY_OF_MONTH, -1);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
                case DATE_NEXT_7 :
                    nowCalendar.add(Calendar.DAY_OF_YEAR, 7);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
                case DATE_NOW :
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
            }
            return paramValue
        }
        return null
    }
}

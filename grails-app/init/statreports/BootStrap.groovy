package statreports

import com.zcareze.report.ReportGrantTo
import com.zcareze.report.ReportGroups
import com.zcareze.report.ReportGroupsController
import com.zcareze.report.ReportInputs
import com.zcareze.report.Report
import com.zcareze.report.ReportOpenTo
import com.zcareze.report.ReportStyle
import com.zcareze.report.ReportTables
import com.zcareze.report.ReportUsually

/**
 * 启动时执行初始化
 */
class BootStrap {

    def grailsApplication

    def init = { servletContext ->

        def hasError = { Object domain ->
            def errors = [], errorMessage = new StringBuilder()
            domain.errors.allErrors.collect(errors) { err -> message(error: err) }
            errors.eachWithIndex { error, index ->
                errorMessage << "${index + 1}.${error};"
            }
            errorMessage as String
        }
        // 元编程动态加入新方法
        //ReportGroupsController.metaClass.hasError = hasError

        grailsApplication.controllerClasses.each { controller ->
            controller.metaClass.hasError = hasError
        }


        // 分组
        def group1 = new ReportGroups(code:"01", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表", color: "ffc100");
        def group2 = new ReportGroups(code:"02", name:"服务管理", comment:"有关服务工作开展情况和开展内容等信息的呈现", color: "7BAFA1");
        def group3 = new ReportGroups(code:"99", name:"监控大屏", comment:"内置专门存放监控大屏报表的分组", color: "b8e986");
        group1.save();
        group2.save();
        group3.save();

        // 报表
        def report1 = new Report(code: "KZRZB", name: "科主任周报", grpCode: group1.code, runway: 1, editorName: "王", editorId: "1",);
        def report2 = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: group2.code, runway: 2, editorName: "王", editorId: "1");
        def report3 = new Report(code: "TZYB", name: "医生团队月报", grpCode: group1.code, runway: 1, editorName: "王", editorId: "1");
        def report4 = new Report(code: "YYYCD", name: "用药依从性统计", grpCode: group2.code, runway: 1, editorName: "王", editorId: "1");

        report1.save();
        new ReportInputs(rpt: report1, name: "orgid", caption: "机构", seqNum: 0, dataType: "31", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构" ).save()
        new ReportStyle(rpt: report1, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt").save()
        new ReportTables(rpt: report1, name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1).save()
        new ReportGrantTo(rpt: report1, orgId: "0b695b6aaabf492bb8e1cb1c08bd4810", roles: "11", manage: 1, granter: "王").save()
        new ReportGrantTo(rpt: report1, orgId: "2c4dca2cddce43f09c496b6dd85bbccb", roles: "03;04;11", manage: 1, granter: "王").save()
        new ReportGrantTo(rpt: report1, orgId: "3a68c0179bcc4532aedcf7e0a37d7fa7", roles: "01;02;11", manage: 1, granter: "王").save()

        report2.save();
        new ReportInputs(rpt: report2, name: "endtime", caption: "结束时间", seqNum: 3, dataType: "23", inputType: 1, defType: "今天" ).save()
        new ReportInputs(rpt: report2, name: "month", caption: "月份", seqNum: 0, dataType: "12", inputType: 2, optionList: "1;2;3;4;5;6;7;8;9;10;11;12", defValue: "12" ).save()
        new ReportInputs(rpt: report2, name: "name", caption: "医生", seqNum: 2, dataType: "11", inputType: 1).save()
        new ReportInputs(rpt: report2, name: "year", caption: "开始时间", seqNum: 1, dataType: "21", inputType: 1, defType: "本年").save()
        new ReportStyle(rpt: report2, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt").save()
        new ReportStyle(rpt: report2, scene: 1, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/6a04cb4e3ed9420aa1f1c881650325ff.xslt").save()
        new ReportTables(rpt: report2, name: "tb0", sqlText: "select A.*,B.name as residentName from rsdt_contract_list as A,resident_list as B where A.resident_id=B.id AND service_months=[month] AND accept_time\n" +
                "BETWEEN date_format([year], '%Y%m%d')\n" +
                "   and date_format([endtime], '%Y%m%d')\n" +
                "  AND doctor_name like  CONCAT('%',[name],'%')  LIMIT 30", seqNum: 1).save()
        new ReportGrantTo(rpt: report2, orgId: "3a68c0179bcc4532aedcf7e0a37d7fa7", roles: "01;02;03;06;11", manage: 1, granter: "王").save()
        new ReportGrantTo(rpt: report2, orgId: "e43fd001573540029ef7565f1c1ea00e", roles: "01;11", manage: 1, granter: "王").save()
        new ReportGrantTo(rpt: report2, orgId: "ef325711521b11e6bbd8d017c2939671", roles: "11;02", manage: 0, granter: "王").save()

        report3.save();
        new ReportStyle(rpt: report3, scene: 2, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/3913c97922e4485d93fd5a4f7e05bd65/0e8f634358764214a508c77abafcd55a.xslt").save()
        new ReportTables(rpt: report3, name: "tb0", sqlText: "select name,abbr,gender,birthday,archive_address from resident_list LIMIT 30", seqNum: 1).save()
        new ReportGrantTo(rpt: report3, orgId: "020989d41dd74304aeae673eaffd86d3", roles: "01;02", manage: 0, granter: "王").save()
        new ReportGrantTo(rpt: report3, orgId: "ee1fe1d63d7f4aa99fedc17c69fbc4c5", roles: "02", manage: 0, granter: "王").save()

        report4.save();
        new ReportStyle(rpt: report4, scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/608639880900448ead762ca2246659ce/464ff931e29645088c3fbd325bed5227.xslt").save()
        new ReportTables(rpt: report4, name: "tb0", sqlText: "select name,abbr,gender,birthday,archive_address from resident_list LIMIT 30", seqNum: 1).save()
        new ReportGrantTo(rpt: report4, orgId: "020989d41dd74304aeae673eaffd86d3", roles: "01;02", manage: 0, granter: "王").save()
        new ReportGrantTo(rpt: report4, orgId: "ee1fe1d63d7f4aa99fedc17c69fbc4c5", roles: "02", manage: 0, granter: "王").save()
        new ReportOpenTo(rpt: report4, orgTreeId: "020989d41dd74304aeae673eaffd86d3", orgTreeLayer: 0, roles: "02", granter: "王").save()

        // 个人常用报表
        new ReportUsually(staffId: "1", rpt: report1, seqNum: 1).save();
        new ReportUsually(staffId: "1", rpt: report2, seqNum: 3).save();
        new ReportUsually(staffId: "1", rpt: report3, seqNum: 2).save();

        new ReportUsually(staffId: "2", rpt: report1, seqNum: 1).save();

    }
    def destroy = {
    }
}

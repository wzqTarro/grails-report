package com.zcareze.report

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Ignore
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
@Ignore
class ReportCtrlLogServiceSpec extends Specification {

    ReportCtrlLogService reportCtrlLogService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new ReportCtrlLog(...).save(flush: true, failOnError: true)
        //new ReportCtrlLog(...).save(flush: true, failOnError: true)
        //ReportCtrlLog reportCtrlLog = new ReportCtrlLog(...).save(flush: true, failOnError: true)
        //new ReportCtrlLog(...).save(flush: true, failOnError: true)
        //new ReportCtrlLog(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //reportCtrlLog.id
    }

    void "test get"() {
        setupData()

        expect:
        reportCtrlLogService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<ReportCtrlLog> reportCtrlLogList = reportCtrlLogService.list(max: 2, offset: 2)

        then:
        reportCtrlLogList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        reportCtrlLogService.count() == 5
    }

    void "test delete"() {
        Long reportCtrlLogId = setupData()

        expect:
        reportCtrlLogService.count() == 5

        when:
        reportCtrlLogService.delete(reportCtrlLogId)
        sessionFactory.currentSession.flush()

        then:
        reportCtrlLogService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        ReportCtrlLog reportCtrlLog = new ReportCtrlLog()
        reportCtrlLogService.save(reportCtrlLog)

        then:
        reportCtrlLog.id != null
    }
}

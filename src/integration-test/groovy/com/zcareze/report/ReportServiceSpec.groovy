package com.zcareze.report

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Ignore
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
@Ignore
class ReportServiceSpec extends Specification {

    ReportService reportService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new Report(...).save(flush: true, failOnError: true)
        //new Report(...).save(flush: true, failOnError: true)
        //Report report = new Report(...).save(flush: true, failOnError: true)
        //new Report(...).save(flush: true, failOnError: true)
        //new Report(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //report.id
    }

    void "test get"() {
        setupData()

        expect:
        reportService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Report> reportList = reportService.list(max: 2, offset: 2)

        then:
        reportList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        reportService.count() == 5
    }

    void "test delete"() {
        Long reportId = setupData()

        expect:
        reportService.count() == 5

        when:
        reportService.delete(reportId)
        sessionFactory.currentSession.flush()

        then:
        reportService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        Report report = new Report()
        reportService.save(report)

        then:
        report.id != null
    }
}

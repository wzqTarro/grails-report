package com.zcareze.report

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class ReportUsuallyServiceSpec extends Specification {

    ReportUsuallyService reportUsuallyService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new ReportUsually(...).save(flush: true, failOnError: true)
        //new ReportUsually(...).save(flush: true, failOnError: true)
        //ReportUsually reportUsually = new ReportUsually(...).save(flush: true, failOnError: true)
        //new ReportUsually(...).save(flush: true, failOnError: true)
        //new ReportUsually(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //reportUsually.id
    }

    void "test get"() {
        setupData()

        expect:
        reportUsuallyService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<ReportUsually> reportUsuallyList = reportUsuallyService.list(max: 2, offset: 2)

        then:
        reportUsuallyList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        reportUsuallyService.count() == 5
    }

    void "test delete"() {
        Long reportUsuallyId = setupData()

        expect:
        reportUsuallyService.count() == 5

        when:
        reportUsuallyService.delete(reportUsuallyId)
        sessionFactory.currentSession.flush()

        then:
        reportUsuallyService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        ReportUsually reportUsually = new ReportUsually()
        reportUsuallyService.save(reportUsually)

        then:
        reportUsually.id != null
    }
}

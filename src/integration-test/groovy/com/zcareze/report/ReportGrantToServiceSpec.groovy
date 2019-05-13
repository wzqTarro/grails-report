package com.zcareze.report

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Ignore
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
@Ignore
class ReportGrantToServiceSpec extends Specification {

    ReportGrantToService reportGrantToService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new ReportGrantTo(...).save(flush: true, failOnError: true)
        //new ReportGrantTo(...).save(flush: true, failOnError: true)
        //ReportGrantTo reportGrantTo = new ReportGrantTo(...).save(flush: true, failOnError: true)
        //new ReportGrantTo(...).save(flush: true, failOnError: true)
        //new ReportGrantTo(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //reportGrantTo.id
    }

    void "test get"() {
        setupData()

        expect:
        reportGrantToService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<ReportGrantTo> reportGrantToList = reportGrantToService.list(max: 2, offset: 2)

        then:
        reportGrantToList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        reportGrantToService.count() == 5
    }

    void "test delete"() {
        Long reportGrantToId = setupData()

        expect:
        reportGrantToService.count() == 5

        when:
        reportGrantToService.delete(reportGrantToId)
        sessionFactory.currentSession.flush()

        then:
        reportGrantToService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        ReportGrantTo reportGrantTo = new ReportGrantTo()
        reportGrantToService.save(reportGrantTo)

        then:
        reportGrantTo.id != null
    }
}

package com.zcareze.report

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class ReportOpenToServiceSpec extends Specification {

    ReportOpenToService reportOpenToService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new ReportOpenTo(...).save(flush: true, failOnError: true)
        //new ReportOpenTo(...).save(flush: true, failOnError: true)
        //ReportOpenTo reportOpenTo = new ReportOpenTo(...).save(flush: true, failOnError: true)
        //new ReportOpenTo(...).save(flush: true, failOnError: true)
        //new ReportOpenTo(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //reportOpenTo.id
    }

    void "test get"() {
        setupData()

        expect:
        reportOpenToService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<ReportOpenTo> reportOpenToList = reportOpenToService.list(max: 2, offset: 2)

        then:
        reportOpenToList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        reportOpenToService.count() == 5
    }

    void "test delete"() {
        Long reportOpenToId = setupData()

        expect:
        reportOpenToService.count() == 5

        when:
        reportOpenToService.delete(reportOpenToId)
        sessionFactory.currentSession.flush()

        then:
        reportOpenToService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        ReportOpenTo reportOpenTo = new ReportOpenTo()
        reportOpenToService.save(reportOpenTo)

        then:
        reportOpenTo.id != null
    }
}

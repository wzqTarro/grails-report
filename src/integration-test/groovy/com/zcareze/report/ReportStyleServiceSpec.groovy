package com.zcareze.report

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Ignore
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
@Ignore
class ReportStyleServiceSpec extends Specification {

    ReportStyleService reportStyleService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new ReportStyle(...).save(flush: true, failOnError: true)
        //new ReportStyle(...).save(flush: true, failOnError: true)
        //ReportStyle reportStyle = new ReportStyle(...).save(flush: true, failOnError: true)
        //new ReportStyle(...).save(flush: true, failOnError: true)
        //new ReportStyle(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //reportStyle.id
    }

    void "test get"() {
        setupData()

        expect:
        reportStyleService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<ReportStyle> reportStyleList = reportStyleService.list(max: 2, offset: 2)

        then:
        reportStyleList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        reportStyleService.count() == 5
    }

    void "test delete"() {
        Long reportStyleId = setupData()

        expect:
        reportStyleService.count() == 5

        when:
        reportStyleService.delete(reportStyleId)
        sessionFactory.currentSession.flush()

        then:
        reportStyleService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        ReportStyle reportStyle = new ReportStyle()
        reportStyleService.save(reportStyle)

        then:
        reportStyle.id != null
    }
}

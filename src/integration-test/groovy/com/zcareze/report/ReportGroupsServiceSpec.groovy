package com.zcareze.report

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Ignore
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
@Ignore
class ReportGroupsServiceSpec extends Specification {

    ReportGroupsService reportGroupsService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new ReportGroups(...).save(flush: true, failOnError: true)
        //new ReportGroups(...).save(flush: true, failOnError: true)
        //ReportGroups reportGroups = new ReportGroups(...).save(flush: true, failOnError: true)
        //new ReportGroups(...).save(flush: true, failOnError: true)
        //new ReportGroups(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //reportGroups.id
    }

    void "test get"() {
        setupData()

        expect:
        reportGroupsService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<ReportGroups> reportGroupsList = reportGroupsService.list(max: 2, offset: 2)

        then:
        reportGroupsList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        reportGroupsService.count() == 5
    }

    void "test delete"() {
        Long reportGroupsId = setupData()

        expect:
        reportGroupsService.count() == 5

        when:
        reportGroupsService.delete(reportGroupsId)
        sessionFactory.currentSession.flush()

        then:
        reportGroupsService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        ReportGroups reportGroups = new ReportGroups()
        reportGroupsService.save(reportGroups)

        then:
        reportGroups.id != null
    }
}

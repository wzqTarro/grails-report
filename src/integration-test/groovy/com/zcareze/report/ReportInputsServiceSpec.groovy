package com.zcareze.report

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Ignore
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
@Ignore
class ReportInputsServiceSpec extends Specification {

    ReportInputsService reportInputsService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new ReportInputs(...).save(flush: true, failOnError: true)
        //new ReportInputs(...).save(flush: true, failOnError: true)
        //ReportInputs reportInputs = new ReportInputs(...).save(flush: true, failOnError: true)
        //new ReportInputs(...).save(flush: true, failOnError: true)
        //new ReportInputs(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //reportInputs.id
    }

    void "test get"() {
        setupData()

        expect:
        reportInputsService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<ReportInputs> reportInputsList = reportInputsService.list(max: 2, offset: 2)

        then:
        reportInputsList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        reportInputsService.count() == 5
    }

    void "test delete"() {
        Long reportInputsId = setupData()

        expect:
        reportInputsService.count() == 5

        when:
        reportInputsService.delete(reportInputsId)
        sessionFactory.currentSession.flush()

        then:
        reportInputsService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        ReportInputs reportInputs = new ReportInputs()
        reportInputsService.save(reportInputs)

        then:
        reportInputs.id != null
    }
}

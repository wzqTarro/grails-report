package com.zcareze.report

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class ReportTablesServiceSpec extends Specification {

    ReportTablesService reportTablesService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new ReportTables(...).save(flush: true, failOnError: true)
        //new ReportTables(...).save(flush: true, failOnError: true)
        //ReportTables reportTables = new ReportTables(...).save(flush: true, failOnError: true)
        //new ReportTables(...).save(flush: true, failOnError: true)
        //new ReportTables(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //reportTables.id
    }

    void "test get"() {
        setupData()

        expect:
        reportTablesService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<ReportTables> reportTablesList = reportTablesService.list(max: 2, offset: 2)

        then:
        reportTablesList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        reportTablesService.count() == 5
    }

    void "test delete"() {
        Long reportTablesId = setupData()

        expect:
        reportTablesService.count() == 5

        when:
        reportTablesService.delete(reportTablesId)
        sessionFactory.currentSession.flush()

        then:
        reportTablesService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        ReportTables reportTables = new ReportTables()
        reportTablesService.save(reportTables)

        then:
        reportTables.id != null
    }
}

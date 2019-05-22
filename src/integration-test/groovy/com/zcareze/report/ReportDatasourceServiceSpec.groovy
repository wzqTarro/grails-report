package com.zcareze.report

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Ignore
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
@Ignore
class ReportDatasourceServiceSpec extends Specification {

    ReportDatasourceService reportDatasourceService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new ReportDatasource(...).save(flush: true, failOnError: true)
        //new ReportDatasource(...).save(flush: true, failOnError: true)
        //ReportDatasource reportDatasource = new ReportDatasource(...).save(flush: true, failOnError: true)
        //new ReportDatasource(...).save(flush: true, failOnError: true)
        //new ReportDatasource(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //reportDatasource.id
    }

    void "test get"() {
        setupData()

        expect:
        reportDatasourceService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<ReportDatasource> reportDatasourceList = reportDatasourceService.list(max: 2, offset: 2)

        then:
        reportDatasourceList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        reportDatasourceService.count() == 5
    }

    void "test delete"() {
        Long reportDatasourceId = setupData()

        expect:
        reportDatasourceService.count() == 5

        when:
        reportDatasourceService.delete(reportDatasourceId)
        sessionFactory.currentSession.flush()

        then:
        reportDatasourceService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        ReportDatasource reportDatasource = new ReportDatasource()
        reportDatasourceService.save(reportDatasource)

        then:
        reportDatasource.id != null
    }
}

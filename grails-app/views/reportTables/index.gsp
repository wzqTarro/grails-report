<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'reportTables.label', default: 'ReportTables')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#list-reportTables" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
            </ul>
        </div>
        <div id="list-reportTables" class="content scaffold-list" role="main">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            %{--<f:table collection="${reportTablesList}" />--}%
            <table>
                <tr>
                    <th>reportId</th><th>name</th><th>sqlText</th><th>seqNum</th>
                </tr>
                <g:each  in="${reportTablesList}" var="reportTable">
                    <tr>
                        <td><g:link controller="report" action="show" params="[id: reportTable.report.id]">${reportTable.report.id}</g:link></td>
                        <td><g:link controller="reportTables" action="show" params="[reportId: reportTable.report.id, name: reportTable.name]">${reportTable.name}</g:link></td>
                        <td>${reportTable.sqlText}</td>
                        <td>${reportTable.seqNum}</td>
                    </tr>
                </g:each>
            </table>
            <div class="pagination">
                <g:paginate total="${reportTablesCount ?: 0}" />
            </div>
        </div>
    </body>
</html>
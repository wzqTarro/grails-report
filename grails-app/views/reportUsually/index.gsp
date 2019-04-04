<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'reportUsually.label', default: 'ReportUsually')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#list-reportUsually" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
            </ul>
        </div>
        <div id="list-reportUsually" class="content scaffold-list" role="main">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            %{--<f:table collection="${reportUsuallyList}" />--}%
            <table>
                <tr>
                    <th>staffId</th><th>reportId</th><th>seqNum</th><th>readTime</th><th>groupCode</th><th>groupName</th><th>groupColor</th>
                </tr>
                <g:each in="${reportUsuallyList}" var="${usually}">
                    <tr>
                        <td><g:link controller="reportUsually" action="show" params="[staffId: usually.staffId, reportId: usually.report.id]">${usually.staffId}</g:link></td>
                        <td><g:link controller="report" action="show" params="[id: usually.report.id]">${usually.report.id}</g:link></td>
                        <td>${usually.seqNum}</td>
                        <td>${usually.lastUpdated}</td>
                        <td>${usually.groupCode}</td>
                        <td>${usually.groupName}</td>
                        <td>${usually.groupColor}</td>
                    </tr>
                </g:each>

            </table>
            <div class="pagination">
                <g:paginate total="${reportUsuallyCount ?: 0}" />
            </div>
        </div>
    </body>
</html>
<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'reportGrantTo.label', default: 'ReportGrantTo')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#list-reportGrantTo" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
            </ul>
        </div>
        <div id="list-reportGrantTo" class="content scaffold-list" role="main">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            %{--<f:table collection="${reportGrantToList}" />--}%
            <table>
               <tr>
                   <th>reportId</th><th>orgId</th><th>roles</th><th>manage</th><th>granter</th><th>grantTime</th>
               </tr>
                <g:each in="${reportGrantToList}" var="reportGrantTo">
                    <tr>
                        <td><g:link controller="report" action="show" params="[id: reportGrantTo.rpt.id]">${reportGrantTo.rpt.id}</g:link></td>
                        <td><g:link controller="reportGrantTo" action="show" params="[reportId: reportGrantTo.rpt.id, orgId: reportGrantTo.orgId]">${reportGrantTo.orgId}</g:link></td>
                        <td>${reportGrantTo.roles}</td>
                        <td>${reportGrantTo.manage}</td>
                        <td>${reportGrantTo.granter}</td>
                        <td>${reportGrantTo.grantTime}</td>
                    </tr>
                </g:each>
            </table>
            <div class="pagination">
                <g:paginate total="${reportGrantToCount ?: 0}" />
            </div>
        </div>
    </body>
</html>
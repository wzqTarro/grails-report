<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'reportStyle.label', default: 'ReportStyle')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#list-reportStyle" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
            </ul>
        </div>
        <div id="list-reportStyle" class="content scaffold-list" role="main">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            %{--<f:table collection="${reportStyleList}" />--}%
            <table>
                <tr>
                    <th>reportId</th><th>scene</th><th>fileUrl</th><th>chart</th><th>comment</th>
                </tr>
                <g:each in="${reportStyleList}" var="reportStyle">
                    <tr>
                        <td><g:link controller="report" action="show" params="[id: reportStyle.report.id]">${reportStyle.report.id}</g:link></td>
                        <td><g:link controller="reportStyle" action="show" params="[reportId: reportStyle.report.id, scene: reportStyle.scene]">${reportStyle.scene}</g:link></td>
                        <td>${reportStyle.fileUrl}</td>
                        <td>${reportStyle.chart}</td>
                        <td>${reportStyle.comment}</td>
                    </tr>
                </g:each>
            </table>
            <div class="pagination">
                <g:paginate total="${reportStyleCount ?: 0}" />
            </div>
        </div>
    </body>
</html>
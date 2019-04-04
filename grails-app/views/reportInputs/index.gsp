<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'reportInputs.label', default: 'ReportInputs')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#list-reportInputs" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
            </ul>
        </div>
        <div id="list-reportInputs" class="content scaffold-list" role="main">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            %{--<f:table collection="${reportInputsList}" />--}%
            <table>
                <tr><th>reportId</th><th>name</th><th>caption</th><th>seqNum</th><th>dataType</th><th>inputType</th><th>optionList</th><th>sqlText</th><th>defValue</th><th>defType</th></tr>
                <g:each in="${reportInputsList}" var="reportInputs">
                    <tr>
                        <td><g:link controller="report" action="show" params="[id: reportInputs.report.id]">${reportInputs.report.id}</g:link></td>
                        <td><g:link controller="reportInputs" action="show" params="[reportId: reportInputs.report.id, name: reportInputs.name]">${reportInputs.name}</g:link></td>
                        <td>${reportInputs.caption}</td>
                        <td>${reportInputs.seqNum}</td>
                        <td>${reportInputs.dataType}</td>
                        <td>${reportInputs.inputType}</td>
                        <td>${reportInputs.optionList}</td>
                        <td>${reportInputs.sqlText}</td>
                        <td>${reportInputs.defValue}</td>
                        <td>${reportInputs.defType}</td>
                    </tr>
                </g:each>
            </table>

            <div class="pagination">
                <g:paginate total="${reportInputsCount ?: 0}" />
            </div>
        </div>
    </body>
</html>
<!DOCTYPE html>
<html>
    <head>
        <title>Equities</title>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
        <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css">
        <link rel="stylesheet" href="/static/css/app.css">
    </head>
    <body>
        <h2>Equities</h2>
        <#if equities??>
        <table id="equities" class="table table-bordered table-hover">
            <thead>
                <tr>
                    <th>EPIC</th>
                    <th>Details</th>
                </tr>
            </thead>
            <tbody>
                <#list equities as equity>
                    <td>${equity.epic}</td>
                    <td><a href="${equity.detailLink}"> Details</a></td>
                </tr>
                </#list>
            </tbody>
        </table>
        </#if>
    </body>
</html>

<!DOCTYPE html>
<html>
    <head>
        <title>Details</title>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
        <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css">
        <link rel="stylesheet" href="/static/css/app.css">
    </head>
    <body>
        <h2>Details</h2>
        <#if details??>
        <table id="details" class="table table-bordered table-hover">
            <thead>
                <tr>
                    <th>EPIC</th>
                    <th>Company Name</th>
                    <th>Asset Type</th>
                    <th>Sector</th>
                    <th>Currency</th>
                    <th>Prices</th>

                </tr>
            </thead>
            <tbody>
                    <td>${details.epic}</td>
                    <td>${details.companyName}</td>
                    <td>${details.assetType}</td>
                    <td>${details.sector}</td>
                    <td>${details.currency}</td>
                    <#list details.prices as price>
                    <td>${price.price}</td>
                </tr>
                </#list>
            </tbody>
        </table>
        </#if>
    </body>
</html>
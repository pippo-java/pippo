<#macro page title>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <meta content="IE=edge" http-equiv="X-UA-Compatible">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>${title}</title>

        <link href="${webjarsAt('bootstrap/css/bootstrap.min.css')}" rel="stylesheet">
        <link href="${webjarsAt('font-awesome/css/font-awesome.min.css')}" rel="stylesheet">
        <link href="${publicAt('css/style.css')}" rel="stylesheet">
    </head>
    <body>
        <div class="container">
            <#nested/>

            <script src="${webjarsAt('jquery/jquery.min.js')}"></script>
            <script src="${webjarsAt('bootstrap/js/bootstrap.min.js')}"></script>
        </div>
    </body>
</html>
</#macro>

<#macro page title>
<html>
    <head>
        <meta charset="utf-8">
        <meta content="IE=edge" http-equiv="X-UA-Compatible">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>${title}</title>

        <link href="/public/css/style.css" rel="stylesheet">
        <link href="/webjars/bootstrap/3.0.2/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body>
        <div class="container">
            <#nested/>

            <script src="/webjars/jquery/1.9.0/jquery.min.js"></script>
            <script src="/webjars/bootstrap/3.0.2/js/bootstrap.min.js"></script>
        </div>
    </body>
</html>
</#macro>
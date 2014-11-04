<#macro page title>
<html>
    <head>
        <meta charset="utf-8">
        <meta content="IE=edge" http-equiv="X-UA-Compatible">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>${title}</title>

        <link href="/css/style.css" rel="stylesheet">
        <link href="/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body>
        <div class="container">
            <#nested/>

            <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
            <script src="/js/bootstrap.min.js"></script>
        </div>
    </body>
</html>
</#macro>
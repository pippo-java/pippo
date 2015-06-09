<html>
    <head>
        <title>Pippo - Ajax Demo</title>

        <link href="${webjarsAt('bootstrap/3.3.1/css/bootstrap.min.css')}" rel="stylesheet">
        <link href="${webjarsAt('font-awesome/4.2.0/css/font-awesome.min.css')}" rel="stylesheet">

        <script src="https://code.jquery.com/jquery-1.10.2.min.js"></script>
        <script src="https://intercoolerreleases-leaddynocom.netdna-ssl.com/intercooler-0.4.10.min.js"></script>
    </head>
    <body>
        <div class="container">
            <div class="page-header">
                <h1>Demo Pippo <small>Ajax</small></h1>
            </div>

            <!-- Demo1 -->
            <div ic-src="/seconds" ic-poll="5s" class="alert alert-success" role="alert">You have been on this page for 0 seconds...</div>

            <!-- Demo2 -->
            <a ic-post-to="/increment" class="btn btn-primary">Click Me!</a>
        </div>
    </body>
</html>

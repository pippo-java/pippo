<#import "base.ftl" as base/>
<@base.page title="Simple - Ajax">
    <div class="page-header">
        <h1>Demo Pippo <small>Ajax</small></h1>
    </div>

    <!-- Demo 1 -->
    <div ic-src="/seconds" ic-poll="5s" class="alert alert-success" role="alert">You have been on this page for 0 seconds...</div>

    <!-- Demo2 -->
    <a ic-post-to="/increment" class="btn btn-primary">Click Me!</a>
</@base.page>

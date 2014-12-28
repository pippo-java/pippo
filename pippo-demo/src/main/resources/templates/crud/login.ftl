<#import "base.ftl" as base/>
<@base.page title="Login">
    <div class="row" style="padding: 120px">
        <div class="col-md-4 col-md-offset-4">
            <#if error??>
                <div class="alert alert-danger alert-dismissible" role="alert">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only">Close</span>
                    </button>
                    ${error}
                </div>
            </#if>

            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">Please sign in</h3>
                </div>
                <div class="panel-body">
                    <form accept-charset="UTF-8" role="form" method="post" action="${contextPath}/login">
                        <fieldset>
                            <div class="form-group">
                                <input class="form-control" placeholder="Username" name="username">
                            </div>
                            <div class="form-group">
                                <input class="form-control" placeholder="Password" name="password" type="password">
                            </div>
                            <input class="btn btn-success btn-block" type="submit" value="Login">
                        </fieldset>
                    </form>
                </div>
            </div>
        </div>
    </div>
</@base.page>
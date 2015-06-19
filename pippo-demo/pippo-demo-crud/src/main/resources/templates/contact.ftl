<#import "base.ftl" as base/>
<@base.page title="Contact">
    <div class="page-header">
        <h2>Contact <small>POST with form url-encoding</small></h2>
    </div>

    <form class="form-horizontal" role="form" method="post" action="${saveUrl}">
        <div class="form-group">
            <label for="name" class="col-sm-2 control-label">Name</label>
            <div class="col-sm-9">
                <input class="form-control" id="name" name="name" required="" value="${(contact.name)!}">
            </div>
        </div>
        <div class="form-group">
            <label for="phone"  class="col-sm-2 control-label">Phone</label>
            <div class="col-sm-9">
                <input class="form-control" id="phone" name="phone" required="" value="${(contact.phone)!}">
            </div>
        </div>
        <div class="form-group">
            <label for="address" class="col-sm-2 control-label">Address</label>
            <div class="col-sm-9">
                <input class="form-control" id="address" name="address" required="" value="${(contact.address)!}">
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-9">
                <input type="hidden" name="_csrf_token" value="${csrfToken}">
                <button type="submit" class="btn btn-default btn-primary">Submit</button>
                <a class="btn" href="${backUrl}">Cancel</a>
            </div>
        </div>
    </form>
</@base.page>

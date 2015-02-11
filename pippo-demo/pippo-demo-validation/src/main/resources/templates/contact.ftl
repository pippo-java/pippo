<#import "base.ftl" as base/>
<#import "alerts.ftl" as alerts/>

<@base.page title="Contact">
    <div class="page-header">
        <h2>Contact</h2>
    </div>

    <@alerts.list messages=flash.getErrorList() type="danger"/>

    <form class="form-horizontal" role="form" method="post" action="${contextPath}/">
        <div class="form-group">
            <label for="name" class="col-sm-2 control-label">Name</label>
            <div class="col-sm-9">
                <input class="form-control" id="name" name="name" value="${(contact.name)!}">
            </div>
        </div>
        <div class="form-group">
            <label for="phone"  class="col-sm-2 control-label">Phone</label>
            <div class="col-sm-9">
                <input class="form-control" id="phone" name="phone" value="${(contact.phone)!}">
            </div>
        </div>
        <div class="form-group">
            <label for="address" class="col-sm-2 control-label">Address</label>
            <div class="col-sm-9">
                <input class="form-control" id="address" name="address" value="${(contact.address)!}">
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-9">
                <button type="submit" class="btn btn-default btn-primary">Submit</button>
            </div>
        </div>
    </form>
</@base.page>

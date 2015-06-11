<form class="form-horizontal" role="form" ic-on-success="$('#edit').hide();" ic-post-to="${saveUrl}" ic-target="#content" ic-replace-target="true">
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
            <button class="btn btn-default btn-primary">Submit</button>
            <a class="btn" onclick="$('#edit').hide();">Cancel</a>
        </div>
    </div>
</form>

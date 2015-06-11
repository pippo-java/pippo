<table id="content" class="table table-striped table-bordered table-hover">
    <thead>
    <tr>
        <th>#</th>
        <th>Name</th>
        <th>Phone</th>
        <th>Address</th>
        <th></th>
    </tr>
    </thead>
    <tbody>
        <#list contacts as contact>
        <tr>
            <td>${contact_index + 1}</td>
            <td>${contact.name}</td>
            <td>${contact.phone}</td>
            <td>${contact.address}</td>
            <td style="text-align: right;">
                <div class="btn-group btn-group-xs">
                    <a class="btn btn-default" ic-get-from="${appPath}/contact/${contact.id}" ic-target="#edit"><i class="fa fa-pencil"></i> Edit</a>
                    <a class="btn btn-default" ic-delete-from="${appPath}/contact/${contact.id}" ic-confirm="Are you sure?" ic-target="closest tr"><i class="fa fa-trash"></i> Delete</a>
                </div>
            </td>
        </tr>
        </#list>
    </tbody>
</table>

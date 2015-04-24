<#import "base.ftl" as base/>
<@base.page title="Contacts">
    <div class="page-header">
        <h2>Contacts</h2>
    </div>

    <div class="buttons pull-right">
        <a id="addContactButton" type="button" class="btn btn-primary" href="${appPath}/contact/0?action=new"><i class="fa fa-plus"></i> Add Contact</a>
    </div>

    <table class="table table-striped table-bordered table-hover">
        <thead>
        <tr>
            <th>#</th>
            <th>Name</th>
            <th>Phone</th>
            <th colspan='2'>Address</th>
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
                        <a class="btn btn-default" href="${appPath}/contact/${contact.id}?action=edit"><i class="fa fa-pencil"></i> Edit</a>
                        <a class="btn btn-default" href="${appPath}/contact/${contact.id}?action=delete"><i class="fa fa-trash"></i> Delete</a>
                    </div>
                </td>
            </tr>
            </#list>
        </tbody>
    </table>
</@base.page>

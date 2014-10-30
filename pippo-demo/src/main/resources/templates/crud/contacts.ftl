<#import "base.ftl" as base/>
<@base.page title="Contacts">
    <div class="page-header">
        <h2>Contacts</h2>
    </div>

    <div class="buttons pull-right">
        <a id="addContactButton" type="button" class="btn btn-primary" href="/contact/0?action=new"/">Add Contact</a>
    </div>

    <table class="table table-striped table-bordered table-hover">
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
            <#--
            <td><a <a href="/contact?id=${contact.id}&action=edit">Edit</a>&nbsp;<a href="/contact?id=${contact.id}&action=delete">Delete</a>
            -->
                <td><a <a href="/contact/${contact.id}?action=edit">Edit</a>&nbsp;<a href="/contact/${contact.id}?action=delete">Delete</a>
            </tr>
            </#list>
        </tbody>
    </table>
</@base.page>

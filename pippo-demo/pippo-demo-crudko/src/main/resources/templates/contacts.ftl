<#import "base.ftl" as base/>
<@base.page title="Contacts">
    <div class="page-header">
        <h2>Contacts <small>RESTful JSON APIs + KnockoutJS</small></h2>
    </div>

    <div class="buttons pull-right">
        <a class="btn btn-primary" data-bind="click: function() { addContact() }"><i class="fa fa-plus"></i> Add Contact</a>
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
        <tbody data-bind="foreach: contacts">
            <tr>
                <td data-bind="text: id"></td>
                <td data-bind="text: name"></td>
                <td data-bind="text: phone"></td>
                <td data-bind="text: address"></td>
                <td style="text-align: right;">
                    <div class="btn-group btn-group-xs">
                        <a class="btn btn-default" data-bind="click: function() { $parent.editContact($data) }"><i class="fa fa-pencil"></i> Edit</a>
                        <a class="btn btn-default" data-bind="click: function() { $parent.deleteContact($data) }"><i class="fa fa-trash"></i> Delete</a>
                    </div>
                </td>
            </tr>
        </tbody>
    </table>

    <form id="editForm" class="form-horizontal" role="form" data-bind="submit: function() { saveContact($data) }" style="display: none">
        <div class="form-group">
            <label for="name" class="col-sm-2 control-label">Name</label>
            <div class="col-sm-9">
                <input class="form-control" id="name" name="name" required="" data-bind="textInput: editedContact.name">
            </div>
        </div>
        <div class="form-group">
            <label for="phone"  class="col-sm-2 control-label">Phone</label>
            <div class="col-sm-9">
                <input class="form-control" id="phone" name="phone" required="" data-bind="textInput: editedContact.phone">
            </div>
        </div>
        <div class="form-group">
            <label for="address" class="col-sm-2 control-label">Address</label>
            <div class="col-sm-9">
                <input class="form-control" id="address" name="address" required="" data-bind="textInput: editedContact.address">
            </div>
        </div>
    </form>
</@base.page>

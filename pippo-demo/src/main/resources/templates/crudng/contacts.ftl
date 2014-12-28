<#import "base.ftl" as base/>
<@base.page title="Contacts">
    <div class="page-header">
        <h2>Contacts <small>RESTful JSON APIs + AngularJS</small></h2>
    </div>

    <div ng-controller="ContactsCtrl">
        <div class="buttons pull-right">
            <a id="addContactButton" type="button" class="btn btn-primary" href="contact/0"/"><i class="fa fa-plus"></i> Add Contact</a>
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
                <tr ng-repeat="contact in contacts">
                    <td>{{contact.id}}</td>
                    <td>{{contact.name}}</td>
                    <td>{{contact.phone}}</td>
                    <td>{{contact.address}}</td>
                    <td style="text-align: right;">
                        <div class="btn-group btn-group-xs">
                            <a class="btn btn-default" href="contact/{{contact.id}}"><i class="fa fa-pencil"></i> Edit</a>
                            <button class="btn btn-default" ng-click="deleteContact(contact.id)"><i class="fa fa-trash"></i> Delete</button>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</@base.page>

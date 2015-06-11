<#import "base.ftl" as base/>
<@base.page title="CRUD - Ajax">
    <div class="page-header">
        <h2>CRUD <small>Ajax</small></h2>
    </div>

    <div class="buttons pull-right">
        <a class="btn btn-primary" ic-get-from="${appPath}/contact/0" ic-target="#edit"><i class="fa fa-plus"></i> Add Contact</a>
    </div>

    <#include "view/contacts.ftl">

    <div id="edit" style="display: none;"></div>
</@base.page>

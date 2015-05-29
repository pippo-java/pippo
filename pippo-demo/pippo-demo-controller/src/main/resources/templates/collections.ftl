<#import "base.ftl" as base/>
<@base.page title="Collections">
    <div class="page-header">
        <h2>Collections <small>POST with indexed-parameter form url-encoding</small></h2>
    </div>

    <form class="form-horizontal" role="form" method="post">
        <div class="form-group">
            <label for="mySet[0]" class="col-sm-2 control-label">Set</label>
            <div class="col-sm-9">
                <#list mySet as value>
                    <input class="form-control" id="mySet[${value_index}]" name="mySet[${value_index}]" value="${value}">
                </#list>
            </div>
        </div>
        <div class="form-group">
            <label for="myList[0]"  class="col-sm-2 control-label">List</label>
            <div class="col-sm-9">
                <#list myList as value>
                    <input class="form-control" id="myList[${value_index}]" name="myList[${value_index}]" value="${value}">
                </#list>
            </div>
        </div>
        <div class="form-group">
            <label for="myTreeSet[0]" class="col-sm-2 control-label">TreeSet</label>
            <div class="col-sm-9">
                <#list myTreeSet as value>
                    <input class="form-control" id="myTreeSet[${value_index}]" name="myTreeSet[${value_index}]" value="${value}">
                </#list>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-9">
                <button type="submit" class="btn btn-default btn-primary">Submit</button>
                <a type="submit" class="btn" href="${appPath}">Cancel</a>
            </div>
        </div>
    </form>
</@base.page>

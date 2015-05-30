<#import "base.ftl" as base/>
<@base.page title="Collections">
    <div class="page-header">
        <h2>Collections <small>POST[PUT] with indexed-parameter form url-encoding</small></h2>
    </div>

    <form class="form-horizontal" role="form" method="post">
        <div class="form-group">
            <div class="col-sm-4">
                <h4>Set&lt;Integer&gt;</h4>
                <#list mySet as value>
                    <input type="text" class="form-control" id="mySet[${value_index}]" name="mySet[${value_index}]" value="${value}">
                </#list>
            </div>
            <div class="col-sm-4">
                <h4>List&lt;Integer&gt;</h4>
                <#list myList as value>
                    <input type="text" class="form-control" id="myList[${value_index}]" name="myList[${value_index}]" value="${value}">
                </#list>
            </div>
            <div class="col-sm-4">
                <h4>TreeSet&lt;String&gt;</h4>
                <#list myTreeSet as value>
                    <input type="text" class="form-control" id="myTreeSet[${value_index}]" name="myTreeSet[${value_index}]" value="${value}">
                </#list>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-4">
                <!-- Specify the PUT method. Pippo will receive the POST and will route to the PUT handler. -->
                <input type="hidden" name="_method" value="PUT">
                <button type="submit" class="btn btn-default btn-primary">Submit</button>
                <a type="submit" class="btn" href="${appPath}">Cancel</a>
            </div>
        </div>
    </form>
</@base.page>

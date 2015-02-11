<#--
 * Prints a single alert message.
 *
 * @param message
 * @param type
 -->
<#macro single message type="info">
    <#if message??>
        <div class="alert alert-${type} alert-dismissable" role="alert">
            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
            <p>${message}</p>
        </div>
    </#if>
</#macro>

<#--
 * Prints a list of alert messages.
 *
 * @param messages
 * @param type
 -->
<#macro list messages type="info">
    <#if messages?has_content>
        <div class="message-list">
            <#list messages as message>
                <div class="alert alert-${type} alert-dismissable" role="alert">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                    <p>${message}</p>
                </div>
            </#list>
        </div>
    </#if>
</#macro>

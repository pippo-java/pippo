yieldUnescaped '<!DOCTYPE html>'
html {
    head {
        title {
            yield statusCode
            yield ": "
            yield statusMessage
        }
        meta(charset:'utf-8')
        style(type:'text/css') { yieldUnescaped """
            body {
                font-family: "Arial", "Sans serif";
                color: darkGray;
                margin-left: auto;
                margin-right: auto;
                max-width: 900px;
                padding: 0px 20px;
            }

            .status-code {
                font-size: 20pt;
                color: darkGray;
                font-weight: bold;
                border-bottom: 1px solid lightGray;
                padding-bottom: 10px;
            }

            .status-code .mode {
                float: right;
                color: lightGray;
                text-transform:uppercase;
                font-weight: normal;
            }

            .frowny {
                font-size: 100pt;
                padding-bottom: 30px;
                text-align: center;
            }

            .text {
                padding-top: 20px;
                text-align:center;
                font-size: 16pt;
                font-weight: bold;
            }

            .message {
                margin-top: 30px;
                text-align: left;
                overflow: auto;
                border: 1px solid #fac7c7;
                border-radius: 2px;
                background-color: rgba(255, 182, 193, 0.15);
                padding: 10px;
                color: #dd0000;
            }

            .application {
                border-top: 1px solid lightGray;
                padding-top: 20px;
                text-align:center;
                font-size: 14pt;
                font-weight: bold;
                padding-bottom: 30px;
            }

            .link {
                color: blue;
                text-decoration: none;
                font-size: 50pt;
            }

            pre, .description, .text, .frowny {
                color: #3B3131;
            }
"""  }
    }

    body {
        div(class:'status-code') {
            div(class:'mode') { yield runtimeMode }
            span(class:'description') { yield statusMessage }
            yield ' '
            yield statusCode
        }
        if (message != null) {
            pre(class:'message') { yield message }
        }
        div(class:'text') { yield "$requestMethod $requestUri" }
        if (stacktrace != null) {
            pre ( stacktrace )
        } else {
            div(class:'frowny') { yield ':(' }
        }
        div(class:'application') {
            yield "$applicationName $applicationVersion"
            div {
                a(class:'link', href:"$appPath/") { yieldUnescaped '&larr;' }
            }
        }
    }
}

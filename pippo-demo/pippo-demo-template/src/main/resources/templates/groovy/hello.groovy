//
// hello.groovy
//
// This demonstrates:
//   1. template inheritance
//   2. variable binding & inheritance
//   3. localized messages
//   4. localized relative date formatting
//   5. localized date formatting
//
layout 'groovy/base.groovy',
lang: lang,
mode: mode,
pageBody: contents {
    h1 {
        yield i18n('pippo.greeting')
        yieldUnescaped '&nbsp;'
        i(class:'fa fa-smile-o') {}
    }

    div(class:'row') {
        div(class:'col-sm-7') {

            p {
                yieldUnescaped i18n('pippo.yourLanguageAndLocale', lang, locale)
            }
            p {
                yieldUnescaped i18n('pippo.theContextPath', contextPath)
            }
            p {
                yield formatTime(testDate, 'full')
                yield ' ('
                yield prettyTime(testDate)
                yield ')'
            }

            div(class: 'dropdown') {
                button(class: 'btn btn-default dropdown-toggle', type: 'button', id: 'languageMenu', 'data-toggle': 'dropdown', 'aria-expanded': 'true') {
                    yield i18n('pippo.languageChoices')
                    yieldUnescaped '&nbsp;'
                    span(class: 'caret')
                }

                ul(class: 'dropdown-menu', role: 'menu', 'aria-labelledby': 'languageMenu') {
                    languageChoices.each { choice ->
                        li(role: 'presentation') {
                            a(role: 'menuitem', tabindex: '-1', href: "?lang=$choice") { yield choice }
                        }
                    }
                }
            }
        }

        div(class:'col-sm-5') {
            div(class:'panel panel-default') {
                div(class:'panel-heading') {
                    h3(class:'panel-title') { yield i18n('pippo.demonstrations') }
                }
                div(class:'panel-body') {
                    ul {
                        li {
                            a(href:"$contextPath/satisfaction") { yield i18n('pippo.unmatchedRoute') }
                        }
                        li {
                            a(href:"$contextPath/exception") { yield i18n('pippo.exceptionHandling') }
                        }
                    }
                }
            }
        }
    }
}

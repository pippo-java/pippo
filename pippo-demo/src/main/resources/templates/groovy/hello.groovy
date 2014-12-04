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
    h1 ( i18n('pippo.greeting') )
    p {
        yieldUnescaped i18n('pippo.yourLanguageAndLocale', lang, locale)
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
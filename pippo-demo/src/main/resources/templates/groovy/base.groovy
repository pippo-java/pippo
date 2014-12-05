yieldUnescaped '<!DOCTYPE html>'
html(lang="$lang") {
    head {
        title( i18n('pippo.welcome') )
        meta(charset:'utf-8')
        meta(content:'IE=edge', 'http-equiv':'X-UA-Compatible')
        meta(name:'viewport', content:'"width=device-width, initial-scale=1.0')

        link(href: webjarsAt('bootstrap/3.3.1/css/bootstrap.min.css'), rel:'stylesheet')
        link(href: webjarsAt('font-awesome/4.2.0/css/font-awesome.min.css'), rel:'stylesheet')
        link(href: publicAt('css/style.css'), rel:'stylesheet')
    }

    body {
        div(class:'container') {
            pageBody()

            hr()
            p { i { yield "Groovy Template Engine ($mode)" } }
        }

        script(src: webjarsAt('jquery/1.11.1/jquery.min.js')) {}
        script(src: webjarsAt('bootstrap/3.3.1/js/bootstrap.min.js')) {}
    }
}

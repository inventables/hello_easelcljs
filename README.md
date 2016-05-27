Current deployment process:

In the console, run:

    lein with-profile optimized cljsbuild once

This produces a Javascript file in the target directory called "cljsbuild-main.js"

Upload that file to your app in the Easel App edit page.
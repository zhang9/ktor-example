<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link rel="icon" href="/webjars/swagger-ui/favicon-32x32.png">
    <link rel="stylesheet" href="/webjars/swagger-ui/swagger-ui.css">
    <style>
        body {
            margin: 0;
        }
    </style>
    <title>webapis</title>
</head>
<body>
    <div id="swagger-ui"></div>
    <script>
        window.onload = function() {
            window.ui = SwaggerUIBundle({
                url: "https://petstore.swagger.io/v2/swagger.json",
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                plugins: [
                    SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "StandaloneLayout",
                spec: ${spec}
            });
        }
    </script>
    <script src="/webjars/swagger-ui/swagger-ui-bundle.js"></script>
    <script src="/webjars/swagger-ui/swagger-ui-standalone-preset.js"></script>
</body>
</html>
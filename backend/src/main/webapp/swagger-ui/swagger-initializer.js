window.onload = function() {
  window.ui = SwaggerUIBundle({
    url: "/backend_war_exploded/v3/api-docs",
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
    tryItOutEnabled: true,
    requestInterceptor: function(request) {
      request.url = request.url.replace(
          "http://localhost:8080/",
          "http://localhost:8080/backend_war_exploded/"
      );
      return request;
    }
  });
};
package io.vertx.sample;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class FirstMsVerticle extends AbstractVerticle {

    static final String HOSTNAME = System.getenv("HOSTNAME");

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.get("/").handler(this::process);
        router.get("/:name").handler(this::process);

        vertx.createHttpServer().requestHandler(router::accept).listen( 8080 );
    }

    private void process(RoutingContext ctx) {
        String message = "Hello";
        if (ctx.pathParam("name") != null) {
            message += " " + ctx.pathParam( "name" );
        }
        JsonObject json = new JsonObject().put("message", message).put("served-by", HOSTNAME);
        ctx.response()
                .putHeader( HttpHeaders.CONTENT_TYPE, "application/json")
                .end(json.encode());
    }
}

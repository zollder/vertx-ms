package io.vertx.sample;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;

public class FirstVerticle extends AbstractVerticle {

    @Override
    public void start() {

/*        vertx.createHttpServer().requestHandler(req -> {
            req.response().end( "Hello from " + Thread.currentThread().getName() );
        }).listen(8080);*/

        HttpServer server = vertx.createHttpServer();
        server.requestStream().toObservable().subscribe(req -> {
            req.response().end( "Hello from RX " + Thread.currentThread().getName() );
        });
        server.rxListen( 8080 ).subscribe();
    }

}

package io.vertx.sample;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class ConsumerBusMsVerticle extends AbstractVerticle {

    @Override
    public void start() {

        vertx.eventBus().<String>consumer("hello", message -> {
            JsonObject json = new JsonObject().put("Served by", this.toString());
            double num = Math.random();

            if (num < 0.6) {
                // check payload in the incoming message
                if (message.body().isEmpty()) {
                    message.reply(json.put("message", "hello "));
                } else {
                    String msg = "hello " + message.body();
                    System.out.println(msg);
                    message.reply(json.put("message", msg));
                }
            } else if (num < 0.9) {
                System.out.println( "Request failed" );
                message.fail(500, "Failed while processing the message");
            } else {
                System.out.println( "Not replying" );
            }
        });
    }

}

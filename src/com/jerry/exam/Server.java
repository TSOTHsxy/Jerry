package com.jerry.exam;

import com.jerry.agent.annotation.url;
import com.jerry.logger.LogFormatter;
import com.jerry.logger.SimpleLogStyle;
import com.jerry.net.request.HttpRequest;
import com.jerry.startup.Jerry;

import java.nio.charset.StandardCharsets;
import java.util.logging.Formatter;
import java.util.logging.Level;

public class Server {
    public static void main(String[] args) {
        new Jerry()
                .service()
                .mapping(new Object() {
                    @url("/")
                    public String hello() {
                        return "hello world";
                    }
                })
                .apply()
                .apply()
                .maxThreads(5)
                .setKeepAlive(true)
                .backlog(150)
                .port(4000)
                .build()
                .service()
                .mapping(new Object() {
                    @url("/show")
                    public String show(HttpRequest request) {
                        return request.requestLine();
                    }
                })
                .build()
                .logStyle(new SimpleLogStyle() {
                    @Override
                    public Level level() {
                        return Level.FINE;
                    }

                    @Override
                    public Formatter format() {
                        return new LogFormatter();
                    }
                })
                .commandPort(8080)
                .charset(StandardCharsets.UTF_8)
                .start();
    }
}

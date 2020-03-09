package com.github.viniciusfcf.vertx;

import java.util.concurrent.CompletionStage;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.runtime.StartupEvent;
import io.vertx.axle.core.eventbus.Message;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;

@Path("/api")
public class HelloResource {

	/**
	 * Utiliza callbacks Para uso avançado ou se tem um código vertx que quer
	 * reutilizar no Quarkus
	 */
	@Inject
	io.vertx.core.Vertx vertxCore;
	@Inject
	io.vertx.core.eventbus.EventBus eventBusCore;

	/**
	 * RX Java Utilizado mais para transformação de streams
	 * 
	 */
	@Inject
	io.vertx.reactivex.core.Vertx vertxRX;
	@Inject
	io.vertx.reactivex.core.eventbus.EventBus eventBusRX;

	/**
	 * 
	 * Funciona muito bem com Quarkus e Microprofile (Utiliza CompletionStage and
	 * Reactive Streams)
	 * 
	 * 
	 */
	@Inject
	io.vertx.axle.core.Vertx vertxAxle;
	@Inject
	io.vertx.axle.core.eventbus.EventBus eventBusAxle;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello() {
		return "ping ok";
	}

	
	public void onStart(@Observes io.vertx.ext.web.Router router) {

		router.route("/eventbus/*").handler(eventBusHandler());
		router.route().failureHandler(errorHandler());
		router.route().handler(staticHandler());

	}

	private io.vertx.ext.web.handler.sockjs.SockJSHandler eventBusHandler() {
		io.vertx.ext.bridge.PermittedOptions permitted = new io.vertx.ext.bridge.PermittedOptions()
				.setAddress("msgarquivos");
		BridgeOptions options = new BridgeOptions().addOutboundPermitted(permitted).addInboundPermitted(permitted);
		io.vertx.ext.web.handler.sockjs.SockJSHandler create = io.vertx.ext.web.handler.sockjs.SockJSHandler
				.create(vertxCore);
		create.bridge(options, event -> {
			if (event.type() == BridgeEventType.SOCKET_CREATED) {
				System.out.println("Socket conectado com sucesso");
			}
			event.complete(true);
		});
		return create;
	}

	private ErrorHandler errorHandler() {
		return ErrorHandler.create(true);
	}

	private StaticHandler staticHandler() {
		return StaticHandler.create().setCachingEnabled(false);
	}

	

	@GET
	@Path("/async/{msg}")
	public CompletionStage<String> hello(@PathParam("msg") String msg) {
		eventBusAxle.publish("msgarquivos", msg);
		return eventBusAxle.<String>request("eventMsg", msg).thenApply(Message::body);
	}
	
}
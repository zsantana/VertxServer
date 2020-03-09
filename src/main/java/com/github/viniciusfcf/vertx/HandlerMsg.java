package com.github.viniciusfcf.vertx;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.vertx.ConsumeEvent;

@ApplicationScoped
public class HandlerMsg {

	@ConsumeEvent(value="eventMsg", local = false)
	public String consume(String name) {
		return "OK : "+name;
	}
}
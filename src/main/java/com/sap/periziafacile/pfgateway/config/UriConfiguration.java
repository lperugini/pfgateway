package com.sap.periziafacile.pfgateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class UriConfiguration {

	private final String name;
	private final String url;

	public UriConfiguration(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public String getName() {
		return name;
	}
}
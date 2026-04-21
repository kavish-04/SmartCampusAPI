package com.w1991110.smartcampus;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        packages("com.w1991110.smartcampus.resource",
                 "com.w1991110.smartcampus.mapper",
                 "com.w1991110.smartcampus.filter");
        register(JacksonFeature.class);
    }
}
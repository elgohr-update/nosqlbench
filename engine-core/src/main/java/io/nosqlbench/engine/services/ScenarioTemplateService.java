package io.nosqlbench.engine.services;

import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.nb.api.annotations.Service;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Service(WebServiceObject.class)
@Singleton
@Path("/services/namedscenarios/")
public class ScenarioTemplateService implements WebServiceObject {

    public ScenarioTemplateService() {
        super();
    }

    @GET
    @Path("service-status")
    public String status() {
        return "Here at " + System.nanoTime();
    }
}

package com.consol.citrus.samples.incident.service;

import org.citrusframework.schema.samples.networkservice.v1.AnalyseIncident;
import org.citrusframework.schema.samples.networkservice.v1.AnalyseIncidentResponse;
import org.springframework.web.client.RestTemplate;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class NetworkServiceClient implements NetworkService {

    /** The rest template */
    private RestTemplate restTemplate;

    /** */
    private String serviceUri;

    @Override
    public AnalyseIncidentResponse analyse(AnalyseIncident incident) {
        return restTemplate.postForObject(serviceUri, incident, AnalyseIncidentResponse.class);
    }

    /**
     * Gets the rest template.
     * @return
     */
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    /**
     * Sets the rest template.
     * @param restTemplate
     */
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Gets the service endpoint uri.
     * @return
     */
    public String getServiceUri() {
        return serviceUri;
    }

    /**
     * Sets the service endpoint uri.
     * @param serviceUri
     */
    public void setServiceUri(String serviceUri) {
        this.serviceUri = serviceUri;
    }
}

/*
 * Copyright 2006-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.samples.incident.service;

import org.citrusframework.schema.samples.networkservice.v1.AnalyseIncident;
import org.citrusframework.schema.samples.networkservice.v1.AnalyseIncidentResponse;
import org.springframework.web.client.RestTemplate;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class NetworkServiceImpl implements NetworkService {

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

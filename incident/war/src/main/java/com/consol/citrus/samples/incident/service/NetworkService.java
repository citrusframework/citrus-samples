package com.consol.citrus.samples.incident.service;

import org.citrusframework.schema.samples.networkservice.v1.AnalyseIncident;
import org.citrusframework.schema.samples.networkservice.v1.AnalyseIncidentResponse;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public interface NetworkService {

    /**
     * Places new analyse order and receives result in response object.
     * @param incident
     * @return
     */
    AnalyseIncidentResponse analyse(AnalyseIncident incident);

}

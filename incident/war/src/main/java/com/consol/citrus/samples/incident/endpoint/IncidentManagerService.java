package com.consol.citrus.samples.incident.endpoint;

import com.consol.citrus.samples.incident.model.OpenIncident;
import com.consol.citrus.samples.incident.model.OpenIncidentResponse;
import org.citrusframework.schema.samples.incidentmanager.v1.IncidentFault;
import org.citrusframework.schema.samples.incidentmanager.v1.IncidentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebParam;
import java.util.Calendar;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class IncidentManagerService implements IncidentManager {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(IncidentManagerService.class);

    @Override
    public OpenIncidentResponse openIncident(@WebParam(partName = "parameters", name = "OpenIncident", targetNamespace = "http://www.citrusframework.org/schema/samples/IncidentManager/v1") OpenIncident in) throws IncidentFault {
        log.info(String.format("Received new incident request '%s'", in.getIncident().getTicketId()));

        OpenIncidentResponse incidentResponse = new OpenIncidentResponse();
        incidentResponse.setTicketId(in.getIncident().getTicketId());
        incidentResponse.setScheduled(Calendar.getInstance());

        log.info(String.format("Successfully processed new incident request '%s'", in.getIncident().getTicketId()));
        return incidentResponse;
    }
}

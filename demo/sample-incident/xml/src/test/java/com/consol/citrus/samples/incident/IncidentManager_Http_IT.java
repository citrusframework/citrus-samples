package com.consol.citrus.samples.incident;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Test
public class IncidentManager_Http_IT extends AbstractTestNGCitrusTest {

    @CitrusXmlTest(name = "IncidentManager_Http_FieldForceError_1_IT")
    public void testIncidentManager_Http_FieldForceError_1() {
    }

    @CitrusXmlTest(name = "IncidentManager_Http_FieldForceError_2_IT")
    public void testIncidentManager_Http_FieldForceError_2() {
    }

    @CitrusXmlTest(name = "IncidentManager_Http_SchemaInvalid_IT")
    public void testIncidentManager_Http_SchemaInvalid() {}

}

package com.consol.citrus.samples.incident;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class IncidentYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/incident/IncidentManager_Http_Ok_1_IT.citrus.it.yaml")
    public void testIncidentManager_Http_Ok_1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/incident/IncidentManager_Http_Ok_2_IT.citrus.it.yaml")
    public void testIncidentManager_Http_Ok_2() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/incident/IncidentManager_Http_FieldForceError_1_IT.citrus.it.yaml")
    public void testIncidentManager_Http_FieldForceError_1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/incident/IncidentManager_Http_FieldForceError_2_IT.citrus.it.yaml")
    public void testIncidentManager_Http_FieldForceError_2() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/incident/IncidentManager_Http_Ok_3_IT.citrus.it.yaml")
    public void testIncidentManager_Http_Ok_3() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/incident/IncidentManager_Http_Ok_4_IT.citrus.it.yaml")
    public void testIncidentManager_Http_Ok_4() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/incident/IncidentManager_Http_SchemaInvalid_IT.citrus.it.yaml")
    public void testIncidentManager_Http_SchemaInvalid() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/incident/IncidentManager_Jms_Ok_1_IT.citrus.it.yaml")
    public void testIncidentManager_Jms_Ok_1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/incident/IncidentManager_Jms_Ok_2_IT.citrus.it.yaml")
    public void testIncidentManager_Jms_Ok_2() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/incident/IncidentManager_Jms_SchemaInvalid_IT.citrus.it.yaml")
    public void testIncidentManager_Jms_SchemaInvalid() {
    }
}

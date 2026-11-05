package com.consol.citrus.samples.todolist;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class SimpleSampleXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/todolist/simple-success-first.citrus.it.xml" })
    public void testSuccessFirst() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/todolist/simple-success-second.citrus.it.xml" })
    public void testSuccessSecond() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/todolist/simple-success-third.citrus.it.xml" })
    public void testSuccessThird() {
    }
}

package com.consol.citrus.samples.bookstore;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class BookstoreXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:com/consol/citrus/samples/bookstore/AddBook_Ok_1_IT.citrus.it.xml")
    public void testAddBook_Ok_1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:com/consol/citrus/samples/bookstore/AddBook_Ok_2_IT.citrus.it.xml")
    public void testAddBook_Ok_2() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:com/consol/citrus/samples/bookstore/GetBookAbstract_Ok_1_IT.citrus.it.xml")
    public void testGetBookAbstract_Ok_1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:com/consol/citrus/samples/bookstore/GetBookDetails_Ok_1_IT.citrus.it.xml")
    public void testGetBookDetails_Ok_1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:com/consol/citrus/samples/bookstore/GetBookDetails_Error_1_IT.citrus.it.xml")
    public void testGetBookDetails_Error_1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:com/consol/citrus/samples/bookstore/ListBooks_Ok_1_IT.citrus.it.xml")
    public void testListBooks_Ok_1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:com/consol/citrus/samples/bookstore/ListBooks_Ok_2_IT.citrus.it.xml")
    public void testListBooks_Ok_2() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:com/consol/citrus/samples/bookstore/SchemaValidation_Error_1_IT.citrus.it.xml")
    public void testSchemaValidation_Error_1() {
    }
}

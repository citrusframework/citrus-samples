package org.citrusframework.samples.showcases.bookstore;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class BookstoreXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:org/citrusframework/samples/showcases/bookstore/AddBook_Ok_1_IT.citrus.it.xml")
    public void testAddBook_Ok_1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:org/citrusframework/samples/showcases/bookstore/AddBook_Ok_2_IT.citrus.it.xml")
    public void testAddBook_Ok_2() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:org/citrusframework/samples/showcases/bookstore/GetBookAbstract_Ok_1_IT.citrus.it.xml")
    public void testGetBookAbstract_Ok_1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:org/citrusframework/samples/showcases/bookstore/GetBookDetails_Ok_1_IT.citrus.it.xml")
    public void testGetBookDetails_Ok_1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:org/citrusframework/samples/showcases/bookstore/GetBookDetails_Error_1_IT.citrus.it.xml")
    public void testGetBookDetails_Error_1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:org/citrusframework/samples/showcases/bookstore/ListBooks_Ok_1_IT.citrus.it.xml")
    public void testListBooks_Ok_1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:org/citrusframework/samples/showcases/bookstore/ListBooks_Ok_2_IT.citrus.it.xml")
    public void testListBooks_Ok_2() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:org/citrusframework/samples/showcases/bookstore/SchemaValidation_Error_1_IT.citrus.it.xml")
    public void testSchemaValidation_Error_1() {
    }
}

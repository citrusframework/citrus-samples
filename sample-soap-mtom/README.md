SOAP MTOM sample ![Logo][1]
==============

This sample uses SOAP web services with MTOM enabled attachments. You can read more about the 
Citrus SOAP features in [reference guide][4]

Objectives
---------

The Citrus tests in this project send and receive SOAP attachments via MTOM enabled. Both client and server are provided by Citrus so
each test receives its own requests as a demo showcase.

The sample shows how to use MTOM enabled SOAP messages as a client and server. First we define the schema and a global namespace for the SOAP
messages.

```java
@Bean
public SimpleXsdSchema todoListSchema() {
    return new SimpleXsdSchema(new ClassPathResource("schema/ImageService.xsd"));
}

@Bean
public XsdSchemaRepository schemaRepository() {
    XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
    schemaRepository.getSchemas().add(todoListSchema());
    return schemaRepository;
}

@Bean
public NamespaceContextBuilder namespaceContextBuilder() {
    NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
    namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("image", "http://www.citrusframework.org/imageService"));
    return namespaceContextBuilder;
}
```
   
The schema repository holds all known schemas in this project. Citrus will automatically check the syntax rules for incoming messages
then. Next we need a SOAP web service client and server component:

```java
@Bean
public SoapMessageFactory messageFactory() {
    return new SaajSoapMessageFactory();
}

@Bean
public WebServiceClient imageClient() {
    return CitrusEndpoints.soap()
                        .client()
                        .defaultUri("http://localhost:8080/services/image")
                        .build();
}

@Bean
public WebServiceServer imageServer() {
    return CitrusEndpoints.soap()
            .server()
            .port(8080)
            .autoStart(true)
            .build();
}
```
    
As you can see client and server are using the same port `8080`. This means that requests are send to the web service endpoint in Citrus. In addition to that we define a SOAP message factory that is
responsible for creating the SOAP envelope. 

Now we can use the web service client and server in the Citrus test.

MTOM enabled
---------

First of all we want to send and receive a SOAP message with MTOM enabled attachment. The request defines the `cid:IMAGE` reference in the payload and enabled MTOM usage on the client request.
    
```java
soap()
    .client(imageClient)
    .send()
    .fork(true)
    .soapAction("addImage")
    .payload("<image:addImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                "<image:image>cid:IMAGE</image:image>" +
            "</image:addImage>")
    .attachment("IMAGE", "application/octet-stream", new ClassPathResource("image/logo.png"))
    .mtomEnabled(true);

soap()
    .server(imageServer)
    .receive()
    .soapAction("addImage")
    .schemaValidation(false)
    .payload("<image:addImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                "<image:image>" +
                    "<xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:IMAGE\"/>" +
                "</image:image>" +
            "</image:addImage>")
    .attachmentValidator(new BinarySoapAttachmentValidator())
    .attachment("IMAGE", "application/octet-stream", new ClassPathResource("image/logo.png"));
```

The server is able to receive the MTOM enabled message. The image data is streamed as a SOAP attachment using MTOM. This means that the image element in the payload
uses a `xop:Inlclude` placeholder with reference to the `cid:IMAGE` attachment. The receiving action in Citrus is able to validate the attachment with `BinarySoapAttachmentValidator` as we have a
PNG image data.

MTOM inline
---------

MTOM also supports inline attachment handling with `mtom-inline` feature enabled. This means that the image data is automatically added in the payload as base64 encoded data stream.
We can enable MTOM inline on the Citrus SOAP attachment object as follows:
 
```java
SoapAttachment attachment = new SoapAttachment();
attachment.setContentType("application/octet-stream");
attachment.setContentResourcePath("image/logo.png");
attachment.setMtomInline(true);
attachment.setContentId("IMAGE");

soap()
    .client(imageClient)
    .send()
    .fork(true)
    .soapAction("addImage")
    .payload("<image:addImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                "<image:image>cid:IMAGE</image:image>" +
            "</image:addImage>")
    .attachment(attachment)
    .mtomEnabled(true);

soap()
    .server(imageServer)
    .receive()
    .soapAction("addImage")
    .payload("<image:addImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                "<image:image>citrus:readFile(image/logo.base64)</image:image>" +
            "</image:addImage>");
``` 

Now with MTOM inline the request is not using `xop:Inlcude` but a base64 encoded data stream in the payload. We can validate this stream with a combination of
`citrus:encodeBase64()` and `citrus:readFile()` functions.
        
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean verify
    
This executes the complete Maven build lifecycle including all Citrus test cases in the integration test phase.
During the build you will see Citrus performing the integration tests.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://www.citrusframework.org
 [3]: https://www.citrusframework.org/reference/html/
 [4]: https://www.citrusframework.org/reference/html#soap

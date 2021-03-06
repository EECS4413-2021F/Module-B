# Module B - Web Services

- [HTTP Server Refactoring Example](#http-server-refactoring-example)
  - [Other Servlets](#other-servlets)
    - [From the Lab](#from-the-lab)
  - [RESTful Products API Example](#restful-products-api-example)
    - [From the Lab](#from-the-lab-1)
  - [Miscellaneous](#miscellaneous)
  - [Common Gateway Interface](#common-gateway-interface)
    - [Bash Scripts:](#bash-scripts)
    - [PERL Scripts:](#perl-scripts)

-----
## HTTP Server Refactoring Example

|   | Description
|---|---------------------------------
| [v1.0](src/httpd/v1/HTTPServer0.java) | The initial code with everything in the `run` method.
| [v1.1](src/httpd/v1/HTTPServer1.java) | The business logic/application code is moved into a `doRequest` method.
| [v1.2](src/httpd/v1/HTTPServer2.java) | The request and response variables are grouped into `Request` and `Response` classes.
| [v1.3](src/httpd/v1/HTTPServer3.java) | The `doRequest` is removed for the `HTTPServer` class entirely into a subclass [`MainService`](src/httpd/v1/MainService3.java).
| [v2](src/httpd/v2/) | The `Request` and `Response` classes and the request and response handling methods are moved into dedicated [`RequestContext`](src/httpd/v2/RequestContext.java) and [`ResponseContext`](src/httpd/v2/ResponseContext.java) classes.
| [v3](src/httpd/v3/) | Split each service into its own class, and group the server code into its own package.
| [v4](src/httpd/v4/) | Reorganize the server code so the server itself isn't a thread. Clients are handle by worker threads.
| [v5](src/httpd/v5/) | Spin-off the service logic into model classes, engines, and data access objects.

The Tomcat servlet versions:

- [HelloService](src/services/HelloService.java)
- [CalcService](src/services/CalcService.java)
- [CalcOpService](src/services/CalcOpService.java)
- [StudentsService](src/services/StudentsService.java)

The associated model classes:

- [CalcEngine](src/model/CalcEngine.java)
- [StudentsDAO](src/model/StudentsDAO.java)
- [Students](src/model/Students.java)
- [Student](src/model/Student.java)

-----
## Other Servlets

- [TotalService](src/services/TotalService.java): Demonstrates accessing and setting the `session`.

#### From the Lab

- [StartService](src/services/StartService.java): Demonstrates redirection, the `session`, querying request properties and initial parameters.
- [VendorIDtoNamesService](src/services/VendorIDtoNamesService.java): Demostrates the `session`, uses a TCP service via model (engine) class.
  - [VendorIDtoNameTCPService](src/services/VendorIDtoNameTCPService.java): The TCP service.
  - [VendorsEngine](src/model/VendorsEngine.java): The model class (engine).

-----
## RESTful Products API Example

- [ProductsAPIService](src/api/services/ProductsAPIService.java): [The Products API](src/api/README.md#products-api)
  - [Product](src/api/model/Product.java):
    Model class for representing a record in the `Product` table in the database.
  - [Products](src/api/model/Products.java):
    Model class for a collection of `Product` objects.
  - [ProductFilter](src/api/model/ProductFilter.java):
    Model class for storing parameters for the search filter.
  - [ProductsDAO](src/api/model/ProductsDAO.java):
    Database access object for CRUD operations into the database.
  - [ProductsAPIRequest](src/api/model/ProductsAPIRequest.java):
    Class to check and parse the JSON request bodies into `Product` and `Products`.
  - [ProductsAPIResponse](src/api/model/ProductsAPIResponse.java):
    Class to form well-structured JSON responses specific to the Products API.

#### From the Lab

- [CartAPIService](src/api/services/CartAPIService.java): [The Cart API](src/api/README.md#cart-api)
  - [CartAPIRequest](src/api/model/CartAPIRequest.java):
    Class to check and parse the JSON request bodies into `ProductFilter` objects.
  - [CartAPIResponse](src/api/model/CartAPIResponse.java):
    Class to form well-structured JSON responses specific to the Cart API.

- Shared:
  - [APIRequest](src/api/model/APIRequest.java):
    Class to deserialize the HTTP Request body as JSON.
  - [APIResponse](src/api/model/APIResponse.java):
    Class to form well-structured JSON responses.
  - [context.xml](WebContent/META-INF/context.xml):
    Database configurations

-----
## Miscellaneous

- [HTTPClient](src/miscs/HTTPClient.java)

-----
## Common Gateway Interface

To run, copy the [cgi-bin](cgi-bin/) directory into your `www` directory and run: `chmod -R go+x ~/www/cgi-bin/`.

#### Bash Scripts:

- [hello.cgi](cgi-bin/hello.cgi)
- [environment.cgi](cgi-bin/environment.cgi)

#### PERL Scripts:

- [hello.pl](cgi-bin/hello.pl)
- [environment.pl](cgi-bin/environment.pl)
- [redirect.pl](cgi-bin/redirect.pl)
- [webpage.pl](cgi-bin/webpage.pl)

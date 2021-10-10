# Example Store API

- [Products API](#products-api)
  - [GET /products](#get-products)
  - [GET /v1/products](#get-v1products)
  - [GET /product/&lt;id&gt;](#get-productid)
  - [GET /v1/product/&lt;id&gt;](#get-v1productid)
  - [PUT /products](#put-products)
  - [PUT /v1/products](#put-v1products)
  - [POST /products](#post-products)
  - [POST /v1/products](#post-v1products)
  - [PUT  /product/&lt;id&gt;](#put--productid)
  - [PUT  /v1/product/&lt;id&gt;](#put--v1productid)
  - [POST /product/&lt;id&gt;](#post-productid)
  - [POST /v1/product/&lt;id&gt;](#post-v1productid)
  - [DELETE /product/&lt;id&gt;](#delete-productid)
  - [DELETE /v1/product/&lt;id&gt;](#delete-v1productid)
  - [Exceptions](#exceptions)
- [Cart API](#cart-api)
  - [GET /cart](#get-cart)
  - [GET /v1/cart](#get-v1cart)
  - [POST /cart/add](#post-cartadd)
  - [POST /v1/cart/add](#post-v1cartadd)
  - [POST /cart/remove](#post-cartremove)
  - [POST /v1/cart/remove](#post-v1cartremove)

**Note:** Most RESTful APIs are not truly RESTful, and this API is no exception.

-----
## Products API

### `GET /products`
### `GET /v1/products`

Retrieve a list of all Products within the database. We can provide query 
parameters to filter our results. The available filters are:

| Field         | Description
|---------------|-----------------------------------------
| `id`          | Search for products by `Product ID`.
| `name`        | Search for products with a `name` that contain this search term.
| `description` | Search for products with a `description` that contain this search phrase.
| `category`    | Search for products by `Category name`.
| `vendor`      | Search for products by `Vendor name`.
| `minQuantity` | Filter for products with a `quantity` (`qty` in the DB) greater than or equal to this value.
| `maxQuantity` | Filter for products with a `quantity` (`qty` in the DB) leass than or equal to this value.
| `minCost`     | Filter for products with a `cost` greater than or equal to this value.
| `maxCost`     | Filter for products with a `cost` leass than or equal to this value.
| `minMSRP`     | Filter for products with a `msrp` greater than or equal to this value.
| `maxMSRP`     | Filter for products with a `msrp` leass than or equal to this value.
| `orderyBy`    | Sort the products in order of: `id`, `name`, `cost`, `msrp`, `quantity`, `category`, and `vendor`.
| `reversed`    | Sort in reverse order. Default: `false`.
| `limit`       | Limit the number of results returned for pagination.
| `offset`      | Offset to the start result for a paginated result.

Additional fields:

| Field         | Description
|---------------|-----------------------------------------
| `chain`       | If provided, filter using the previous request, in addition to the newly given parameters. A principle of REST is that each request is stateless. Every request is treated as new. No session, no history. So, this feature violates REST, however, I wanted to demonstrate the use of sesssions, so I've added it here.

Sample Response: `GET /v1/products`

```json
{
  "version": "1.0",
  "rid": 4,
  "method": "GET",
  "uri": "/Module_B/v1/products",
  "length": 110,
  "search": {},
  "results": {
    "products": [
      {
        "id": "S10_1678",
        "name": "1969 Harley Davidson Ultimate Chopper",
        "description": "This replica features working kickstand, front suspension, gear-shift lever, footbrake lever, drive chain, wheels and steering. All parts are particularly delicate due to their precise scale and require special care and attention.",
        "category": "Motorcycles",
        "vendor": "Min Lin Diecast",
        "quantity": 7933,
        "cost": 48.81,
        "msrp": 95.7
      },
      {
        "id": "S10_2016",
        "name": "1996 Moto Guzzi 1100i",
        "description": "Official Moto Guzzi logos and insignias, saddle bags located on side of motorcycle, detailed engine, working steering, working suspension, two leather seats, luggage rack, dual exhaust pipes, small saddle bag located on handle bars, two-tone paint with chrome accents, superior die-cast detail , rotating wheels , working kick stand, diecast metal with plastic parts and baked enamel finish.",
        "category": "Motorcycles",
        "vendor": "Highway 66 Mini Classics",
        "quantity": 6625,
        "cost": 68.99,
        "msrp": 118.94
      },
      ...
    ]
  }
}
```

### `GET /product/<id>`
### `GET /v1/product/<id>`

Retrieve a specific `Product` with the given `<id>`.

Sample Response: `GET /v1/product/S10_1678`

```json
{
  "version": "1.0",
  "rid": 1,
  "method": "GET",
  "uri": "/Module_B/v1/product/S10_1678",
  "result": {
    "id": "S10_1678",
    "name": "1969 Harley Davidson Ultimate Chopper",
    "description": "This replica features working kickstand, front suspension, gear-shift lever, footbrake lever, drive chain, wheels and steering. All parts are particularly delicate due to their precise scale and require special care and attention.",
    "category": "Motorcycles",
    "vendor": "Min Lin Diecast",
    "quantity": 7933,
    "cost": 48.81,
    "msrp": 95.7
  }
}
```

### `PUT /products`
### `PUT /v1/products`

Adds one or more `Product` objects to the database, given as JSON.

| Field         | Description
|---------------|-----------------------------------------
| `update`      | If provided, updates the values of existing `Product` object. Each `Product` must specify its `ID`.


Sample Request 1: `PUT /v1/products`

```json
{
  "id": "A320_2019",
  "name": "US Airways A320 Model Airplane",
  "description": "A replica scale model of the original Airbus A320-200 US Airways model aircraft. The US Airways A320 Model Airplane is crafted from pressure cast composite resin and is an impressive airplane gift. Our A320 US Airways Model Airplane arrives fully assembled and includes a display stand.",
  "category": "Planes",
  "vendor": "Min Lin Diecast",
  "quantity": 1000,
  "cost": 270.00,
  "msrp": 395.7
}
```

Sample Response 1: `PUT /v1/products`

```json
{
  "version": "1.0",
  "rid": 3,
  "method": "PUT",
  "uri": "/Module_B/v1/products",
  "inserted": {
    "id": "A320_2019",
    "name": "US Airways A320 Model Airplane",
    "description": "A replica scale model of the original Airbus A320-200 US Airways model aircraft. The US Airways A320 Model Airplane is crafted from pressure cast composite resin and is an impressive airplane gift. Our A320 US Airways Model Airplane arrives fully assembled and includes a display stand.",
    "category": "Planes",
    "vendor": "Min Lin Diecast",
    "quantity": 1000,
    "cost": 270,
    "msrp": 395.7
  }
}
```

Sample Request 2: `PUT /v1/products`

```json
[
  {
    "id": "M20_5010",
    "name": "Full Stack Space Shuttle Endeavour Model",
    "description": "Our space shuttle Endeavour with full stack replica scale model is crafted after the original produced for NASA. The Endeavour had an orbiter designation of OV-105, and was the last space shuttle that was built. It ultimately took 25 flights. This Endeavour space shuttle with full stack model arrives completely assembled and has an attractive base for display.",
    "category": "Planes",
    "vendor": "Min Lin Diecast",
    "quantity": 1000,
    "cost": 410.00,
    "msrp": 595.7
  }, {
    "id": "M54_4003",
    "name": "Cessna 150/152 Model Airplane",
    "description": "This Cessna 150/152 Model Airplane is a replica scale model of the original Cessna 150/152 model aircraft. The Cessna 150/152 Model Airplane is crafted from wood and is an impressive aviation gift. Our Cessna 150/152 model aircraft arrives fully assembled and includes a display stand.",
    "category": "Planes",
    "vendor": "Min Lin Diecast",
    "quantity": 5780,
    "cost": 194.00,
    "msrp": 255.3
  }
]
```

Sample Response 2: `PUT /v1/products`

```json
{
  "version": "1.0",
  "rid": 4,
  "method": "PUT",
  "uri": "/Module_B/v1/products",
  "length": 2,
  "inserted": {
    "products": [
      {
        "id": "M20_5010",
        "name": "Full Stack Space Shuttle Endeavour Model",
        "description": "Our space shuttle Endeavour with full stack replica scale model is crafted after the original produced for NASA. The Endeavour had an orbiter designation of OV-105, and was the last space shuttle that was built. It ultimately took 25 flights. This Endeavour space shuttle with full stack model arrives completely assembled and has an attractive base for display.",
        "category": "Planes",
        "vendor": "Min Lin Diecast",
        "quantity": 1000,
        "cost": 410,
        "msrp": 595.7
      },
      {
        "id": "M54_4003",
        "name": "Cessna 150/152 Model Airplane",
        "description": "This Cessna 150/152 Model Airplane is a replica scale model of the original Cessna 150/152 model aircraft. The Cessna 150/152 Model Airplane is crafted from wood and is an impressive aviation gift. Our Cessna 150/152 model aircraft arrives fully assembled and includes a display stand.",
        "category": "Planes",
        "vendor": "Min Lin Diecast",
        "quantity": 5780,
        "cost": 194,
        "msrp": 255.3
      }
    ]
  }
}
```

### `POST /products`
### `POST /v1/products`

While strictly speaking `POST` is not a RESTful HTTP method, however, it is
useful for easily distinguishing between a create request and a update
request. The `PUT` is create or update when a flag is given and `POST` is
strictly for updates. Additionally, the `PUT` method should technically
require us provide the entire `Product` representation within the request,
not just a partial representation. So instead, we will use the `POST` method.
Most RESTful APIs are not truly RESTful, and this API is no exception.

Same as `PUT /products?update=true`.

### `PUT  /product/<id>`
### `PUT  /v1/product/<id>`
### `POST /product/<id>`
### `POST /v1/product/<id>`

Update the values in the specified `Product`, given as JSON.

Sample Request: `POST /v1/product/S700_3505`

```json
{
  "quantity": 556,
  "cost": 151.09
}
```

Sample Response: `POST /v1/product/S700_3505`

```json
{
  "version": "1.0",
  "rid": 7,
  "method": "POST",
  "uri": "/Module_B/v1/product/S700_3505",
  "updated": {
    "id": "S700_3505",
    "name": "The Titanic",
    "description": "Completed model measures 19 1/2 inches long, 9 inches high, 3inches wide and is in barn red/black. All wood and metal.",
    "category": "Ships",
    "vendor": "Carousel DieCast Legends",
    "quantity": 556,
    "cost": 151.09,
    "msrp": 100.17
  }
}
```

### `DELETE /product/<id>`
### `DELETE /v1/product/<id>`

Delete the specified `Product`. Returns the delete record.

Sample Response: `DELETE /v1/product/A320_2019`

```json
{
  "version": "1.0",
  "rid": 9,
  "method": "DELETE",
  "uri": "/Module_B/v1/product/A320_2019",
  "deleted": "A320_2019",
  "removed": {
    "id": "A320_2019",
    "name": "US Airways A320 Model Airplane",
    "description": "A replica scale model of the original Airbus A320-200 US Airways model aircraft. The US Airways A320 Model Airplane is crafted from pressure cast composite resin and is an impressive airplane gift. Our A320 US Airways Model Airplane arrives fully assembled and includes a display stand.",
    "category": "Planes",
    "vendor": "Min Lin Diecast",
    "quantity": 1000,
    "cost": 270,
    "msrp": 395.7
  }
}
```

### Exceptions

Sample Exception:

```json
{
  "version": "1.0",
  "rid": 10,
  "method": "DELETE",
  "uri": "/Module_B/v1/product/A320_2019",
  "exception": {
    "detailMessage": "No product exists with the given ID, use insert instead: A320_2019",
    "stackTrace": [
      {
        "declaringClass": "api.model.ProductsDAO",
        "methodName": "deleteProduct",
        "fileName": "ProductsDAO.java",
        "lineNumber": 278
      },
      {
        "declaringClass": "api.services.ProductsAPIService",
        "methodName": "doDeleteOne",
        "fileName": "ProductsAPIService.java",
        "lineNumber": 353
      },
      {
        "declaringClass": "api.services.ProductsAPIService",
        "methodName": "doDelete",
        "fileName": "ProductsAPIService.java",
        "lineNumber": 107
      },
      {
        "declaringClass": "javax.servlet.http.HttpServlet",
        "methodName": "service",
        "fileName": "HttpServlet.java",
        "lineNumber": 666
      },
      ...
    ],
    "suppressedExceptions": []
  }
}
```

-----
## Cart API

### `GET /cart`
### `GET /v1/cart`

Retrieve a list of all Products within the shopping cart.

Sample Response: `GET /v1/cart`

```json
{
  "version": "1.0",
  "rid": 5,
  "method": "GET",
  "uri": "/Module_B/v1/cart",
  "length": 2,
  "cart": {
    "products": [
      {
        "id": "S10_1678",
        "name": "1969 Harley Davidson Ultimate Chopper",
        "description": "This replica features working kickstand, front suspension, gear-shift lever, footbrake lever, drive chain, wheels and steering. All parts are particularly delicate due to their precise scale and require special care and attention.",
        "category": "Motorcycles",
        "vendor": "Min Lin Diecast",
        "quantity": 7933,
        "cost": 48.81,
        "msrp": 95.7
      },
      {
        "id": "S10_2016",
        "name": "1996 Moto Guzzi 1100i",
        "description": "Official Moto Guzzi logos and insignias, saddle bags located on side of motorcycle, detailed engine, working steering, working suspension, two leather seats, luggage rack, dual exhaust pipes, small saddle bag located on handle bars, two-tone paint with chrome accents, superior die-cast detail , rotating wheels , working kick stand, diecast metal with plastic parts and baked enamel finish.",
        "category": "Motorcycles",
        "vendor": "Highway 66 Mini Classics",
        "quantity": 6625,
        "cost": 68.99,
        "msrp": 118.94
      }
    ]
  }
}
```

### `POST /cart/add`
### `POST /v1/cart/add`

Add all of the items to the cart that match the given search filter.

| Field         | Description
|---------------|-----------------------------------------
| `chain`       | If provided, add the products that match the previous request's filter. A principle of REST is that each request is stateless. Every request is treated as new. No session, no history. So, this feature violates REST, however, I wanted to demonstrate the use of sesssions, so I've added it here.
| `addAll`      | Flag to permit adding all of the products in the store into the shopping cart.

This is not a RESTful endpoint. First, it uses `POST` which strictly speaking is not a
RESTful HTTP method. Most RESTful APIs are not truly RESTful, and this API is no exception.
It has been pointed out, "why not use `PUT /cart` instead?". However, the `POST /cart/add` feature is
different semantically than `PUT /cart`. `PUT /cart` would create the cart, a new cart, but we want to
add an item or items to the cart, the existing cart.

Sample request: `POST /v1/cart/add`

```json
{"id": "S10_2016"}
```

Sample response: `POST /v1/cart/add`

```json
{
  "version": "1.0",
  "rid": 7,
  "method": "POST",
  "uri": "/Module_B/v1/cart/add",
  "length": 1,
  "search": {
    "id": "S10_2016"
  },
  "added": {
    "products": [
      {
        "id": "S10_2016",
        "name": "1996 Moto Guzzi 1100i",
        "description": "Official Moto Guzzi logos and insignias, saddle bags located on side of motorcycle, detailed engine, working steering, working suspension, two leather seats, luggage rack, dual exhaust pipes, small saddle bag located on handle bars, two-tone paint with chrome accents, superior die-cast detail , rotating wheels , working kick stand, diecast metal with plastic parts and baked enamel finish.",
        "category": "Motorcycles",
        "vendor": "Highway 66 Mini Classics",
        "quantity": 6625,
        "cost": 68.99,
        "msrp": 118.94
      }
    ]
  }
}
```

### `POST /cart/remove`
### `POST /v1/cart/remove`

Remove all of the items to the cart that match the given search filter.

| Field         | Description
|---------------|-----------------------------------------
| `chain`       | If provided, remove the products that match the previous request's filter.  A principle of REST is that each request is stateless. Every request is treated as new. No session, no history. So, this feature violates REST, however, I wanted to demonstrate the use of sesssions, so I've added it here.

This is not a RESTful endpoint. First, it uses `POST` which strictly speaking is not a
RESTful HTTP method. Most RESTful APIs are not truly RESTful, and this API is no exception.
It has been pointed out, "why not use `DELETE /cart` instead?". However, the `POST /cart/remove` feature is
different semantically than `DELETE /cart`. `DELETE /cart` would delete the entire cart, but we want to
just remove an item or some items from the cart and not delete whole thing.

Sample request: `POST /v1/cart/remove`

```json
{"id": "S10_1678"}
```

Sample response: `POST /v1/cart/remove`

```json
{
  "version": "1.0",
  "rid": 8,
  "method": "POST",
  "uri": "/Module_B/v1/cart/remove",
  "length": 1,
  "search": {
    "id": "S10_1678"
  },
  "removed": {
    "products": [
      {
        "id": "S10_1678",
        "name": "1969 Harley Davidson Ultimate Chopper",
        "description": "This replica features working kickstand, front suspension, gear-shift lever, footbrake lever, drive chain, wheels and steering. All parts are particularly delicate due to their precise scale and require special care and attention.",
        "category": "Motorcycles",
        "vendor": "Min Lin Diecast",
        "quantity": 7933,
        "cost": 48.81,
        "msrp": 95.7
      }
    ]
  }
}
```

# RESTful Products API

### `GET /products`

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

### `GET /product/<id>`

Retrieve a specific `Product` with the given `<id>`.

### `PUT /products`

Adds one or more `Product` objects to the database, given as JSON.

| Field         | Description
|---------------|-----------------------------------------
| `update`      | If provided, updates the values of existing `Product` object. Each `Product` must specify its `ID`.


### `PUT /product/<id>`

Update the values in the specified `Product`, given as JSON.

### `DELETE /product/<id>`

Delete the specified `Product`. Returns the delete record.

# address-lookup-poc-frontend

Scala Play frontend built with the HMRC Giter8 scaffold for searching UK addresses by postcode, post town or UPRN.

## Dependencies

- Java 21+, sbt and Docker.
- Service Manager (`sm2`) configured with access to HMRC artefacts; connect to the HMRC VPN.
- An existing Docker container named `mongodb`, exposing MongoDB on `localhost:27017`.
- `GG_AUTH_SERVICES` and `IDENTITY_VERIFICATION` (9927) for authentication and the local login stub.
- `ADDRESS_SEARCH_API` (8607) and `ADDRESS_LOOKUP` (9022).
- `BAS_GATEWAY` (9557) and `BAS_GATEWAY_FRONTEND` (9553) for sign-out.
- `CONTACT_FRONTEND` (9250) and `FEEDBACK_FRONTEND` (9514) for feedback.

## Running locally

From the project directory:

```bash
./start_dependencies.sh
./run_local.sh
```

The dependency script starts the existing MongoDB container and starts and verifies the services through `sm2`. The local runner starts the frontend on port 9000.

Open <http://localhost:9000/address-lookup-poc-frontend/> and sign in through the local login stub using **Individual** and confidence level **50**.

Local test searches: postcode `ZZ1Z 6AB`, post town `Town`, or UPRN `990091234568`.

## License

Licensed under the [Apache 2.0 License](LICENSE).

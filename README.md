# Diffusion REST Adapter

[Overview](documentation/Overview.md)
[Configuration](documentation/Configuration.md)
[Services](documentation/Services.md)
[Endpoints](documentation/Endpoints.md)
[ServiceSessions](documentation/ServiceSessions.md)

## Building

### Running integration tests

The integration tests require a Diffusion server to run against.
They are run by the profile, `integration-test`, enabled when the environmental property `DIFFUSION_HOME` is set.
They use the `diffusion-maven-plugin` to start up and shutdown the the Diffusion server.
The `diffusion-maven-plugin` requires a system dependency to run, these `systemPath` for this dependency is relative to
the `DIFFUSION_HOME` environmental variable.

There are two categories for the integration tests, Embedded service tests and live service tests.
The embedded service tests host their own REST services embedded within the JVM process.
The live service tests use REST services generally available on the internet.
The only the embedded service tests are run by default.
To run only the live service tests enable the profile `live-services-test`.
To run both the embedded services tests and the live service tests enable the profile `all-tests`.

### Build artifact overview

#### adapter-client

The `adapter-client` module creates an executable JAR with all dependencies shaded in.
It loads the configuration model from the filesystem in the current working directory.
If the client is closed the JVM process will be terminated.

### adapter

The `adapter` module is more suitable for embedding the adapter in other applications.
It expects to be notified of changes to the model.

## Connecting to Diffusion

The session will require the `register_handler`, `modify_topic` and `update_topic` permissions to function correctly.
If the session fails to connect to Diffusion the client is closed.

### Connection loss

If the connection is lost the session will attempt to recover.
If recovery fails the client is closed.

## Polling REST services

If the initial poll of a REST endpoint fails no topic will be created for it.
If the topic does not exist with the same metadata or it cannot be created no subsequent polls will be made.
Redirection responses will be followed.

## Backup adapter clients

Multiple instances of the adapter client can be run, but only one client will poll a given REST service and update
Diffusion with the result.

Each service will either be in a `standby` or `active` state.
This state is co-ordinated with other adapter clients and control sessions using update sources on the root topic for
the service.
This state can be observed through a `ServiceListener`.
Only when the service is `active` will the client poll it and update Diffusion.
A client can have both active and standby services.

When a client with `active` services closes one client with the service in `standby` will switch to `active` and take
over.
If there is no other client configured with the service the topics associated with the service will be removed.

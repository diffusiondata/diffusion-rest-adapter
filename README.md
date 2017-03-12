# Diffusion REST Adapter

| Documentation |
| --- |
| [Overview](documentation/Overview.md) |
| [Configuration](documentation/Configuration.md) |
| [Services](documentation/Services.md) |
| [Endpoints](documentation/Endpoints.md) |
| [ServiceSessions](documentation/ServiceSessions.md) |
| [Web Interface](documentation/WebInterface.md) |

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

The integration tests assume that the ports 8080, 8081, 8443 and 8444 are available to listen on.

## Deployable artifact overview

### adapter-client

The `adapter-client` module creates an executable JAR with all dependencies shaded in.
It loads the configuration model from the filesystem in the current working directory.
If the client is closed the JVM process will be terminated.

### adapter

The `adapter` module is more suitable for embedding the adapter in other applications.
It expects to be notified of changes to the model instead of reading from the file system.

### diffusion-rest-adapter-integrated-server

The `diffusion-rest-adapter-integrated-server` module creates an integrated application server and `adapter-client`.
It is used to deploy an instance of the Diffusion REST Adapter with a web front end as a single executable.
The configuration model is provided through the web interface.

### diffusion-rest-adapter-cf-integrated-server

The `diffusion-rest-adapter-cf-integrated-server` module provides an artifact that can be deployed as a CloudFoundry
application.
It deploys an instance of the `diffusion-rest-adapter-integrated-server` that connects to a
[Diffusion Cloud](https://docs.pushtechnology.com/cloud/latest/) service bound to the application.
It can be deployed to [IBM Bluemix](https://www.ibm.com/cloud-computing/bluemix/).

### cloudfoundry-rest-adapter

The `cloudfoundry-rest-adapter` module provides an artifact that can be deployed as a CloudFoundry
application.
It deploys an instance of the `adapter-client` with the `client-controlled-model-store` that connects to a
[Reappt](https://www.reappt.io/) service bound to the application.
It can be deployed to [IBM Bluemix](https://www.ibm.com/cloud-computing/bluemix/).
The `cloudfoundry-web-interface` should also be deployed along with it.

### cloudfoundry-web-interface

The `cloudfoundry-web-interface` module provides an artifact that can be deployed as a CloudFoundry
application.
It deploys an instance of the web interface that connects to a [Reappt](https://www.reappt.io/) service bound to the
application.
It can be deployed to [IBM Bluemix](https://www.ibm.com/cloud-computing/bluemix/).
The `cloudfoundry-rest-adapter` should also be deployed along with it.

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

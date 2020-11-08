# Diffusion REST Adapter

| Documentation |
| --- |
| [Overview](documentation/Overview.md) |
| [Configuration](documentation/Configuration.md) |
| [Services](documentation/Services.md) |
| [Endpoints](documentation/Endpoints.md) |
| [ServiceSessions](documentation/ServiceSessions.md) |

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
Only the embedded service tests are run by default.
To run only the live service tests enable the profile `live-services-test`.
To run both the embedded services tests and the live service tests enable the profile `all-tests`.

The integration tests assume that the ports 8080, 8081, 8443 and 8444 are available to listen on.

## Deployable artifact overview

### adapter-client

The `adapter-client` module creates an executable JAR with all dependencies shaded in.
It loads the configuration model from the filesystem.
It defaults to loading the configuration from the current directory but can be passed a directory as a command line argument.
If the session is lost, the JVM process will be terminated.

### adapter

The `adapter` module is more suitable for embedding the adapter in other applications.
It expects to be notified of changes to the model instead of reading from the file system.

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
Topics will be removed if they are not updated within twice the polling period.

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

## Client Versions

| Adapter Version | Client Version | Server Version |
| --- | --- | --- |
| 1.0.x | 5.9.1 | 5.9.x - 6.5.x |
| 1.1.x | 5.9.1 | 5.9.x - 6.5.x |
| 2.0.x | 6.0.3 - 6.1.x | 6.0.x - 6.5.x |
| 3.0.x | 6.2.x - 6.4.x | 6.2.x - 6.6.x |
| 4.0.x | 6.4.x - 6.6.x | 6.4.x - 6.6.x |

## Licensing

This project is licensed under the [Apache Licence, v. 2](https://www.apache.org/licenses/LICENSE-2.0).

# Services

Each REST Service can have multiple `Endpoints`.

## On configuration

When the configuration is set each service in the new model is processed.
For each service the `TopicManagementClient` and `PublishingClient` are notified of the service.
The `TopicManagementClient` registers the rootTopic of the service for removal when the session closes.
The `PublishingClient` registers the rootTopic of the service for an `EventedUpdateSource`.
A `ServiceSession` is created for the service.

After the new model has been processed the old model is cleaned up.
For each service in the old model the `PublishingClient` is notified of the service removal.
This closes the `EventedUpdateSource` created for the service.

## On active

When the `EventedUpdateSource` of a service becomes active the `ServiceSession` is started and the client attempts to
initialise each `Endpoint`.
If an `Endpoint` is initialised it will be added to the `ServiceSession`.

## On standby

When the `EventedUpdateSource` of a service enters stand by it waits to be made active.

## Also see

| [Endpoints](Endpoints.md) |
| [ServiceSessions](ServiceSessions.md) |
| [README](../README.md) |

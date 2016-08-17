# ServiceSessions

A `ServiceSession` manages the efforts to poll a service.

## Polling

When an `Endpoint` is being polled a task is scheduled to run periodically that triggers the poll request.
The response is handled asynchronously using a handler.

## Start the ServiceSession

When a `ServiceSession` is started any endpoints that have already been added will being to be polled using a handler
that will publish an update to Diffusion.

## Add an endpoint

The behaviour when adding an `Endpoint` depends on the state of the `ServiceSession`.
If the `ServiceSession` has not been started, nothing happens.
If the `ServiceSession` has been started the endpoint will be polled using a handler that will publish the response as
an update to Diffusion.

## Stop the ServiceSession

Stopping a `ServiceSession` cancels the tasks used to trigger a poll of all endpoints that have been added.

## Also see

| [Services](Services.md) |
| [Endpoints](Endpoints.md) |
| [ServiceSessions](ServiceSessions.md) |
| [README](../README.md) |

# Overview

## Topic management

An update source is registered for each service.
When it becomes active a `JSON` topic will be created for each endpoint of the service.

The client will request that the topics are removed with the session for each service.

## Polling

The client polls each endpoint at the rate configured for the service.
When a response is received it is converted to a `JSON` value and published to the Diffusion topic created for the
endpoint.

## Reconfiguration

### Detecting reconfiguration

The client currently support polling a persisted model for changes.
When a change is detected it will reconfigure to use the new model.

### Applying reconfiguration

The client will perform a partial reconfiguration to update only what has changed.
On reconfiguration new components are first created and started.
Once the new components are in place the old components are stopped and removed.
The new components may be in standby until the old components are stopped.

## Also see

[README](../README.md)

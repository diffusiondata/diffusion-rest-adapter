# Web Interface

A web interface for managing the REST adapter configuration can be deployed as part of the application.
It allows you to add and remove services and endpoints dynamically.
It is deployed as part of the `diffusion-rest-adapter-integrated-server` artifact.

The web interface uses Diffusion to communicate with the REST adapter.
The REST adapter holds the configuration model in memory.

To login to the interface you need to provide a username and password known to the Diffusion server you are managing.
The principal should have permission to send and receive messages on the path `adapter/rest/model/store`.

| Also see |
| --- |
| [README](../README.md) |

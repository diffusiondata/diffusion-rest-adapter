# Endpoints

## On initialisation

When an endpoint is initialised the client performs a chain of actions.

1. Request the endpoint
2. Validate the content type of the response for the endpoint type
3. Parse the response for the endpoint type
4. Create a topic with the initial value from the response
5. Add the endpoint to the `ServiceSession`

If any stage fails the subsequent stages will not be attempted.

## Also see

| [Services](Services.md) |
| [ServiceSessions](ServiceSessions.md) |
| [README](../README.md) |

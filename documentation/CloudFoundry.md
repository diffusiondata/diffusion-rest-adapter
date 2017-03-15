# CloudFoundry

CloudFoundry is a Platform as a Service (PaaS) offering.
It allows applications to be run in the cloud and to access services.
The [IBM Bluemix](https://www.ibm.com/cloud-computing/bluemix/) platform supports CloudFoundry applications and can
access Diffusion servers as a service through [Diffusion Cloud](https://docs.pushtechnology.com/cloud/latest/).

The REST adapter can be deployed to run as an application in a CloudFoundry service.
To configure a REST adapter running in CloudFoundry the web interface must be used.

The REST adapter and web interface can be deployed either as a single application
(`diffusion-rest-adapter-cf-integrated-server`) or as separate applications (`cloudfoundry-rest-adapter` and
`cloudfoundry-web-interface`).

The REST adapter will discover a Diffusion Cloud service that has been bound to the application and obtain the
credentials to access it from CloudFoundry.
CloudFoundry will make the web interface available publicly.
Access to the web interface requires login with credentials configured in the Diffusion Cloud service.

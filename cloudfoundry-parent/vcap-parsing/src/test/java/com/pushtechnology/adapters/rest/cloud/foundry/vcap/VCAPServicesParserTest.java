package com.pushtechnology.adapters.rest.cloud.foundry.vcap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Unit tests for {@link VCAPServicesParser}.
 *
 * @author Push Technology Limited
 */
public final class VCAPServicesParserTest {

    @Test
    public void parse() throws IOException {
        final String testVcapServices = "{\n" +
            "    \"push-reappt\": [\n" +
            "        {\n" +
            "            \"credentials\": {\n" +
            "                \"host\": \"example.us.reappt.io\",\n" +
            "                \"principal\": \"binding\",\n" +
            "                \"credentials\": \"password\"\n" +
            "            },\n" +
            "            \"syslog_drain_url\": null,\n" +
            "            \"label\": \"push-reappt\",\n" +
            "            \"provider\": null,\n" +
            "            \"plan\": \"free\",\n" +
            "            \"name\": \"cloudfoundry-name\",\n" +
            "            \"tags\": [\n" +
            "                \"(S) Cloud\",\n" +
            "                \"web_and_app\",\n" +
            "                \"Bluemix\",\n" +
            "                \"Platform\",\n" +
            "                \"(S) Internet of Things\",\n" +
            "                \"Service\",\n" +
            "                \"Runs on SoftLayer\",\n" +
            "                \"Internet of Things\",\n" +
            "                \"(P) IT Infrastructure\",\n" +
            "                \"Mobile\",\n" +
            "                \"Supply Chain\",\n" +
            "                \"web and app\",\n" +
            "                \"(S) Mobile\",\n" +
            "                \"ibm_dedicated_public\",\n" +
            "                \"Development\",\n" +
            "                \"Application\",\n" +
            "                \"ibm_third_party\"\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}";

        final VCAPServicesParser parser = new VCAPServicesParser();

        final VCAPServices vcapServices = parser.parse(testVcapServices);
        assertNotNull(vcapServices);

        final ServiceEntry<ReapptCredentials> serviceEntry = vcapServices.getReappt();
        assertNotNull(serviceEntry);

        assertEquals("push-reappt", serviceEntry.getLabel());
        assertEquals("cloudfoundry-name", serviceEntry.getName());

        final ReapptCredentials credentials = serviceEntry.getCredentials();
        assertNotNull(credentials);

        assertEquals("example.us.reappt.io", credentials.getHost());
        assertEquals("binding", credentials.getPrincipal());
        assertEquals("password", credentials.getCredentials());
    }

    @Test(expected = JsonMappingException.class)
    public void parseEmpty() throws IOException {
        final String testVcapServices = "{\n" +
            "    \"push-reappt\": []\n" +
            "}";

        final VCAPServicesParser parser = new VCAPServicesParser();

        parser.parse(testVcapServices);
    }
}

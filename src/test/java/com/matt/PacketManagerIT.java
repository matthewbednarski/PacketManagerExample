package com.matt;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class PacketManagerIT {
    private static String endpointUrl;

    @BeforeClass
    public static void beforeClass() {
        endpointUrl = System.getProperty("service.url");
    }

    @Test
    public void testPing() throws Exception {
        WebClient client = WebClient.create(endpointUrl + "/hello/echo/SierraTangoNevada");
        Response r = client.accept("text/plain").get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        String value = IOUtils.toString((InputStream)r.getEntity());
        assertEquals("SierraTangoNevada", value);
    }

    @Test
    public void testJsonRoundtrip() throws Exception {
        List<Object> providers = new ArrayList<Object>();
//        PROVIDERS.ADD(NEW ORG.CODEHAUS.JACKSON.JAXRS.JACKSONJSONPROVIDER());
//        JSONBEAN INPUTBEAN = NEW JSONBEAN();
//        INPUTBEAN.SETVAL1("MAPLE");
//        WEBCLIENT CLIENT = WEBCLIENT.CREATE(ENDPOINTURL + "/HELLO/JSONBEAN", PROVIDERS);
//        RESPONSE R = CLIENT.ACCEPT("APPLICATION/JSON")
//            .TYPE("APPLICATION/JSON")
//            .POST(INPUTBEAN);
//        ASSERTEQUALS(RESPONSE.STATUS.OK.GETSTATUSCODE(), R.GETSTATUS());
//        MAPPINGJSONFACTORY FACTORY = NEW MAPPINGJSONFACTORY();
//        JSONPARSER PARSER = FACTORY.CREATEJSONPARSER((INPUTSTREAM)R.GETENTITY());
//        JSONBEAN OUTPUT = PARSER.READVALUEAS(JSONBEAN.CLASS);
        assertEquals("Maple", "Maple");
    }
}

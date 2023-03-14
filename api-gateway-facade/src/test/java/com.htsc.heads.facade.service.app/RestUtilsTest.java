package com.xchen.heimdall.facade.service.app;

import com.xchen.heimdall.facade.service.app.util.RestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author xchen
 * @date 2022/3/23
 */
class RestUtilsTest {

    @Test
    void testNormalizeUrl() {

        String exceptedUrl = "http://127.0.0.1:7007/rds/nats";

        assertEquals(exceptedUrl, RestUtils.normalizeUrl("http://127.0.0.1:7007//rds/nats"));

        assertEquals(exceptedUrl, RestUtils.normalizeUrl("http://127.0.0.1:7007/rds/nats/"));

        assertEquals(exceptedUrl, RestUtils.normalizeUrl("http://127.0.0.1:7007//rds///nats/"));

        assertEquals(exceptedUrl, RestUtils.normalizeUrl(exceptedUrl));
    }

    @Test
    void testAppendQueryString() {
        String data = "{\"userId\": \"xchen\"}";
        String url = "http://127.0.0.1:7007/rds/nats";

        assertEquals("http://127.0.0.1:7007/rds/nats?userId=xchen",
                RestUtils.appendQueryString(url, data));

        data = "{\"userId\": \"xchen\", \"pageSize\": 1, \"pageNum\": 10}";
        assertEquals("http://127.0.0.1:7007/rds/nats?userId=xchen&pageSize=1&pageNum=10",
                RestUtils.appendQueryString(url, data));

        data = "";
        assertEquals("http://127.0.0.1:7007/rds/nats",
                RestUtils.appendQueryString(url, data));
    }

    @Test
    void testAppendComplexQueryString() {
        String url = "http://127.0.0.1:7007/test";
        String data = null;

        // test str arr
        data = "{\"a\":[\"aa\", \"ab\", \"ac\"]}";
        assertEquals("http://127.0.0.1:7007/test?a=aa,ab,ac",
                RestUtils.appendQueryString(url, data));
        // test integer arr
        data = "{\"a\":[1, 2, 3]}";
        assertEquals("http://127.0.0.1:7007/test?a=1,2,3",
                RestUtils.appendQueryString(url, data));
        // test double arr
        data = "{\"a\":[1.1, 2.2, 3.3]}";
        assertEquals("http://127.0.0.1:7007/test?a=1.1,2.2,3.3",
                RestUtils.appendQueryString(url, data));

        // test pojo
        data = "{\"a\":1,\"b\":{\"a\":1,\"b\":2}}";
        assertEquals("http://127.0.0.1:7007/test?a=1&b.a=1&b.b=2",
                RestUtils.appendQueryString(url, data));

        // test complex pojo
        data = "{\"a\":\"aa\",\"b\":[\"ba\",\"bb\"],\"c\":[\"ca\",\"cb\"],\"d\":{\"da\":\"daa\",\"db\":[\"dba\",\"dbb\",\"dbc\"],\"dc\":[\"dca\",\"dcb\"]}}";
        assertEquals("http://127.0.0.1:7007/test?a=aa&b=ba,bb&c=ca,cb&d.da=daa&d.db=dba,dbb,dbc&d.dc=dca,dcb",
                RestUtils.appendQueryString(url, data));
    }




}

package io.nosqlbench.docsys.core;

import org.junit.Test;

public class DocsysMarkdownEndpointTest {

    @Test
    public void testDocLoader() {
        DocsysMarkdownEndpoint ep = new DocsysMarkdownEndpoint();
        String markdownList = ep.getMarkdownList(true);
    }

}

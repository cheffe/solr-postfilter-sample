/*
 * MIT License
 *
 * Copyright (c) 2019 cheffe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.cheffe.solr.postfilter;

import com.google.common.truth.Truth;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.core.CoreContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.io.IOException;

@TestInstance(Lifecycle.PER_CLASS)
class PostFilterPerformanceTest {
  private CoreContainer container;
  private EmbeddedSolrServer server;

  PostFilterPerformanceTest() {
  }

  @BeforeAll
  void startSolrServer() {
    this.container = new CoreContainer("src/test/resources/solrHome");
    this.container.load();
    this.server = new EmbeddedSolrServer(this.container, "postfilter");
  }

  @AfterAll
  void stopSolrServer() throws IOException {
    this.server.close();
    this.container.shutdown();
  }

  @Test
  void ping() throws IOException, SolrServerException {
    SolrPingResponse response = this.server.ping();
    Truth.assertThat(response.toString()).contains("status=OK");
  }
}
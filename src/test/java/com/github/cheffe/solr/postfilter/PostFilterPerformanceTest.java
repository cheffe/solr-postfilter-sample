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

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.String.format;

@TestInstance(Lifecycle.PER_CLASS)
class PostFilterPerformanceTest {
  private CoreContainer container;
  private EmbeddedSolrServer server;

  @BeforeAll
  void startSolrServer() throws IOException, SolrServerException {
    container = new CoreContainer("src/test/resources/solrHome");
    container.load();
    server = new EmbeddedSolrServer(container, "postfilter");

    createSampleData();
  }

  private void createSampleData() throws IOException, SolrServerException {
    int batchSize = 100000;
    List<SolrInputDocument> batch = new ArrayList<>(batchSize);
    for (int id = 1; id < 180001; id++) {
      SolrInputDocument doc = new SolrInputDocument();
      doc.setField("id", id);

      List<Integer> ints = new ArrayList<>(10);
      List<String> strings = new ArrayList<>(10);
      for(int i = 0; i < 10; i++) {
        ints.add(id % 100);
        strings.add("string-" + id % 100);
      }
      doc.setField("ints", ints);
      doc.setField("strings", strings);

      String customid = id % 200000 + "_" +  format ("%03d", 1 + id / 200000);
      doc.setField("customid", customid);

      batch.add(doc);
      if (batch.size() == batchSize) {
        server.add(batch);
        batch.clear();
      }

    }
    if (batch.size() > 0) {
      server.add(batch);
    }
    server.commit(true, true);
  }

  @AfterAll
  void stopSolrServer() throws IOException {
    server.close();
    container.shutdown();
  }

  @ParameterizedTest
  @ValueSource(ints = {50, 500, 1000, 1500, 2000, 2500, 5000})
  void benchmark(int count) throws IOException, SolrServerException {
    SolrQuery query  = new SolrQuery("*:*");
    query.addFilterQuery(format("{!idFilter count=%d}", count));
    QueryResponse response = server.query(query);
    System.out.format("count=%d - QTime=%d", count, response.getQTime());
    assertThat(response.getResults().getNumFound()).isEqualTo(count);
  }
}
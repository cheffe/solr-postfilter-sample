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

import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.search.DelegatingCollector;
import org.apache.solr.search.ExtendedQueryBase;
import org.apache.solr.search.PostFilter;

import java.io.IOException;
import java.util.HashSet;

public class IdFilter extends ExtendedQueryBase implements PostFilter {

  private HashSet<BytesRef> customSet;

  IdFilter(HashSet<BytesRef> customSet) {
    super();
    this.customSet = customSet;
  }

  @Override
  public boolean getCache() {
    return false;
  }

  @Override
  public int getCost() {
    return Math.max(super.getCost(), 100);
  }

  @Override
  public DelegatingCollector getFilterCollector(IndexSearcher searcher) {
    return new DelegatingCollector() {

      private SortedDocValues sortedDocValues;

      @Override
      protected void doSetNextReader(LeafReaderContext context) throws IOException {
        super.doSetNextReader(context);
        sortedDocValues = DocValues.getSorted(context.reader(), "customid");
      }

      @Override
      public void collect(int docNumber) throws IOException {
        if (sortedDocValues.advanceExact(docNumber) && isValid(sortedDocValues.binaryValue())) {
          super.collect(docNumber);
        }
      }

      private boolean isValid(BytesRef customId) {
        return customSet.contains(customId);
      }

    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IdFilter that = (IdFilter) o;

    if (this.customSet == null) {
      return that.customSet == null;
    }
    if (that.customSet == null) {
      return false;
    }

    return this.customSet.hashCode() == that.customSet.hashCode();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + (customSet != null ? customSet.hashCode() : 0);
    return result;
  }

}
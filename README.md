# solr-postfilter-sample
Trying to reproduce the performance described on StackOverflow [SOLR 7+ / Lucene 7+ and performance issues with DelegatingCollector and PostFilter](https://stackoverflow.com/q/57663857/2160152).

## results

This table includes the runtime in _ms_ per notable change.

| filter size | 50 | 500 | 1000 | 1500 | 2000 | 2500 | 5000 |
|---|---|---|---|---|---|---|---|
| initial | 723 |371 |341 | 335 | 324 | 339 | 356 |
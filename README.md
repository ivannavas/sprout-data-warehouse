# Sprout Data Warehouse

A personal data warehouse for everything I generate about myself, built with
[Sprout](https://github.com/ivannavas/sprout-ai-framework), my own AI framework.

The point is to have it all in one place so it can be crossed: charted over time, correlated between
sources, or simply asked about in plain language — when did I start something, how did a relationship
evolve, how did I sleep the nights I argued with someone, do my projects move when I am training. The
interesting things are the ones that span sources, which is exactly what no single app I already use can
show me, whether as an answer or as a graph.

There are two halves to that. **Getting the data in:** I keep adding sources, each one normalised into
the same model, so there is steadily more to cross. Some arrive structured and just need mapping; others
are unstructured, and a Sprout agent reads them and writes out the records — as with my voice diary,
whose transcriptions are also indexed as embeddings so they stay searchable semantically. **Getting it
back out:** dashboards and charts over the structured records, and a query agent that can reach both
those records and the embeddings. Neither is built yet — the extraction side came first, because
without data there is nothing to plot or ask about.

Nothing an agent writes is trusted straight away: it all lands pending validation, and I confirm it
myself through the API. That is what makes anything built on top of it worth trusting — I can decide
whether a chart or an answer draws only on what I have confirmed, or on everything.

> **This is a personal project.** It is built for me, for my data, and for the way I live. It will grow
> as I need it to, and the shape of it changes whenever what I have falls short. It is not a product, it
> has no roadmap, and it is not trying to solve anyone else's problem. If it is useful to you, go ahead
> — but decisions get made based on what I need.

## Running it

Java 21 and Spring Boot, against PostgreSQL with pgvector. The database stays on my own machine, but the
extraction runs on Anthropic's API (Claude) and the embeddings on Voyage: the transcriptions themselves
do leave the machine.

Configuration is in `application.yml`, which has local defaults and reads the database, model and source
locations from the environment.

```
mvn spring-boot:run
```

The schema is not generated automatically — tables are created by hand.

# Sprout Data Warehouse

A personal data warehouse built on top of my voice diary, and built with
[Sprout](https://github.com/ivannavas/sprout-ai-framework), my own AI framework.

Every day I record what happened. The transcription lands on disk, and a Sprout agent reads it and
writes whatever is structured in it to the database — people, feelings, activities, whatever I am
tracking at the time. The transcription also gets indexed as embeddings, so the history can be queried
semantically.

The point is to be able to ask that history questions later — when did I start something, how did a
relationship evolve, how long since I saw someone — instead of sitting on years of audio I am never
going to listen to again.

Nothing the agent writes is trusted straight away: it all lands pending validation, and I confirm it
myself through the API.

> **This is a personal project.** It is built for me, for my data, and for the way I keep my diary.
> It will grow as I need it to, and the shape of it changes whenever what I have falls short. It is not
> a product, it has no roadmap, and it is not trying to solve anyone else's problem. If it is useful to
> you, go ahead — but decisions get made based on what I need.

## Running it

Java 21 and Spring Boot, against PostgreSQL with pgvector and a local Ollama. Everything runs on my own
machine: the transcriptions are a personal diary and do not leave it.

Configuration is in `application.yml`, which has local defaults and reads the database, model and diary
location from the environment.

```
mvn spring-boot:run
```

The schema is not generated automatically — tables are created by hand.

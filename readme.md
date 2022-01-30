# Fully

A polylith "workspace as a framework": A polylith workspace that comes pre-bundled with a set of built-in components
that would cover most of the needs of a web application, from database connectivity, routing, schema and validation,
graphql/eql, and messaging in both frontend and backend applications.

## Goals
- Fully's components (or otherwise libraries) live in your project, just as your own components would, thus making them
  hackable and introspectable.
- Fully should be a personalizable framework, meaning you can keep your own flavor of Fully forked with your additional
  built-in components to use between your projects.
- The framework not only provide components, but also the ability of gluing them together without hassle. A lot of the
  time spent when making a project is deciding your tech-stack, and then gluing all the libraries together. Fully aims
  to provide tha capability to start working on business logic Day One.
- Components should be usable à la carte. Meaning you choose how much of the framework you want to use. The framework
  should provide different tools of building your applications,
- Components should adhere to SOLID principles: When the built-in components adhere to SOLID principles, it's easier to
  build your own components using them that will also adhere to SOLID principles.
- Components do not reinvent the wheel. They use already existing libraries of the ecosystem and abstract them into a
  sane API.
- Each component implements an interface/protocol. This interface double-acts as a specification as well, so that
  multiple components of the same interface should not only correctly implement the interface on the code-level, but
  should follow the specification on the API-level
- The two points above entail that inter-component interaction is not dependent on the underlying implementation/library
  dependencies. Assuming this property holds true also means that the "gluing" of these components is facilitated, by
  the framework.
- Library/Stack agnostic: Fully will provide, out of the box, components for most use-cases covering the most used
  libraries/stack. If, for example, a database driver component does not exist for a specific database, you could
  implement your own, following the interface/specification and being sure that other components that depend on it would
  work just fine. If a built-in component cover most of what you need but not quite enough, by having it live in your
  workspace means that you can just add your missing functionality.
- Component abstraction are not meant to have complete parity with the underlying library. For example if the xtdb
  database component's query is not enough for your business logic, then you could use XTDB's datalog api instead.
  Re-wrapping all of Clojure's ecosystem is not the goal, but abstracting the functionality that's necessary by the rest
  of the built-in components and most common use-cases

## Interfaces

### Backend

| interface       | Description
|-----------------|-------------
| repository      | Act's as an abstraction (a little as an ORM), over database connectivity. Ability to do simple queries
| ring-handler    | Provides a Ring handler.
| ring-server     | Provides functionality for running a Ring web server (e.g Jetty, Undertow)
| errors          | Provides exception throwing error functions and a wrap-errors middleware function handling errors
| middleware      | Provides a sane default wrap-middleware.
| routes          | The HTTP routes of your application. This is fed into ring-handler to produce the handler.
| system          | Provides the [system](https://github.com/stuartsierra/component) of your application, wires all your components and their dependencies together.
|

### Frontend (WIP)

### Common

| interface      | Description
|----------------|-----------------------
| schema         | Provides the schema of your Application's domain.
| schema-manager | Abstraction over schema libraries (like Malli). Handles validation and facilitates in building schema-driven logic.
| schema-utils   | Utils for schema transformation etc.
| logger         | Abstraction over logging, either locally to console, file or remotely to an external system.
| resolver       | Abstraction over graph/logic resolvers/mutations (i.e GraphQL, Pathom)
| config         | Reading and loading configurations (from file, ENV, remote etc.)
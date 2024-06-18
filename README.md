# Two-Stage Guice Provider

by [Jean Tessier](https://jeantessier.com/)

This document shows how to pass an object between
[Guice](https://github.com/google/guice) providers so each one can initialize it
in turn.

The finalized code makes up the rest of this repo.

All examples use Guice 7.0.  They were tested with Spock 2.4 using Java 21.

## Introduction

I got the idea for this pattern as I was trying to reuse a framework.  This
framework had been written for one specific application but its authors felt it
was generic enough to write other applications.  I was to be the first one to
try and use it outside its original application.

The framework uses Guice for its dependency injection.  Early on, I encountered
a problem where the original application did a lot of custom settings up in a
provider method of its `Module`.  I needed to have a similar module that would
duplicate a lot of the setup, minus the bits specific to the original
application, plus some new bits specific to my application.  I wanted to
extract the common parts to one generic module and trick Guice into chaining
providers on the same object to complete its initialization.

This is their story.

## Generic Service

Imagine the following service interface:

```java
package service;

public interface Service {
    void setupClient1();
    void setupClient2();
    String getState();
}
```

It has different setup methods for different clients.  This example is to
illustrate that different clients might set up the service differently.  In real
life, there would be no dependency from the service upon its clients.  For a
more concrete example, the service might be a web server class and each client
might configure a different set of URLs.

For the purposes of this discussion, here is a quick specification of what we
are looking for:

```groovy
package service

import spock.lang.Specification

class ServiceImplTest extends Specification {

    def sut = new ServiceImpl()

    def "has default state"() {
        expect:
        sut.state == "generic"
    }

    def "setupClient1 sets the state to client1"() {
        when:
        sut.setupClient1()

        then:
        sut.state == "client1"
    }

    def "setupClient2 sets the state to client2"() {
        when:
        sut.setupClient2()

        then:
        sut.state == "client2"
    }

}
```

And here is a simple implementation that satisfied it:

```java
package service;

public class ServiceImpl implements Service {
    private String state = "generic";

    public void setupClient1() {
        state = "client1";
    }

    public void setupClient2() {
        state = "client2";
    }

    public String getState() {
        return state;
    }
}
```

## Generic Guice Module

To begin with, we need a base `Module` that creates a generic `Service`
implementation.  Here is a test for it:

```groovy
package generic

import spock.lang.Specification

class GenericModuleTest extends Specification {

    def sut = new GenericModule()

    def "provides a generic service"() {
        when:
        def actualService = sut.provideService()

        then:
        actualService != null
        actualService.state == "generic"
    }

}
```

And here is a generic Guice module with a provider method to create a plain
`ServiceImpl` whenever a `Service` instance is required.

```java
package generic;

import com.google.inject.*;
import service.*;

public class GenericModule extends AbstractModule {
    @Provides
    public Service provideService() {
        return new ServiceImpl();
    }
}
```

You can use it like this:

    Injector injector = Guice.createInjector(new GenericModule());
    Service service = injector.getInstance(Service.class);

The variable `service` now references a plain `ServiceImpl`.

## Specialized Guice Modules

Different clients who require a customized `Service` instance need to use their
own module instead of the previous `GenericModule`.

A hypothetical client No. 1 could need a module that follows the following
specification, that it calls `setupClient1()` on an existing `Service` instance:

```groovy
package client1

import service.Service
import spock.lang.Specification

class Client1ModuleTest extends Specification {

    def sut = new Client1Module()

    def "configures a generic service for client1"() {
        given:
        def mockService = Mock(Service)

        when:
        def actualService = sut.provideService(mockService)

        then:
        actualService == mockService

        and:
        1 * mockService.setupClient1()
    }

}
```

This hypothetical client No. 1 could use this module:

```java
package client1;

import com.google.inject.*;
import service.*;

public class Client1Module extends AbstractModule {
    @Provides
    public Service provideService() {
        Service service = new ServiceImpl();
        service.setupClient1();
        return service;
    }
}
```

A hypothetical client No. 2 could require this specification instead:

```groovy
package client2

import service.Service
import spock.lang.Specification

class Client2ModuleTest extends Specification {

    def sut = new Client2Module()

    def "configures a generic service for client2"() {
        given:
        def mockService = Mock(Service)

        when:
        def actualService = sut.provideService(mockService)

        then:
        actualService == mockService

        and:
        1 * mockService.setupClient2()
    }

}
```

Which this implementation satisfies:

```java
package client2;

import com.google.inject.*;
import service.*;

public class Client2Module extends AbstractModule {
    @Provides
    public Service provideService() {
        Service service = new ServiceImpl();
        service.setupClient2();
        return service;
    }
}
```

These two modules have some problems.

1. There is code duplication between both modules when it comes to instantiating `ServiceImpl`.
1. Each module is tied to using the `ServiceImpl` implementation of `Service`.

It would be better if we could somehow inject a `Service` implementation and
have the module do its custom configuration.

## A Solution That Does Not Work

Ideally, I would like to separate the choice of `Service` implementation, as
provided by `GenericModule`, from the application-specific configuration, as
provided by `Client1Module` and `Client2Module`.

I could rewrite `Client1Module` as:

```java
    package client1;

    import com.google.inject.*;
    import service.*;

    public class Client1Module extends AbstractModule {
        @Provides
        public Service provideService(Service service) {
            service.setupClient1();
            return service;
        }
    }
```

And use it like this:

    Injector injector = Guice.createInjector(new GenericModule(), new Client1Module());
    Service service = injector.getInstance(Service.class);

I would hope that Guice picks to call `Client1Module.provideService()` to
satisfy my request and injects a `Service` into this call by calling
`GenericModule.provideService()`.  But that's not the case.  Because I now have
two providers for `Service`, Guice gets confused and throws an error with the
following message:

    A binding to service.Service was already configured at generic.GenericModule.provideService().

I need to differentiate between the two providers, somehow.

## A Solution That Works

I can use annotations to distinguish between the kinds of `Service` each
provider is responsible for.

First, I qualify the `Service` provided by `GenericModule` as "generic".

```java
package generic;

import com.google.inject.*;
import com.google.inject.name.*;
import service.*;

public class GenericModule extends AbstractModule {
    @Provides
    @Named("generic")
    public Service provideService() {
        return new ServiceImpl();
    }
}
```

I will also qualify the `Service` provided by client No. 1's module.  This way,
it is explicit in the calling code which `Service` I am interested in.

```java
package client1;

import com.google.inject.*;
import com.google.inject.name.*;
import service.*;

public class Client1Module extends AbstractModule {
    @Provides
    @Named("client1")
    public Service provideService(@Named("generic") Service service) {
        service.setupClient1();
        return service;
    }
}
```

Here, it is made explicit that client No. 1 requires a "client1" flavor of
`Service`, which will be provided by `Client1Module.provideService()`.  The
latter requires a "generic" flavor of `Service`, which will be provided by
`GenericModule.provideService()`.  The client simply asks for what it needs and
lets Guice take care of the rest.

    Injector injector = Guice.createInjector(new GenericModule(), new Client1Module());
    Service service = injector.getInstance(Key.get(Service.class, Names.named("client1")));

Client No. 2 would use the following module to obtain a "client2" flavor of
`Service`.

```java
package client2;

import com.google.inject.*;
import com.google.inject.name.*;
import service.*;

public class Client2Module extends AbstractModule {
    @Provides
    @Named("client2")
    public Service provideService(@Named("generic") Service service) {
        service.setupClient2();
        return service;
    }
}
```

And use it like this:

    Injector injector = Guice.createInjector(new GenericModule(), new Client2Module());
    Service service = injector.getInstance(Key.get(Service.class, Names.named("client2")));

## More Realistic

More realistically, I would declare a `Client1` class that requires a "client1"
kind of `Service`:

```java
package client1;

import com.google.inject.*;
import com.google.inject.name.*;
import generic.*;
import service.*;

public class Client1 extends Client {
    @Inject
    public Client1(@Named("client1") Service service) {
        super(service);
    }
}
```

And a `Client2` class that requires a "client2" kind of `Service`:

```java
package client2;

import com.google.inject.*;
import com.google.inject.name.*;
import generic.*;
import service.*;

public class Client2 extends Client {
    @Inject
    public Client2(@Named("client2") Service service) {
        super(service);
    }
}
```

Here is a simple, generic `Client` superclass:

```java
package generic;

import service.*;

public class Client {
    private final Service service;

    public Client(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }
}
```

The nice thing is that `Client` does not need to know about the difference
between clients No. 1 and No. 2.  It deals exclusively in terms of the generic
`Service` interface.  Guice uses the configuration information to pick the
right implementation and customize it appropriately.

    Injector injector = Guice.createInjector(new GenericModule(), new Client1Module());
    Client client = injector.getInstance(Client1.class);

## Conclusion

This pattern for using Guice might prove useful.

Another possibility is that the way `Service` gets customized is all wrong.  I
could conceive of a `Customizer` object that would get injected into
`GenericModule.provideService()`.  `Client1Module` and `Client2Module` would
provide bindings to specific types of `Customizer`, and that would be the end
of it.  This design might be more robust.

But if you cannot change `GenericModule`, then maybe the pattern I just covered
can be of assistance.

## Further Developments

After I sent this document to a few people for review, someone brought up the
[upcoming `@New` annotation for Guice](http://code.google.com/p/google-guice/issues/detail?id%3D231).
It seems to address some of the same concerns as the pattern I describe on this
page.  One difference is that my pattern can be chained for as long as
necessary, whereas you can use the `@New` annotation only once.

----

| Date       | Edit                                                                                    |
|------------|-----------------------------------------------------------------------------------------|
| 2009-01-28 | First draft.                                                                            |
| 2009-01-29 | Last substantial edit.                                                                  |
| 2020-04-28 | Making sure it all still works with the latest version of Guice, convert tests to Spock |
| 2020-05-03 | Remove empty `configure()` methods that are no longer needed in Guice `4.2.3`.          |

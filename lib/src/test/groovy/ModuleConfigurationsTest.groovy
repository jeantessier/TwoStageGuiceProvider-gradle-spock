import client1.Client1
import client1.Client1Module
import client2.Client2
import client2.Client2Module
import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.name.Names
import generic.GenericModule
import service.Service
import spock.lang.Specification
import spock.lang.Unroll

class ModuleConfigurationsTest extends Specification {

    def genericModule = new GenericModule()

    @Unroll
    def "gets a service configured for #name"() {
        given:
        def injector = Guice.createInjector(genericModule, specificModule)

        expect:
        def actualService = injector.getInstance(Key.get(Service, Names.named(name)))
        actualService.state == expectedState

        where:
        specificModule | name | expectedState
        new Client1Module() | "client1" | "client1"
        new Client2Module() | "client2" | "client2"
    }

    @Unroll
    def "gets a client for #clientClass"() {
        given:
        def injector = Guice.createInjector(genericModule, specificModule)

        expect:
        def actualClient = injector.getInstance(clientClass)
        actualClient.service.state == expectedState

        where:
        specificModule | clientClass | expectedState
        new Client1Module() | Client1 | "client1"
        new Client2Module() | Client2 | "client2"
    }

}

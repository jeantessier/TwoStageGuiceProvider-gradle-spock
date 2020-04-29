package client1

import service.Service
import spock.lang.Specification

class Client1ModuleTest extends Specification {

    def sut = new Client1Module()

    def "configure does nothing"() {
        expect:
        sut.configure()
    }

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

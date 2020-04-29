package client2

import service.Service
import spock.lang.Specification

class Client2ModuleTest extends Specification {

    def sut = new Client2Module()

    def "configure does nothing"() {
        expect:
        sut.configure()
    }

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

package generic

import spock.lang.Specification

class GenericModuleTest extends Specification {

    def sut = new GenericModule()

    def "configure does nothing"() {
        expect:
        sut.configure()
    }

    def "provides a generic service"() {
        when:
        def actualService = sut.provideService()

        then:
        actualService != null
        actualService.state == "generic"
    }

}

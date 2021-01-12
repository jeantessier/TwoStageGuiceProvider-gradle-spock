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

/*
 *  Copyright (c) 2001-2009, Jean Tessier
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *
 *      * Neither the name of Jean Tessier nor the names of his contributors
 *        may be used to endorse or promote products derived from this software
 *        without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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

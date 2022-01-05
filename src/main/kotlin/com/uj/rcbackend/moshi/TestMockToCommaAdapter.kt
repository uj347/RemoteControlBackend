package com.uj.rcbackend.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.uj.rcbackend.commandinvokerpart.commandhierarchy.Command
import com.uj.rcbackend.commandinvokerpart.commandhierarchy.mocks.MockCommand

class TestMockToCommaAdapter {

    @ToJson
    fun comToMock (command: Command): MockCommand {
        return MockCommand()
    }
    @FromJson
    fun mockToCom(mock: MockCommand): Command {
      return  mock
    }
}
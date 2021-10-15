package remotecontrolbackend.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import remotecontrolbackend.command_invoker_part.command_hierarchy.Command
import remotecontrolbackend.command_invoker_part.command_hierarchy.MockCommand

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
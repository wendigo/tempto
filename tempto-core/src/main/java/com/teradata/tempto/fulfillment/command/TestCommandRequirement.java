/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teradata.tempto.fulfillment.command;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Commands will be executed before the test.
 */
public class TestCommandRequirement
        extends CommandRequirement
{
    public static TestCommandRequirement testCommand(String command)
    {
        return new TestCommandRequirement(new Command(command));
    }

    public TestCommandRequirement(Command setupCommand)
    {
        this(singletonList(setupCommand));
    }

    public TestCommandRequirement(List<Command> setupCommands)
    {
        super(setupCommands);
    }

    @Override
    public String toString()
    {
        return "TestCommandRequirement{} " + super.toString();
    }
}
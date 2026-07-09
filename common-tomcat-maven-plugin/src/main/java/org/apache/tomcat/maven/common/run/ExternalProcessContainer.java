package org.apache.tomcat.maven.common.run;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.concurrent.TimeUnit;

/**
 * Simple wrapper so {@link EmbeddedRegistry} can stop forked external JVMs through its existing reflection contract.
 */
public class ExternalProcessContainer
{
    private final Process process;

    private final String description;

    public ExternalProcessContainer( Process process, String description )
    {
        this.process = process;
        this.description = description;
    }

    public void stop()
        throws InterruptedException
    {
        if ( !process.isAlive() )
        {
            return;
        }

        process.destroy();
        if ( !process.waitFor( 5, TimeUnit.SECONDS ) )
        {
            process.destroyForcibly();
            process.waitFor( 5, TimeUnit.SECONDS );
        }
    }

    @Override
    public String toString()
    {
        return description;
    }
}

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

import java.io.IOException;
import java.lang.reflect.Method;
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

        destroyProcessTreeOnWindows();
        process.destroy();
        if ( !process.waitFor( 5, TimeUnit.SECONDS ) )
        {
            process.destroyForcibly();
            process.waitFor( 5, TimeUnit.SECONDS );
        }
    }

    private void destroyProcessTreeOnWindows()
        throws InterruptedException
    {
        if ( !System.getProperty( "os.name" ).toLowerCase().contains( "win" ) )
        {
            return;
        }

        try
        {
            Long pid = getProcessId();
            if ( pid == null )
            {
                return;
            }
            Process taskkill = new ProcessBuilder( "taskkill", "/PID", Long.toString( pid ), "/T", "/F" ).start();
            taskkill.waitFor( 5, TimeUnit.SECONDS );
        }
        catch ( IOException e )
        {
            // process.destroy() below remains a best-effort fallback.
        }
    }

    private Long getProcessId()
    {
        try
        {
            Method pidMethod = Process.class.getMethod( "pid" );
            return (Long) pidMethod.invoke( process );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    @Override
    public String toString()
    {
        return description;
    }
}

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExternalProcessContainerTest
{
    @Test
    public void shouldDestroyProcessWhenItDoesNotExitGracefully()
        throws Exception
    {
        TestProcess process = new TestProcess();

        new ExternalProcessContainer( process, "test process" ).stop();

        assertEquals( 1, process.destroyCalls );
        assertEquals( 1, process.destroyForciblyCalls );
    }

    @Test
    public void shouldStopARealChildJvm()
        throws Exception
    {
        Process process = new ProcessBuilder( getJavaExecutable(), "-cp", System.getProperty( "java.class.path" ),
                                              Sleeper.class.getName() ).start();

        new ExternalProcessContainer( process, "sleeper" ).stop();

        assertTrue( "Child JVM did not exit", process.waitFor( 5, TimeUnit.SECONDS ) );
    }

    private String getJavaExecutable()
    {
        String executable = System.getProperty( "os.name" ).toLowerCase().contains( "win" ) ? "java.exe" : "java";
        return new File( new File( System.getProperty( "java.home" ), "bin" ), executable ).getAbsolutePath();
    }

    public static final class Sleeper
    {
        public static void main( String[] args )
            throws InterruptedException
        {
            Thread.sleep( TimeUnit.MINUTES.toMillis( 1 ) );
        }
    }

    private static final class TestProcess
        extends Process
    {
        private int destroyCalls;

        private int destroyForciblyCalls;

        @Override
        public OutputStream getOutputStream()
        {
            return new ByteArrayOutputStream();
        }

        @Override
        public InputStream getInputStream()
        {
            return new ByteArrayInputStream( new byte[0] );
        }

        @Override
        public InputStream getErrorStream()
        {
            return new ByteArrayInputStream( new byte[0] );
        }

        @Override
        public int waitFor()
        {
            return 0;
        }

        @Override
        public boolean waitFor( long timeout, TimeUnit unit )
        {
            return false;
        }

        @Override
        public int exitValue()
        {
            return 0;
        }

        @Override
        public void destroy()
        {
            destroyCalls++;
        }

        @Override
        public Process destroyForcibly()
        {
            destroyForciblyCalls++;
            return this;
        }

        @Override
        public boolean isAlive()
        {
            return true;
        }
    }
}

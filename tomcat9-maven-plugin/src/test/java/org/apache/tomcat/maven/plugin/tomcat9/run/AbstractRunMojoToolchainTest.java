package org.apache.tomcat.maven.plugin.tomcat9.run;
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

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AbstractRunMojoToolchainTest
{
    @After
    public void clearToolchainFlag()
    {
        System.clearProperty( AbstractRunMojo.TOOLCHAIN_FORKED_INVOCATION_PROPERTY );
    }

    @Test
    public void shouldSkipToolchainLookupInsideForkedInvocation()
        throws Exception
    {
        TestRunMojo mojo = new TestRunMojo();
        mojo.toolchainManager = new StaticToolchainManager( new StaticToolchain( "/fake/bin/java" ) );

        System.setProperty( AbstractRunMojo.TOOLCHAIN_FORKED_INVOCATION_PROPERTY, "true" );

        assertEquals( null, mojo.getToolchainJavaExecutable() );
    }

    @Test
    public void shouldReturnJavaExecutableFromToolchain()
        throws Exception
    {
        TestRunMojo mojo = new TestRunMojo();
        mojo.toolchainManager = new StaticToolchainManager( new StaticToolchain( "/toolchains/jdk/bin/java" ) );

        assertEquals( "/toolchains/jdk/bin/java", mojo.getToolchainJavaExecutable() );
    }

    @Test
    public void shouldFailWhenToolchainDoesNotExposeJava()
        throws Exception
    {
        TestRunMojo mojo = new TestRunMojo();
        mojo.toolchainManager = new StaticToolchainManager( new StaticToolchain( null ) );

        try
        {
            mojo.getToolchainJavaExecutable();
            fail( "Expected missing java executable to fail." );
        }
        catch ( MojoExecutionException expected )
        {
            assertTrue( expected.getMessage().contains( "does not define a java executable" ) );
        }
    }

    @Test
    public void shouldBuildMavenCommandWithExecutionIdAndForkOverride()
        throws Exception
    {
        TestRunMojo mojo = new TestRunMojo();
        List<String> command = mojo.buildToolchainMavenCommand( "/toolchains/jdk/bin/java" );

        assertEquals( "/toolchains/jdk/bin/java", command.get( 0 ) );
        assertTrue( command.contains( "-Dmaven.tomcat.fork=false" ) );
        assertTrue( command.contains( "-D" + AbstractRunMojo.TOOLCHAIN_FORKED_INVOCATION_PROPERTY + "=true" ) );
        assertTrue( command.contains( "-Dsample.flag=sample-value" ) );
        assertTrue( command.contains( "io.github.cnhongwei:tomcat9-maven-plugin:3.0.120-SNAPSHOT:run@toolchain-run" ) );
        assertTrue( command.contains( mojo.project.getFile().getAbsolutePath() ) );
    }

    private static final class TestRunMojo
        extends AbstractRunMojo
    {
        private TestRunMojo()
        {
            Model model = new Model();
            model.setGroupId( "org.example" );
            model.setArtifactId( "sample" );
            model.setVersion( "1.0-SNAPSHOT" );

            project = new MavenProject( model );
            project.setFile( new File( "pom.xml" ).getAbsoluteFile() );
            project.setBasedir( new File( "." ).getAbsoluteFile() );

            Properties userProperties = new Properties();
            userProperties.setProperty( "sample.flag", "sample-value" );
            session = new MavenSession( null, new Settings(), null, null, null, Collections.emptyList(), "",
                                        new Properties(), userProperties, new Date() );

            PluginDescriptor pluginDescriptor = new PluginDescriptor();
            pluginDescriptor.setGroupId( "io.github.cnhongwei" );
            pluginDescriptor.setArtifactId( "tomcat9-maven-plugin" );
            pluginDescriptor.setVersion( "3.0.120-SNAPSHOT" );

            MojoDescriptor mojoDescriptor = new MojoDescriptor();
            mojoDescriptor.setPluginDescriptor( pluginDescriptor );
            mojoDescriptor.setGoal( "run" );

            mojoExecution = new MojoExecution( mojoDescriptor, "toolchain-run" );
        }

        @Override
        protected File getDocBase()
        {
            return new File( "." );
        }

        @Override
        protected File getContextFile()
        {
            return null;
        }
    }

    private static final class StaticToolchainManager
        implements ToolchainManager
    {
        private final Toolchain toolchain;

        private StaticToolchainManager( Toolchain toolchain )
        {
            this.toolchain = toolchain;
        }

        public Toolchain getToolchainFromBuildContext( String type, MavenSession session )
        {
            return toolchain;
        }
    }

    private static final class StaticToolchain
        implements Toolchain
    {
        private final String javaExecutable;

        private StaticToolchain( String javaExecutable )
        {
            this.javaExecutable = javaExecutable;
        }

        public String getType()
        {
            return "jdk";
        }

        public String findTool( String toolName )
        {
            if ( "java".equals( toolName ) )
            {
                return javaExecutable;
            }
            return null;
        }
    }
}

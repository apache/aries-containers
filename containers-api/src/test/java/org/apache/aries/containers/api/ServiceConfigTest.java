package org.apache.aries.containers.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.aries.containers.ServiceConfig;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ServiceConfigTest {
    @Test
    public void testServiceConfig1() {
        ServiceConfig sc = ServiceConfig.builder("svc1", "myimg").build();
        assertEquals("svc1", sc.getServiceName());
        assertEquals("myimg", sc.getContainerImage());
    }

    @Test
    public void testServiceConfig2() {
        ServiceConfig sc = ServiceConfig.builder("svc1", "myimg").
                commandLine("-c", "runscript.sh").
                entryPoint("/bin/sh").
                cpu(3.142).
                env("foo", "bar").
                env("a", "b c d").
                instances(17).
                memory(5.5).
                port(8080, true).
                port(9090).
                build();

        assertArrayEquals(new String[] {"-c", "runscript.sh"}, sc.getCommandLine());
        assertEquals("/bin/sh", sc.getEntryPoint());
        assertEquals(3.142, sc.getRequestedCpuUnits(), 0.001);

        Map<String, String> expectedEnv = new HashMap<>();
        expectedEnv.put("foo", "bar");
        expectedEnv.put("a", "b c d");
        assertEquals(expectedEnv, sc.getEnvVars());
        assertEquals(17, sc.getRequestedInstances());
        assertEquals(5.5, sc.getRequestedMemory(), 0.01);
        assertEquals(8080, sc.getMainPort());
        assertEquals(Arrays.asList(8080, 9090), sc.getContainerPorts());
    }

    @Test
    public void testServiceConfig3() {
        Map<String, String> env = new HashMap<>();
        env.put("a", "b");
        env.put("c", "d");

        ServiceConfig sc = ServiceConfig.builder("svc1", "myimg").
                env(env).build();
        assertEquals(env, sc.getEnvVars());
    }

}

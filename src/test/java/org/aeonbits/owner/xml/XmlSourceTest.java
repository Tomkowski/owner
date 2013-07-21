/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.xml;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Luigi R. Viggiano
 */
public class XmlSourceTest {

    public static interface ServerConfig extends Config, Accessible {

        @Key("server.http.port")
        int httpPort();
        
        @Key("server.http.hostname")
        String httpHostname();
        
        @Key("server.ssh.port")
        int sshPort();

        @Key("server.ssh.address")
        String sshAddress();
        
        @Key("server.ssh.alive.interval")
        int aliveInterval();
        
        @Key("server.ssh.user")
        String sshUser();
    }

    @Test
    public void testXmlReading() {
        ServerConfig cfg = ConfigFactory.create(ServerConfig.class);
        assertEquals(80, cfg.httpPort());
        assertEquals("localhost", cfg.httpHostname());
        assertEquals(22, cfg.sshPort());
        assertEquals("127.0.0.1", cfg.sshAddress());
        assertEquals(60, cfg.aliveInterval());
        assertEquals("admin", cfg.sshUser());
    }
    
    @Test
    public void testStoreToXML() throws IOException {
        ServerConfig cfg = ConfigFactory.create(ServerConfig.class);
        File target = new File("target/test-generated-resources/XmlSourceTest$ServerConfig.properties.xml");
        target.getParentFile().mkdirs();
        cfg.storeToXML(new FileOutputStream(target), "this is an example");
        
        Properties props = new Properties();
        props.loadFromXML(new FileInputStream(target));

        assertEquals(String.valueOf(cfg.httpPort()), props.getProperty("server.http.port"));
        assertEquals(cfg.httpHostname(), props.getProperty("server.http.hostname"));
        assertEquals(String.valueOf(cfg.sshPort()), props.getProperty("server.ssh.port"));
        assertEquals(cfg.sshAddress(), props.getProperty("server.ssh.address"));
        assertEquals(String.valueOf(cfg.aliveInterval()), props.getProperty("server.ssh.alive.interval"));
        assertEquals(cfg.sshUser(), props.getProperty("server.ssh.user"));
    }

}

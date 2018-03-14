package com.credits.service;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class ServiceTest {

    protected void clean(String address) {
        File folder = new File(System.getProperty("user.dir") + File.separator + "credits" +
            File.separator + address);
        if (folder.exists()) {
            File[] list = folder.listFiles();
            for (File file : list) {
                file.delete();
            }
            folder.delete();
        }
    }
}

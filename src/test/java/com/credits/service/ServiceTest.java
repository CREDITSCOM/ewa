package com.credits.service;

import com.credits.App;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {App.class})
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

package com.notes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
//@ServletComponentScan(basePackages = {"com.notes.spring.filter"})
public class JavaNotesApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(JavaNotesApplication.class);
        ApplicationContext applicationContext = builder.headless(false).run(args);
        Swingclient swing = applicationContext.getBean(Swingclient.class);
        swing.setVisible(true);
    }

}

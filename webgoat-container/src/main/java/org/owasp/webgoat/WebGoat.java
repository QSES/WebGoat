package org.owasp.webgoat;

import org.owasp.webgoat.plugins.PluginsLoader;
import org.owasp.webgoat.session.Course;
import org.owasp.webgoat.session.WebSession;
import org.owasp.webgoat.session.WebgoatContext;
import org.owasp.webgoat.session.WebgoatProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import javax.servlet.ServletContext;
import java.io.File;

@SpringBootApplication
@PropertySource("classpath:/webgoat.properties")
public class WebGoat extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WebGoat.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebGoat.class, args);
    }

    @Bean(name = "pluginTargetDirectory")
    public File pluginTargetDirectory() {
        File tempDir = com.google.common.io.Files.createTempDir();
        tempDir.deleteOnExit();
        return tempDir;
    }

    @Bean
    public PluginsLoader pluginsLoader(@Qualifier("pluginTargetDirectory") File pluginTargetDirectory) {
        System.out.println("Plugin target directory: " + pluginTargetDirectory.toString());
        return new PluginsLoader(pluginTargetDirectory);
    }

    @Bean
    @Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public WebSession webSession(Course course, WebgoatContext webgoatContext, ServletContext context) {
        return new WebSession(course, webgoatContext, context);
    }

    @Bean
    public Course course(PluginsLoader pluginsLoader, WebgoatContext webgoatContext, ServletContext context,
                         WebgoatProperties webgoatProperties) {
        Course course = new Course(webgoatProperties);
        course.loadCourses(webgoatContext, context, "/");
        course.loadLessonFromPlugin(pluginsLoader.loadPlugins());
        return course;
    }
}
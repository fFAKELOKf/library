package com.khazhimetov.library.config;

import com.khazhimetov.library.dao.*;
import com.khazhimetov.library.dao.impl.*;
import com.khazhimetov.library.dao.BookDao;
import com.khazhimetov.library.dao.OrderDao;
import com.khazhimetov.library.dao.UserDao;
import com.khazhimetov.library.dao.impl.BookDaoImpl;
import com.khazhimetov.library.dao.impl.OrderDaoImpl;
import com.khazhimetov.library.dao.impl.UserDaoImpl;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * Main application configuration class.
 * <p>
 * Configures core Spring beans such as DAO implementations,
 * message source for i18n, and locale resolution/interceptor settings.
 */
@Configuration
@ComponentScan(basePackages = "com.khazhimetov.library")
@PropertySource("classpath:application.properties")
public class AppConfig {

    /**
     * Enables support for resolving placeholders in properties files.
     *
     * @return PropertySourcesPlaceholderConfigurer instance
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * Configures and returns the UserDao implementation bean.
     *
     * @return a new instance of UserDaoImpl
     */
    @Bean
    public UserDao userDao() {
        return new UserDaoImpl();
    }


    /**
     * Configures and returns the BookDao implementation bean.
     *
     * @return a new instance of BookDaoImpl
     */
    @Bean
    public BookDao bookDao() {
        return new BookDaoImpl();
    }

    /**
     * Configures and returns the OrderDao implementation bean.
     *
     * @return a new instance of OrderDaoImpl
     */
    @Bean
    public OrderDao orderDao() {
        return new OrderDaoImpl();
    }

    /**
     * Configures the message source for internationalization (i18n).
     *
     * @return ResourceBundleMessageSource with base name and encoding
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }

    /**
     * Configures the locale resolver that uses cookies to store the user's locale.
     *
     * @return CookieLocaleResolver configured with defaults
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setCookieName("locale");
        resolver.setCookieMaxAge(3600);
        return resolver;
    }

    /**
     * Defines an interceptor to switch locales based on the request parameter.
     *
     * @return a configured LocaleChangeInterceptor
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
}

package com.khazhimetov.library.config;

import com.khazhimetov.library.interceptor.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Locale;

/**
 * Spring MVC configuration class.
 * <p>
 * Configures Thymeleaf view resolver, static resources, locale resolution,
 * and request interceptors including custom authentication logic.
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    private LocaleChangeInterceptor localeChangeInterceptor;

    /**
     * Constructor injection for LocaleChangeInterceptor.
     *
     * @param localeChangeInterceptor the interceptor for changing locale based on request parameter
     */
    @Autowired
    public WebConfig(LocaleChangeInterceptor localeChangeInterceptor) {
        this.localeChangeInterceptor = localeChangeInterceptor;
    }

    /**
     * Configures the session-based locale resolver.
     *
     * @return configured LocaleResolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    /**
     * Configures the Thymeleaf template resolver.
     *
     * @return SpringResourceTemplateResolver
     */
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("/WEB-INF/views/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    /**
     * Configures the Thymeleaf template engine.
     *
     * @return SpringTemplateEngine with Spring EL support enabled
     */
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    /**
     * Configures the Thymeleaf view resolver.
     *
     * @return configured ThymeleafViewResolver
     */
    @Bean
    public ViewResolver viewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setCharacterEncoding("UTF-8");
        viewResolver.setContentType("text/html; charset=UTF-8");
        return viewResolver;
    }

    /**
     * Registers static resource handlers for CSS and JS files.
     *
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("/static/js/");
    }

    /**
     * Adds locale change and authentication interceptors.
     *
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor);
        registry.addInterceptor(new AuthenticationInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/login", "/register", "/static/**", "/books", "/books/search");
    }

    /**
     * Maps the root path (\"/\") to redirect to the book listing page.
     *
     * @param registry the view controller registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/books");
    }
}

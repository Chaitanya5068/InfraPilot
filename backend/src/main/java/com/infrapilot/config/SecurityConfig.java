package com.infrapilot.config;

import com.infrapilot.ui.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/v1/health", "/actuator/health", "/actuator/prometheus").permitAll()
                .anyRequest().permitAll());
        setLoginView(http, LoginView.class);
    }
}

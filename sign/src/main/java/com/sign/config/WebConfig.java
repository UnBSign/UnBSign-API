// package com.sign.config;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
// import com.sign.interceptor.JwtInterceptor;

// @Configuration
// public class WebConfig implements WebMvcConfigurer {

//     @Bean
//     public JwtInterceptor jwtInterceptor() {
//         return new JwtInterceptor();
//     }

//     @Override
//     public void addInterceptors(InterceptorRegistry registry) {
//         registry.addInterceptor(jwtInterceptor())
//                 .addPathPatterns("/api/**"); // Define as rotas a serem interceptadas
//     }
// }


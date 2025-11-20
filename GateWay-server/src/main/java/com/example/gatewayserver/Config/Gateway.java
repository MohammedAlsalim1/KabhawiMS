package com.example.gatewayserver.Config;

import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.Buildable;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class Gateway {
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Forge Routes
                .route("authForge-signUp", Gateway::signUp)
                .route("authForge-signIn", Gateway::signIn)
                .route("authForge-parseToken", Gateway::parseToken)

                // Product Server Routes
                .route("product-server", Gateway::productServer)
                // cart Server Routes
                .route("cart-server", Gateway::cartServer)
                .build();
    }

    @Bean
    public CorsWebFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
    private static Buildable<Route> parseToken(PredicateSpec predicateSpec) {
        return predicateSpec.path("/parse-token")
                .uri("lb://auth-forge");
    }

    private static Buildable<Route> signIn(PredicateSpec predicateSpec) {
        return predicateSpec.path("/login")
                .uri("lb://auth-forge");
    }

    private static Buildable<Route> signUp(PredicateSpec predicateSpec) {
        return predicateSpec.path("/signUp")
                .filters(f -> f.rewritePath("/signUp", "/sign-up"))
                .uri("lb://auth-forge");
    }
    private static Buildable<Route> productServer(PredicateSpec predicateSpec) {
        return predicateSpec.path("/api/**")
                .uri("lb://product-server");
    }
    private static Buildable<Route> cartServer(PredicateSpec predicateSpec) {
        return predicateSpec.path("/api/**")
                .uri("lb://cart-server");
    }
}

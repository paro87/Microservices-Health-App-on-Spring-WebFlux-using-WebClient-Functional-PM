package com.paro.departmentservice.router;

import com.paro.departmentservice.handler.DepartmentHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class DepartmentRouter {

    @Bean
    public RouterFunction<ServerResponse> router(DepartmentHandler departmentHandler) {
        RouterFunction<ServerResponse> departmentRoutes = RouterFunctions
                .route(GET("/").and(accept(APPLICATION_JSON)), departmentHandler::getAll)
                .andRoute(GET("/{id}").and(accept(APPLICATION_JSON)), departmentHandler::getByDepartmentId)
                .andRoute(POST("/").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), departmentHandler::add)
                .andRoute(GET("/hospital/{hospitalId}").and(accept(APPLICATION_JSON)), departmentHandler::getByHospitalId)
                .andRoute(GET("/hospital/{hospitalId}/with-patients").and(accept(APPLICATION_JSON)), departmentHandler::getByHospitalWithPatients);


        RouterFunction<ServerResponse> nestedRoute =RouterFunctions.nest(RequestPredicates.path("/v1"), departmentRoutes);

        return departmentRoutes;
        //For Swagger
        //return nestedRoute;

    }

    //2 - With builder
/*
    @Bean
    public RouterFunction<ServerResponse> router(DepartmentHandler departmentHandler) {
        RouterFunction<ServerResponse> departmentRoutes = RouterFunctions.route()
                .path("", builder -> builder
                        .GET("/", accept(MediaType.APPLICATION_JSON), departmentHandler::getAll))
                        .GET("/{id}", accept(MediaType.APPLICATION_JSON), departmentHandler::getByDepartmentId)
                        .POST("/", accept(MediaType.APPLICATION_JSON), departmentHandler::add)
                        .GET("/hospital/{hospitalId}", accept(MediaType.APPLICATION_JSON), departmentHandler::getByHospitalId)
                        .GET("/hospital/{hospitalId}/with-patients", accept(MediaType.APPLICATION_JSON), departmentHandler::getByHospitalWithPatients)
                .build();
        RouterFunction<ServerResponse> nestedRoute =RouterFunctions.nest(RequestPredicates.path("/v1"), departmentRoutes);

        //return hospitalRoutes;
        //For Swagger
        return nestedRoute;
    }
*/

}

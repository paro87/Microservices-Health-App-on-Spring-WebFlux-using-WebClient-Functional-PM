package com.paro.hospitalservice.router;

import com.paro.hospitalservice.handler.HospitalHandler;
import com.paro.hospitalservice.model.Hospital;
import com.paro.hospitalservice.repository.HospitalRepository;
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
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class HospitalRouter {

    @Bean
    public RouterFunction<ServerResponse> router(HospitalHandler hospitalHandler) {
        RouterFunction<ServerResponse> hospitalRoutes = RouterFunctions
                .route(GET("/").and(accept(APPLICATION_JSON)), hospitalHandler::getAll)
                .andRoute(GET("/{id}").and(accept(APPLICATION_JSON)), hospitalHandler::getByHospitalId)
                .andRoute(POST("/").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), hospitalHandler::add)
                .andRoute(PUT("/{id}").and(accept(APPLICATION_JSON)), hospitalHandler::put)
                .andRoute(PATCH("/{id}").and(accept(APPLICATION_JSON)), hospitalHandler::patch)
                .andRoute(DELETE("/{id}").and(accept(APPLICATION_JSON)), hospitalHandler::delete)
                .andRoute(GET("/{id}/with-departments").and(accept(APPLICATION_JSON)), hospitalHandler::getHospitalWithDepartments)
                .andRoute(GET("/{id}/with-departments-and-patients").and(accept(APPLICATION_JSON)), hospitalHandler::getHospitalWithDepartmentsAndPatients)
                .andRoute(GET("/{id}/with-patients").and(accept(APPLICATION_JSON)), hospitalHandler::getHospitalWithPatients);

        RouterFunction<ServerResponse> nestedRoute =RouterFunctions.nest(RequestPredicates.path("/service"), hospitalRoutes);

        //return hospitalRoutes;
        //For Swagger
        return nestedRoute;

    }

    //2 - With builder
/*    @Bean
    public RouterFunction<ServerResponse> router(HospitalHandler hospitalHandler) {
        RouterFunction<ServerResponse> hospitalRoutes =  RouterFunctions.route()
                .path("", builder -> builder
                        .GET("/", accept(MediaType.APPLICATION_JSON), hospitalHandler::getAll))
                        .GET("/{id}", accept(MediaType.APPLICATION_JSON), hospitalHandler::getByHospitalId)
                        .POST("/", accept(MediaType.APPLICATION_JSON), hospitalHandler::add)
                        .GET("/{id}/with-departments", accept(MediaType.APPLICATION_JSON), hospitalHandler::getHospitalWithDepartments)
                        .GET("/{id}/with-departments-and-patients", accept(MediaType.APPLICATION_JSON), hospitalHandler::getHospitalWithDepartmentsAndPatients)
                        .GET("/{id}/with-patients", accept(MediaType.APPLICATION_JSON), hospitalHandler::getHospitalWithPatients)
                .build();

        RouterFunction<ServerResponse> nestedRoute =RouterFunctions.nest(RequestPredicates.path("/v1"), hospitalRoutes);

        //return hospitalRoutes;
        //For Swagger
        return nestedRoute;
    }*/
}

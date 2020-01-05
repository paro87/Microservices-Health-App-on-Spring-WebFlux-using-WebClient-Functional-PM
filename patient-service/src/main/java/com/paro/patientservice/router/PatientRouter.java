package com.paro.patientservice.router;

import com.paro.patientservice.handler.PatientHandler;
import com.paro.patientservice.model.Patient;
import com.paro.patientservice.repository.PatientRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class PatientRouter {
    //1

    @Bean
    public RouterFunction<ServerResponse> router(PatientHandler patientHandler) {
        RouterFunction<ServerResponse> patientRoutes = RouterFunctions
                .route(GET("/").and(accept(APPLICATION_JSON)), patientHandler::getAll)
                .andRoute(GET("/{id}").and(accept(APPLICATION_JSON)), patientHandler::getByPatientId)
                .andRoute(POST("/").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), patientHandler::add)
                .andRoute(PUT("/{id}").and(accept(APPLICATION_JSON)), patientHandler::put)
                .andRoute(PATCH("/{id}").and(accept(APPLICATION_JSON)), patientHandler::patch)
                .andRoute(DELETE("/{id}").and(accept(APPLICATION_JSON)), patientHandler::delete)
                .andRoute(GET("/department/{departmentId}").and(accept(APPLICATION_JSON)), patientHandler::getByDepartmentId)
                .andRoute(GET("/hospital/{hospitalId}").and(accept(APPLICATION_JSON)), patientHandler::getByHospitalId);

        RouterFunction<ServerResponse> nestedRoute =RouterFunctions.nest(RequestPredicates.path("/service"), patientRoutes);

        //return patientRoutes;

        return nestedRoute;

    }

/*
    //2 - Without PatientHandler class
    @Bean
    public RouterFunction<ServerResponse> router (PatientRepository patientRepository) {
        return route(GET("/"), request -> ok().contentType(APPLICATION_JSON).body(patientRepository.findAll(), Patient.class))
                .andRoute(GET("/{id}"), request -> ok().contentType(APPLICATION_JSON).body(patientRepository.findByPatientId(Long.valueOf(request.pathVariable("id"))), Patient.class))
                .andRoute(POST("/"), request -> ok().contentType(APPLICATION_JSON).body(patientRepository.saveAll(request.bodyToMono(Patient.class)), Patient.class))
                .andRoute(GET("/department/{departmentId}"), request -> ok().contentType(APPLICATION_JSON).body(patientRepository.findByDepartmentId(Long.valueOf(request.pathVariable("departmentId"))), Patient.class))
                .andRoute(GET("/hospital/{hospitalId}"), request -> ok().contentType(APPLICATION_JSON).body(patientRepository.findByHospitalId(Long.valueOf(request.pathVariable("hospitalId"))), Patient.class));
    }
*/
/*
    //3
    @Bean
    public RouterFunction<ServerResponse> router(PatientHandler patientHandler) {
        return RouterFunctions.route()
                .path("", builder -> builder
                        .GET("/", accept(MediaType.APPLICATION_JSON), patientHandler::getAll))
                        .GET("/{id}", accept(MediaType.APPLICATION_JSON), patientHandler::getByPatientId)
                        .POST("/", accept(MediaType.APPLICATION_JSON), patientHandler::add)
                        .GET("/department/{departmentId}", accept(MediaType.APPLICATION_JSON), patientHandler::getByDepartmentId)
                        .GET("/hospital/{hospitalId}", accept(MediaType.APPLICATION_JSON), patientHandler::getByHospitalId)
                .build();
    }
*/
}

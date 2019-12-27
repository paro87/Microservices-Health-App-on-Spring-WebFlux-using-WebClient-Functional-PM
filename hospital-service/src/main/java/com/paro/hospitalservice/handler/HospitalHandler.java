package com.paro.hospitalservice.handler;


import com.paro.hospitalservice.model.Department;
import com.paro.hospitalservice.model.Hospital;
import com.paro.hospitalservice.model.Patient;
import com.paro.hospitalservice.repository.HospitalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.hystrix.HystrixCommands;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component

public class HospitalHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HospitalHandler.class);

    private String departmentClient="http://department-service";
    private String patientClient="http://patient-service";
    private static final String RESOURCE_PATH="/hospital/";
    private String REQUEST_URI_Department=departmentClient+RESOURCE_PATH;
    private String REQUEST_URI_Patient=patientClient+RESOURCE_PATH;

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Autowired
    private WebClient.Builder webClientBuilder;

    private HospitalRepository hospitalRepository;
    @Autowired
    public void HospitalService (HospitalRepository hospitalRepository){
        this.hospitalRepository=hospitalRepository;

    }



    public Mono<ServerResponse> getByHospitalId(ServerRequest request) {
        Long patientId= Long.valueOf(request.pathVariable("id"));
        return ok().contentType(MediaType.APPLICATION_JSON).body(hospitalRepository.findByHospitalId(patientId), Hospital.class);

    }
    
    /*public Mono<ServerResponse> getAll(ServerRequest request) {
        return ok().contentType(MediaType.APPLICATION_JSON).body(hospitalRepository.findAll(), Hospital.class);
    }*/

    //Knoldus
    public Mono<ServerResponse> getAll(ServerRequest request) {
        return ok().contentType(MediaType.APPLICATION_JSON).body(fromPublisher(hospitalRepository.findAll(), Hospital.class));
    }

    public Mono<ServerResponse> add(ServerRequest request) {
        //1-The only solution for saving Mono - https://stackoverflow.com/questions/47918441/why-spring-reactivemongorepository-doest-have-save-method-for-mono
        Mono<Hospital> hospital=request.bodyToMono(Hospital.class);
        Mono<Hospital> hospitalSaved=hospital.flatMap(entity -> hospitalRepository.save(entity));
        return ServerResponse.status(HttpStatus.OK).body(hospitalSaved, Hospital.class);

        //2
        /*Mono<Hospital> hospitalAdded= hospitalRepository.saveAll(hospital).next();
        return hospitalAdded;*/

        //3
        // return ServerResponse.ok().build(hospitalRepository.saveAll(request.bodyToMono(Hospital.class)).next());
    }

    public Mono<ServerResponse> getHospitalWithDepartments(ServerRequest request) {
        Long hospitalId= Long.valueOf(request.pathVariable("id"));
        Mono<Hospital> hospitalList=hospitalRepository.findByHospitalId(hospitalId);
        LOGGER.info("Departments found with hospital id={}", hospitalId);
        //1-Returning value without usage of Hystrix
        /*
        Mono<Hospital> hospitalMono= hospitalList.flatMap(hospital ->{
            Flux<Department> departmentFlux = webClientBuilder.build().get().uri(REQUEST_URI_Department + hospital.getHospitalId()).exchange().flatMapMany(response -> response.bodyToFlux(Department.class));
            return departmentFlux.collectList()
                    .map(list->{
                        hospital.setDepartmentList(list);
                        return hospital;
                    });
        });
        */
        //2-Using Hystrix: Returns only List<Department> as null
        Mono<Hospital> hospitalMono=hospitalList.flatMap(hospital ->{
            Flux<Department> departmentFlux = webClientBuilder.build().get().uri(REQUEST_URI_Department + hospital.getHospitalId()).exchange().flatMapMany(response -> response.bodyToFlux(Department.class));
            Mono<Hospital> hospitalMono2= departmentFlux.collectList()
                    .map(list->{
                        hospital.setDepartmentList(list);
                        return hospital;
                    });
            return HystrixCommands
                    .from(hospitalMono2)
                    .fallback(d->{
                        List<Department> departmentListNotFound=new ArrayList<>();
                        Department departmentNotFound=new Department(0L, "UNKNOWN", hospitalId);
                        departmentListNotFound.add(departmentNotFound);
                        hospital.setDepartmentList(departmentListNotFound);
                        return Mono.just(hospital);
                    })
                    .commandName("getHospitalWithDepartments_Fallback")
                    .toMono();
        });
        return ok().contentType(MediaType.APPLICATION_JSON).body(fromPublisher(hospitalMono, Hospital.class));
    }

    public Mono<ServerResponse> getHospitalWithDepartmentsAndPatients(ServerRequest request) {
        Long hospitalId= Long.valueOf(request.pathVariable("id"));
        Mono<Hospital> hospitalList=hospitalRepository.findByHospitalId(hospitalId);
        LOGGER.info("Departments and patients found with hospital id={}", hospitalId);
        //1-1-Returning value without usage of Hystrix
        /*
        Mono<Hospital> hospitalMono= hospitalList.flatMap(hospital ->{
            Flux<Department> departmentFlux = webClientBuilder.build().get().uri(REQUEST_URI_Department + hospital.getHospitalId()+"/with-patients").exchange().flatMapMany(response -> response.bodyToFlux(Department.class));
            return departmentFlux.collectList()
                    .map(list->{
                        hospital.setDepartmentList(list);
                        return hospital;
                    });
        });
        */
        Mono<Hospital> hospitalMono=hospitalList.flatMap(hospital ->{
            Flux<Department> departmentFlux = webClientBuilder.build().get().uri(REQUEST_URI_Department + hospital.getHospitalId()+"/with-patients").exchange().flatMapMany(response -> response.bodyToFlux(Department.class));
            Mono<Hospital> hospitalMono2= departmentFlux.collectList()
                    .map(list->{
                        hospital.setDepartmentList(list);
                        return hospital;
                    });
            return HystrixCommands
                    .from(hospitalMono2)
                    .fallback(d->{
                        List<Department> departmentListNotFound=new ArrayList<>();
                        Department departmentNotFound=new Department(0L, "UNKNOWN", hospitalId);
                        departmentListNotFound.add(departmentNotFound);
                        hospital.setDepartmentList(departmentListNotFound);
                        return Mono.just(hospital);
                    })
                    .commandName("getHospitalWithDepartmentsAndPatients_Fallback")
                    .toMono();
        });
        return ok().contentType(MediaType.APPLICATION_JSON).body(fromPublisher(hospitalMono, Hospital.class));

    }

    public Mono<ServerResponse> getHospitalWithPatients(ServerRequest request) {
        Long hospitalId= Long.valueOf(request.pathVariable("id"));
        Mono<Hospital> hospitalList=hospitalRepository.findByHospitalId(hospitalId);
        LOGGER.info("Patients found with hospital id={}", hospitalId);
        //1-Returning value without usage of Hystrix
        /*
        Mono<Hospital> hospitalMono= hospitalList.flatMap(hospital ->{
            Flux<Patient> patientFlux = webClientBuilder.build().get().uri(REQUEST_URI_Patient + hospital.getHospitalId()).exchange().flatMapMany(response -> response.bodyToFlux(Patient.class));
            return patientFlux.collectList()
                    .map(list->{
                        hospital.setPatientList(list);
                        return hospital;
                    });
        });
        */
        //2-Using Hystrix: Returns only List<Patient> as null
        Mono<Hospital> hospitalMono=hospitalList.flatMap(hospital ->{
            Flux<Patient> patientFlux = webClientBuilder.build().get().uri(REQUEST_URI_Patient + hospital.getHospitalId()).exchange().flatMapMany(response -> response.bodyToFlux(Patient.class));
            Mono<Hospital> hospitalMono2= patientFlux.collectList()
                    .map(list->{
                        hospital.setPatientList(list);
                        return hospital;
                    });
            return HystrixCommands
                    .from(hospitalMono2)
                    .fallback(d->{
                        List<Patient> patientListNotFound=new ArrayList<>();
                        Patient patientNotFound=new Patient(0L, "UNKNOWN", "UNKNOWN", hospitalId, 0L);
                        patientListNotFound.add(patientNotFound);
                        hospital.setPatientList(patientListNotFound);
                        return Mono.just(hospital);
                    })
                    .commandName("getHospitalWithPatients_Fallback")
                    .toMono();
        });

        return ok().contentType(MediaType.APPLICATION_JSON).body(fromPublisher(hospitalMono, Hospital.class));
    }
}

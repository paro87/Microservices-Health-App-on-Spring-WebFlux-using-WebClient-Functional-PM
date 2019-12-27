package com.paro.patientservice.handler;

import com.paro.patientservice.model.Patient;
import com.paro.patientservice.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.xml.ws.Response;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component

public class PatientHandler {
    private PatientRepository patientRepository;
    @Autowired
    public PatientHandler(PatientRepository patientRepository){
        this.patientRepository=patientRepository;
    }

/*    public Mono<ServerResponse> getAll(ServerRequest request) {
        return ok().contentType(MediaType.APPLICATION_JSON).body(patientRepository.findAll(), Patient.class);
    }*/

    public Mono<ServerResponse> getByPatientId(ServerRequest request) {
        Long patientId= Long.valueOf(request.pathVariable("id"));
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(patientRepository.findByPatientId(patientId), Patient.class);

    }

    public Mono<ServerResponse> add(ServerRequest request) {
        //1-The only solution for saving Mono - https://stackoverflow.com/questions/47918441/why-spring-reactivemongorepository-doest-have-save-method-for-mono
        Mono<Patient> patient=request.bodyToMono(Patient.class);
        Mono<Patient> patientSaved=patient.flatMap(entity -> patientRepository.save(entity));
        return ServerResponse.status(HttpStatus.OK).body(patientSaved, Patient.class);

        //2
        /*Mono<Patient> patientAdded= patientRepository.saveAll(patient).next();
        return patientAdded;*/

        //3
        // return ServerResponse.ok().build(patientRepository.saveAll(request.bodyToMono(Patient.class)).next());
    }

    public Mono<ServerResponse> getByDepartmentId(ServerRequest request) {
        Long departmentId= Long.valueOf(request.pathVariable("departmentId"));
        return ServerResponse.ok().body(patientRepository.findByDepartmentId(departmentId), Patient.class);
    }



    public Mono<ServerResponse> getByHospitalId(ServerRequest request) {
        Long hospitalId= Long.valueOf(request.pathVariable("hospitalId"));
        return ServerResponse.ok().body(patientRepository.findByHospitalId(hospitalId), Patient.class);
    }

    //Knoldus
    public Mono<ServerResponse> getAll(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromPublisher(patientRepository.findAll(), Patient.class));
    }

}

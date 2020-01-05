package com.paro.patientservice.handler;

import com.paro.patientservice.model.Patient;
import com.paro.patientservice.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

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

    //Knoldus
    public Mono<ServerResponse> getAll(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromPublisher(patientRepository.findAll(), Patient.class));
    }

    public Mono<ServerResponse> add(ServerRequest request) {
        //1-The only solution for saving Mono - https://stackoverflow.com/questions/47918441/why-spring-reactivemongorepository-doest-have-save-method-for-mono
        Mono<Patient> patient=request.bodyToMono(Patient.class);
        Mono<Patient> patientSaved=patient.flatMap(entity -> patientRepository.save(entity));
        return ServerResponse.status(HttpStatus.CREATED).body(patientSaved, Patient.class);

        //2
        /*Mono<Patient> patientAdded= patientRepository.saveAll(patient).next();
        return patientAdded;*/

        //3
        // return ServerResponse.ok().build(patientRepository.saveAll(request.bodyToMono(Patient.class)).next());
    }

    public Mono<ServerResponse> put(ServerRequest request) {
        Long patientId= Long.valueOf(request.pathVariable("id"));
        Mono<Patient> patientToPut=request.bodyToMono(Patient.class);
        Mono<Patient> patientPut=patientRepository.findByPatientId(patientId)
                .flatMap(patient ->patientRepository.delete(patient))
                .then(patientRepository.saveAll(patientToPut).next());

        return ServerResponse.status(HttpStatus.OK).body(patientPut, Patient.class);

        // Possible 2nd solution
        /*
        Long patientId= Long.valueOf(request.pathVariable("id"));
        Mono<Patient> patientToPut=request.bodyToMono(Patient.class);
        Mono<Patient> patientFound=patientRepository.findByPatientId(patientId);
        Mono<Patient> patientPut=patientFound.map(patientFromRepo -> {
            patientToPut.map(patientBeingPut -> {
                if (patientBeingPut.getFirstname()!=null) {
                    patientFromRepo.setFirstname(patientBeingPut.getFirstname());
                }
                if (patientBeingPut.getSurname()!=null) {
                    patientFromRepo.setSurname(patientBeingPut.getSurname());
                }
                if (patientBeingPut.getDepartmentId()!=null) {
                    patientFromRepo.setDepartmentId(patientBeingPut.getDepartmentId());
                }
                if (patientBeingPut.getHospitalId()!=null) {
                    patientFromRepo.setHospitalId(patientBeingPut.getHospitalId());
                }
                return patientFromRepo;
            }).subscribe();
            return patientFromRepo;
        });
        return ServerResponse.status(HttpStatus.OK).body(patientPut, Patient.class);*/
    }

    public Mono<ServerResponse> patch(ServerRequest request) {
        Long patientId= Long.valueOf(request.pathVariable("id"));
        Mono<Patient> patientToPatch=request.bodyToMono(Patient.class);

        Mono<Patient> patientFound=patientRepository.findByPatientId(patientId);
        Mono<Patient> patientPatched=patientFound.map(patientFromRepo -> {

            patientToPatch.map(patientBeingPatched -> {

                if (patientBeingPatched.getPatientId()!=null) {
                    patientFromRepo.setPatientId(patientBeingPatched.getPatientId());
                }
                if (patientBeingPatched.getFirstname()!=null) {
                    patientFromRepo.setFirstname(patientBeingPatched.getFirstname());
                }
                if (patientBeingPatched.getSurname()!=null) {
                    patientFromRepo.setSurname(patientBeingPatched.getSurname());
                }
                if (patientBeingPatched.getDepartmentId()!=null) {
                    patientFromRepo.setDepartmentId(patientBeingPatched.getDepartmentId());
                }
                if (patientBeingPatched.getHospitalId()!=null) {
                    patientFromRepo.setHospitalId(patientBeingPatched.getHospitalId());
                }
                return patientFromRepo;
            }).subscribe();
            return patientFromRepo;
        });
        //return patientRepository.saveAll(patientPatched).next();
        return ServerResponse.status(HttpStatus.CREATED).body(patientRepository.saveAll(patientPatched).next(), Patient.class);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Long patientId= Long.valueOf(request.pathVariable("id"));
        Mono<Patient> patientToBeDeleted=patientRepository.findByPatientId(patientId);
        return patientToBeDeleted.flatMap(patient -> ServerResponse.noContent().build(patientRepository.delete(patient)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getByDepartmentId(ServerRequest request) {
        Long departmentId= Long.valueOf(request.pathVariable("departmentId"));
        return ServerResponse.ok().body(patientRepository.findByDepartmentId(departmentId), Patient.class);
    }



    public Mono<ServerResponse> getByHospitalId(ServerRequest request) {
        Long hospitalId= Long.valueOf(request.pathVariable("hospitalId"));
        return ServerResponse.ok().body(patientRepository.findByHospitalId(hospitalId), Patient.class);
    }



}

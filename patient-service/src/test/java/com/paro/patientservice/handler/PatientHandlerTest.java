package com.paro.patientservice.handler;

import com.paro.patientservice.model.Patient;
import com.paro.patientservice.repository.PatientRepository;
import com.paro.patientservice.router.PatientRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ContextConfiguration(classes = {PatientRouter.class, PatientHandler.class})
@WebFluxTest
//@Import(PatientRouter.class)
//@WebFluxTest - does not detect RouterFunction beans, so we added ContextConfiguration
class PatientHandlerTest {
    @Autowired
    private ApplicationContext context;
    @MockBean
    private PatientRepository patientRepository;

    @Autowired
    private PatientRouter patientRouter;

    private Patient patient1;
    private Patient patient2;
    private Patient patient3;
    private Patient patient4;

    private Mono<Patient> patientMono;
    private Flux<Patient> patientFlux;

    private WebTestClient testClient;

    @BeforeEach
    void setUp() {
        patient1=new Patient(1L, "John", "Grisham", 1L, 11L);
        patient2=new Patient(2L, "Mary", "Adams", 1L, 11L);
        patient3=new Patient(3L, "Adam", "Anniston", 1L, 11L);
        patient4=new Patient(4L, "Brad", "Richards", 1L, 11L);
        patientMono=Mono.just(patient1);
        patientFlux=Flux.just(patient1, patient2, patient3, patient4);

        testClient=WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void getAll() {
        given(patientRepository.findAll()).willReturn(patientFlux);
        testClient.get().uri("/")
                .exchange()             //submits the request, which will be handled by the controller that WebTestClient is bound to the PatientController
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$[0].id").isEqualTo(patient1.getId())
                .jsonPath("$[0].patientId").isEqualTo(patient1.getPatientId())
                .jsonPath("$[1].firstname").isEqualTo(patient2.getFirstname())
                .jsonPath("$[1].surname").isEqualTo(patient2.getSurname())
                .jsonPath("$[2].departmentId").isEqualTo(patient3.getDepartmentId())
                .jsonPath("$[2].hospitalId").isEqualTo(patient3.getHospitalId());
        verify(patientRepository).findAll();

    }

    @Test
    void getByPatientId() {
        given(patientRepository.findByPatientId(1L)).willReturn(patientMono);
        testClient.get().uri("/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(patient1.getId())
                .jsonPath("$.patientId").isEqualTo(patient1.getPatientId());
        verify(patientRepository).findByPatientId(1L);

    }

    @Test
    void add() {
        given(patientRepository.save(any())).willReturn(patientMono);

        String patient="{\"id\":null,\"patientId\":1,\"firstname\":\"John\",\"surname\":\"Grisham\",\"hospitalId\":1,\"departmentId\":11}";

        //2-Testing of "Receiving and saving of Mono<Patient> object"
        testClient.post().uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(patientMono, Patient.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(patient);

        verify(patientRepository).save(any());
    }

    @Test
    void getByDepartmentId() {
        given(patientRepository.findByDepartmentId(11L)).willReturn(patientFlux);
        testClient.get().uri("/department/11")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$[0].id").isEqualTo(patient1.getId())
                .jsonPath("$[1].patientId").isEqualTo(patient2.getPatientId());
        verify(patientRepository).findByDepartmentId(11L);
    }

    @Test
    void getByHospitalId() {
        given(patientRepository.findByHospitalId(1L)).willReturn(patientFlux);
        testClient.get().uri("/hospital/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$[0].id").isEqualTo(patient1.getId())
                .jsonPath("$[1].patientId").isEqualTo(patient2.getPatientId());
        verify(patientRepository).findByHospitalId(1L);
    }
}
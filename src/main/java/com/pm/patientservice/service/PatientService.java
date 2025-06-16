package com.pm.patientservice.service;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final   PatientRepository patientRepository;
    private final BillingServiceGrpcClient  billingServiceGrpcClient;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient) {

        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;

    }


    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();

        return patients.stream().map(
                PatientMapper::toPatientResponseDTO).toList();

    }


    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail()))
        {
            throw new EmailAlreadyExistsException("A patient with this email already exist");
        }
            Patient patient = patientRepository.save(PatientMapper.toPatient(patientRequestDTO));
            billingServiceGrpcClient.createBillingAcount(patient.getId().toString(),patient.getName(),patient.getAddress());
            return PatientMapper.toPatientResponseDTO(patient);
    }

    public PatientResponseDTO updatePatient (UUID id , PatientRequestDTO patientRequestDTO) {

        Patient patient = patientRepository.findById(id).
                orElseThrow(()-> new PatientNotFoundException("Patient not found with ID :" + id));

        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id))
        {
            throw new EmailAlreadyExistsException("A patient with this email already exist");
        }
        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
        Patient updatedPatient =  patientRepository.save(patient);
        return PatientMapper.toPatientResponseDTO(updatedPatient);
    }

    public void deletePatient (UUID id) {
        Patient patient = patientRepository.findById(id).orElseThrow(()->
                new PatientNotFoundException("Patient not found with ID :" + id));
        patientRepository.deleteById(id);

    }




}

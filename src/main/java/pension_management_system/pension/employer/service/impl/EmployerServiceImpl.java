package pension_management_system.pension.employer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.employer.dto.EmployerRequest;
import pension_management_system.pension.employer.dto.EmployerResponse;
import pension_management_system.pension.employer.entity.Employer;
import pension_management_system.pension.employer.mapper.EmployerMapper;
import pension_management_system.pension.employer.repository.EmployerRepository;
import pension_management_system.pension.employer.service.EmployerService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployerServiceImpl implements EmployerService {

    private final EmployerRepository employerRepository;
    private final EmployerMapper employerMapper;

    @Override
    @Transactional
    public EmployerResponse registerEmployer(EmployerRequest request) {
        log.info("Registering Employer request : {}", request.getCompanyName());
        if (employerRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new IllegalArgumentException("Employer already exists with registration number: " + request.getRegistrationNumber());
        }
        if (employerRepository.existsByCompanyName(request.getCompanyName())) {
            throw new IllegalArgumentException("Employer already exists with company name: " + request.getCompanyName());
        }
        Employer employer = employerMapper.toEntity(request);
        employer.setActive(true);

        Employer savedEmployer = employerRepository.save(employer);
        log.info("Employer registered successfully with ID: {}", savedEmployer.getId());

        return employerMapper.toResponse(savedEmployer);
    }

    @Override
    public EmployerResponse updateEmployer(Long id, EmployerRequest request) {
        log.info("Updating Employer request : {}", request.getCompanyName());

        Employer existingEmployer = employerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found with ID: " + id));
        // Only check for duplicate if registration number is being changed
        if (!request.getRegistrationNumber().equals(existingEmployer.getRegistrationNumber())) {
            if (employerRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
                throw new IllegalArgumentException("Employer already exists with registration number: " + request.getRegistrationNumber());
            }
        }
        employerMapper.updateEntityFromRequest(request, existingEmployer);
        Employer updatedEmployer = employerRepository.save(existingEmployer);
        log.info("Employer updated successfully with ID: {}", updatedEmployer.getId());
        return employerMapper.toResponse(updatedEmployer);
    }

    @Override
    @Transactional
    public EmployerResponse getEmployerById(Long id) {
        log.info("Getting Employer by ID: {}", id);
        Employer existingEmployer = employerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found with ID " + id));
        return employerMapper.toResponse(existingEmployer);
    }

    @Override
    @Transactional
    public EmployerResponse getEmployerByEmployerId(String employerId) {
        log.info("Getting Employer by ID: {}", employerId);
        Employer exisingEmployer = employerRepository.findByEmployerId(employerId)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found with ID " + employerId));
        return  employerMapper.toResponse(exisingEmployer);
    }

    @Override
    public List<EmployerResponse> getAllActiveEmployers() {
        log.info("Getting all employers by active");
        return employerRepository.findAll().stream()
                .filter(Employer::getActive)
                .map(employerMapper::toResponse)
                .collect(Collectors.toList());
    }

//    @Override
//    public List<EmployerResponse> getAllEmployers() {
//        log.info("Getting all employers");
//        return employerRepository.findAll().stream()
//                .map(employerMapper::toResponse)
//                .collect(Collectors.toList());
//    }

    @Override
    public void softDeleteEmployer(Long id) {
        log.info("Deleting Employer by ID: {}", id);
        Employer employer = employerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found with ID " + id));
        employer.softDelete();
        employerRepository.save(employer);
        log.info("Employer deleted successfully with ID: {}", employer.getId());


    }

    @Override
    public void deactivateEmployer(Long id) {
        log.info("Deactivating Employer by ID: {}", id);
        Employer employer = employerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found with ID " + id));
        employer.deactivate();
        employerRepository.save(employer);
        log.info("Employer deactivated successfully with ID: {}", employer.getId());

    }

    @Override
    public void reactivateEmployer(Long id) {
        log.info("Reactivating Employer by ID: {}", id);
        Employer employer = employerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found with ID " + id));
        employer.activate();
        employerRepository.save(employer);
        log.info("Employer reactivated successfully with ID: {}", employer.getId());
    }

}

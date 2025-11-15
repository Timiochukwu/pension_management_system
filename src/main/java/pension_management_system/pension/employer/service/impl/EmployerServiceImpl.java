package pension_management_system.pension.employer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.employer.dto.EmployerRequest;
import pension_management_system.pension.employer.dto.EmployerResponse;
import pension_management_system.pension.employer.entity.Employer;
import pension_management_system.pension.employer.mapper.EmployerMapper;
import pension_management_system.pension.employer.repository.EmployerRepository;
import pension_management_system.pension.employer.service.EmployerService;
import pension_management_system.pension.employer.specification.EmployerSpecification;

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
        if (request.getRegistrationNumber().equals(existingEmployer.getRegistrationNumber())) {
            throw new IllegalArgumentException("Employer already exists with registration number: " + request.getRegistrationNumber());
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
        log.info("Deleting Employer by ID: {}", id);
        Employer employer = employerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found with ID " + id));
        employer.deactivate();
        employerRepository.save(employer);
        log.info("Employer deactivated successfully with ID: {}", employer.getId());

    }

    @Override
    public void reactivateEmployer(Long id) {
        log.info("Deleting Employer by ID: {}", id);
        Employer employer = employerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found with ID " + id));
        employer.activate();
        employerRepository.save(employer);
        log.info("Employer deactivated successfully with ID: {}", employer.getId());
    }

    /**
     * ADVANCED SEARCH - Search employers with multiple filter criteria
     *
     * How this works:
     * 1. Creates a "Specification" - a dynamic query builder
     * 2. Uses EmployerSpecification to combine all filter criteria
     * 3. Passes to repository which converts to SQL WHERE clauses
     * 4. Returns paginated results
     * 5. Maps each Employer entity to EmployerResponse DTO
     *
     * Example: If you pass companyName="Tech" and city="Lagos"
     * SQL generated: SELECT * FROM employers WHERE company_name LIKE '%Tech%' AND city LIKE '%Lagos%'
     *
     * @Transactional(readOnly = true) - Opens database connection in read-only mode
     *                                    Faster because database doesn't lock tables for writing
     */
    @Override
    @Transactional(readOnly = true)
    public Page<EmployerResponse> searchEmployers(
            String employerId,
            String companyName,
            String registrationNumber,
            String email,
            String phoneNumber,
            String industry,
            Boolean active,
            String city,
            String state,
            String country,
            Pageable pageable
    ) {
        // Log the search operation for debugging/monitoring
        log.info("Searching employers with filters - companyName: {}, city: {}", companyName, city);

        // STEP 1: Build the dynamic query using Specification pattern
        // This creates SQL WHERE clauses based on which parameters are not null
        Specification<Employer> spec = EmployerSpecification.filterEmployers(
                employerId, companyName, registrationNumber, email, phoneNumber,
                industry, active, city, state, country
        );

        // STEP 2: Execute the query with pagination
        // Repository converts Specification to SQL and runs it
        Page<Employer> employers = employerRepository.findAll(spec, pageable);

        // STEP 3: Convert Entity to DTO
        // "map" transforms each Employer to EmployerResponse
        // This hides database details and sends only needed fields to client
        return employers.map(employerMapper::toResponse);
    }

    /**
     * QUICK SEARCH - Simple keyword search across multiple fields
     *
     * Perfect for search boxes where user types one keyword and you want to search
     * across company name, email, employer ID, etc.
     *
     * How it works:
     * 1. Takes single search term
     * 2. EmployerSpecification creates OR condition across multiple fields
     * 3. SQL generated: WHERE (company_name LIKE '%term%' OR email LIKE '%term%' OR ...)
     *
     * Example: searchTerm="microsoft"
     * Searches in: company name, email, employer ID, registration number, etc.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<EmployerResponse> quickSearch(String searchTerm, Pageable pageable) {
        log.info("Quick search for employers with term: {}", searchTerm);

        // Build query that searches across multiple fields
        Specification<Employer> spec = EmployerSpecification.searchEmployers(searchTerm);

        // Execute query and get paginated results
        Page<Employer> employers = employerRepository.findAll(spec, pageable);

        // Convert to DTO for response
        return employers.map(employerMapper::toResponse);
    }

}

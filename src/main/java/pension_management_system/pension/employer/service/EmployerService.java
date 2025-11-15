package pension_management_system.pension.employer.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pension_management_system.pension.employer.dto.EmployerRequest;
import pension_management_system.pension.employer.dto.EmployerResponse;

import java.util.List;

/**
 * EmployerService Interface
 *
 * Purpose: Defines contract for employer management operations
 * This interface declares methods that will be implemented in EmployerServiceImpl
 *
 * Why use interface?
 * - Loose coupling: Controllers depend on interface, not concrete implementation
 * - Easy testing with mock objects
 * - Can swap implementations without changing controllers
 */
public interface EmployerService {
    EmployerResponse registerEmployer(EmployerRequest request);
    EmployerResponse updateEmployer(Long id, EmployerRequest request);
    EmployerResponse getEmployerById(Long id);
    EmployerResponse getEmployerByEmployerId(String employerId);
    List<EmployerResponse> getAllActiveEmployers();
    void softDeleteEmployer(Long id);
    void deactivateEmployer(Long id);
    void reactivateEmployer(Long id);

    /**
     * Search and filter employers with pagination
     *
     * Allows advanced filtering by multiple criteria at once
     *
     * @param employerId Filter by employer ID (partial match)
     * @param companyName Filter by company name (partial match)
     * @param registrationNumber Filter by registration number
     * @param email Filter by email address
     * @param phoneNumber Filter by phone number
     * @param industry Filter by industry/sector
     * @param active Filter by active status (true/false)
     * @param city Filter by city
     * @param state Filter by state/province
     * @param country Filter by country
     * @param pageable Pagination settings (page number, size, sort)
     * @return Page containing matching employers
     */
    Page<EmployerResponse> searchEmployers(
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
    );

    /**
     * Quick search employers by keyword
     *
     * Searches across multiple fields (name, email, ID, etc.) at once
     * Useful for simple search boxes where you don't know which field to search
     *
     * @param searchTerm The keyword to search for
     * @param pageable Pagination settings
     * @return Page containing matching employers
     */
    Page<EmployerResponse> quickSearch(String searchTerm, Pageable pageable);
}
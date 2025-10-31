package pension_management_system.pension.employer.service;


import pension_management_system.pension.employer.dto.EmployerRequest;
import pension_management_system.pension.employer.dto.EmployerResponse;

import java.util.List;

public interface EmployerService {
    EmployerResponse registerEmployer(EmployerRequest request);
    EmployerResponse updateEmployer(Long id, EmployerRequest request);
    EmployerResponse getEmployerById(Long id);
    EmployerResponse getEmployerByEmployerId(String employerId);
    List<EmployerResponse> getAllActiveEmployers();
    void softDeleteEmployer(Long id);
    void deactivateEmployer(Long id);
    void reactivateEmployer(Long id);
}
package pension_management_system.pension.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SwaggerConfig - API Documentation Configuration
 *
 * Purpose: Auto-generate interactive API documentation
 *
 * What is Swagger/OpenAPI?
 * - Industry standard for documenting REST APIs
 * - Auto-generates documentation from code
 * - Provides interactive UI to test APIs
 * - Generates client SDKs automatically
 *
 * Benefits:
 * - No manual documentation needed
 * - Always up-to-date with code
 * - Test APIs directly from browser
 * - Share with frontend developers
 * - Generate API clients for mobile/web
 *
 * Access:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - API Docs JSON: http://localhost:8080/v3/api-docs
 *
 * Features:
 * - Try out endpoints with real requests
 * - See request/response schemas
 * - Authentication support (JWT Bearer)
 * - Export to Postman/Insomnia
 *
 * What gets documented?
 * - All @RestController endpoints
 * - Request/response DTOs
 * - HTTP methods and paths
 * - Query params, path variables
 * - Authentication requirements
 *
 * Annotations for better docs:
 * @Operation(summary = "Get member by ID")
 * @ApiResponse(responseCode = "200", description = "Success")
 * @Parameter(description = "Member ID")
 *
 * @Configuration - Spring configuration class
 */
@Configuration
public class SwaggerConfig {

    /**
     * APPLICATION METADATA
     *
     * Injected from application.properties
     * Customizable per environment
     */
    @Value("${app.name:Pension Management System}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:Enterprise Pension Management System with Payment Integration}")
    private String appDescription;

    /**
     * CONFIGURE OPENAPI
     *
     * Defines API metadata, servers, and security
     *
     * Returns OpenAPI object that Swagger UI uses
     * to render documentation
     *
     * @return Configured OpenAPI specification
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API Information
                .info(apiInfo())

                // Server URLs (development, staging, production)
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api-staging.pensionsystem.com")
                                .description("Staging Server"),
                        new Server()
                                .url("https://api.pensionsystem.com")
                                .description("Production Server")
                ))

                // Security Configuration (JWT Bearer)
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", securityScheme()))

                // Apply security globally (all endpoints require JWT)
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    /**
     * API INFORMATION
     *
     * Metadata displayed on Swagger UI homepage
     *
     * Includes:
     * - API title and version
     * - Description
     * - Contact information
     * - License details
     * - Terms of service
     *
     * @return API information object
     */
    private Info apiInfo() {
        return new Info()
                .title(appName + " API")
                .version(appVersion)
                .description(appDescription + "\n\n" +
                        "## Features\n" +
                        "- **Member Management**: Create and manage pension members\n" +
                        "- **Contributions**: Track and process contributions\n" +
                        "- **Payments**: Integrate with Paystack and Flutterwave\n" +
                        "- **Benefits**: Manage benefit claims and approvals\n" +
                        "- **Reports**: Generate PDF, Excel, and CSV reports\n" +
                        "- **Analytics**: Dashboard with real-time metrics\n\n" +
                        "## Authentication\n" +
                        "All endpoints (except /api/auth/**) require JWT Bearer token.\n\n" +
                        "**How to authenticate:**\n" +
                        "1. Call `POST /api/auth/login` with username and password\n" +
                        "2. Copy the returned JWT token\n" +
                        "3. Click 'Authorize' button above\n" +
                        "4. Enter: `Bearer <your-token>`\n" +
                        "5. Click 'Authorize'\n" +
                        "6. Now you can test all endpoints\n\n" +
                        "## Rate Limiting\n" +
                        "API is rate-limited to prevent abuse:\n" +
                        "- Public endpoints: 10 requests/minute\n" +
                        "- Authenticated endpoints: 100 requests/minute\n\n" +
                        "## Support\n" +
                        "For API support, contact: support@pensionsystem.com"
                )
                .contact(new Contact()
                        .name("Pension Management Team")
                        .email("support@pensionsystem.com")
                        .url("https://pensionsystem.com/support"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://pensionsystem.com/license"))
                .termsOfService("https://pensionsystem.com/terms");
    }

    /**
     * SECURITY SCHEME
     *
     * Defines JWT Bearer authentication
     *
     * How it works:
     * 1. User clicks "Authorize" button in Swagger UI
     * 2. Enters JWT token in format: Bearer <token>
     * 3. Swagger includes token in Authorization header
     * 4. All subsequent requests include the token
     *
     * Security Scheme Type: HTTP
     * Scheme: bearer
     * Bearer Format: JWT
     *
     * This tells Swagger:
     * - Use HTTP Authorization header
     * - Format: Authorization: Bearer <token>
     * - Token format is JWT
     *
     * @return Security scheme configuration
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Enter JWT token obtained from /api/auth/login endpoint.\n\n" +
                        "Format: Bearer <token>\n\n" +
                        "Example: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
    }
}

/**
 * ENHANCING CONTROLLERS WITH SWAGGER ANNOTATIONS
 *
 * Add these annotations to your controllers for better documentation:
 *
 * ```java
 * @RestController
 * @RequestMapping("/api/members")
 * @Tag(name = "Member Management", description = "APIs for managing pension members")
 * public class MemberController {
 *
 *     @Operation(
 *         summary = "Get member by ID",
 *         description = "Retrieves detailed information about a specific member"
 *     )
 *     @ApiResponses(value = {
 *         @ApiResponse(
 *             responseCode = "200",
 *             description = "Member found successfully",
 *             content = @Content(schema = @Schema(implementation = MemberResponse.class))
 *         ),
 *         @ApiResponse(
 *             responseCode = "404",
 *             description = "Member not found"
 *         ),
 *         @ApiResponse(
 *             responseCode = "401",
 *             description = "Unauthorized - Invalid or missing JWT token"
 *         )
 *     })
 *     @GetMapping("/{id}")
 *     public ResponseEntity<MemberResponse> getMemberById(
 *         @Parameter(description = "Member ID", example = "123")
 *         @PathVariable Long id
 *     ) {
 *         // implementation
 *     }
 * }
 * ```
 *
 * SWAGGER ANNOTATIONS GUIDE
 *
 * Controller level:
 * @Tag(name = "Category Name", description = "Category description")
 *
 * Method level:
 * @Operation(summary = "Short description", description = "Detailed description")
 * @ApiResponses(value = { ... }) - Define possible responses
 *
 * Parameter level:
 * @Parameter(description = "Param description", example = "example value")
 * @RequestBody(description = "Request body description")
 *
 * Model level (DTOs):
 * @Schema(description = "Model description")
 * On fields:
 * @Schema(description = "Field description", example = "example value")
 *
 * ACCESSING SWAGGER UI
 *
 * Development:
 * http://localhost:8080/swagger-ui.html
 * http://localhost:8080/swagger-ui/index.html
 *
 * API Docs JSON:
 * http://localhost:8080/v3/api-docs
 *
 * API Docs YAML:
 * http://localhost:8080/v3/api-docs.yaml
 *
 * CUSTOMIZING SWAGGER UI
 *
 * In application.properties:
 *
 * # Swagger UI Configuration
 * springdoc.swagger-ui.path=/api-docs
 * springdoc.swagger-ui.enabled=true
 * springdoc.swagger-ui.operationsSorter=method
 * springdoc.swagger-ui.tagsSorter=alpha
 * springdoc.swagger-ui.tryItOutEnabled=true
 * springdoc.api-docs.path=/v3/api-docs
 *
 * # Package scanning
 * springdoc.packages-to-scan=pension_management_system.pension
 * springdoc.paths-to-match=/api/**
 *
 * # Disable in production
 * springdoc.swagger-ui.enabled=${SWAGGER_ENABLED:false}
 *
 * GENERATING CLIENT SDKs
 *
 * Use OpenAPI Generator to create client libraries:
 *
 * # JavaScript/TypeScript
 * npx @openapitools/openapi-generator-cli generate \
 *   -i http://localhost:8080/v3/api-docs \
 *   -g typescript-axios \
 *   -o ./client
 *
 * # Java
 * npx @openapitools/openapi-generator-cli generate \
 *   -i http://localhost:8080/v3/api-docs \
 *   -g java \
 *   -o ./client
 *
 * # Python
 * npx @openapitools/openapi-generator-cli generate \
 *   -i http://localhost:8080/v3/api-docs \
 *   -g python \
 *   -o ./client
 *
 * TESTING APIS
 *
 * 1. Open Swagger UI
 * 2. Login to get JWT token:
 *    - POST /api/auth/login
 *    - Enter credentials
 *    - Copy token from response
 * 3. Click "Authorize" button
 * 4. Paste token: Bearer <your-token>
 * 5. Test any endpoint by clicking "Try it out"
 *
 * EXPORTING TO POSTMAN
 *
 * 1. Copy OpenAPI JSON URL: http://localhost:8080/v3/api-docs
 * 2. Open Postman
 * 3. Import → Link → Paste URL
 * 4. Postman imports all endpoints automatically
 * 5. Set up environment variable for JWT token
 *
 * PRODUCTION CONSIDERATIONS
 *
 * Security:
 * - Disable Swagger in production (set springdoc.swagger-ui.enabled=false)
 * - Or protect with authentication
 * - Don't expose internal implementation details
 *
 * Performance:
 * - Swagger scanning can slow startup
 * - Consider disabling in production
 * - Use separate docs server if needed
 *
 * Documentation Quality:
 * - Add @Operation to all endpoints
 * - Provide examples in @Schema
 * - Document all error responses
 * - Keep descriptions up-to-date
 */

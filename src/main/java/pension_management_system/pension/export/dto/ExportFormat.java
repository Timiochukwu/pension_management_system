package pension_management_system.pension.export.dto;

/**
 * ExportFormat Enum - File formats for data exports
 *
 * Purpose: Defines available formats for exporting data from the system
 *
 * What is an Export?
 * - Extract data from database
 * - Convert to file format (CSV, Excel, PDF)
 * - Allow user to download
 * - Use for reporting, analysis, backups, or data transfer
 *
 * Why support multiple formats?
 * - Different use cases need different formats
 * - Users have different tools and preferences
 * - Some formats better for specific tasks
 *
 * Use Cases by Format:
 * - CSV: Bulk imports, database migrations, simple analysis
 * - EXCEL: Complex analysis, charts, pivot tables, business users
 * - PDF: Official documents, printing, archival, non-editable format
 *
 * How it works:
 * 1. User requests export: "Export members to Excel"
 * 2. Frontend sends format: { "format": "EXCEL" }
 * 3. Backend generates file in requested format
 * 4. User downloads file
 *
 * Example API request:
 * POST /api/v1/export/members
 * {
 *   "format": "EXCEL",
 *   "filters": {...}
 * }
 */
public enum ExportFormat {

    /**
     * CSV - Comma-Separated Values
     *
     * Plain text format with comma-delimited data
     *
     * Advantages:
     * - Universal compatibility (opens in Excel, Google Sheets, any text editor)
     * - Small file size
     * - Easy to parse programmatically
     * - Fast to generate
     * - Works with database import tools
     *
     * Disadvantages:
     * - No formatting (no colors, fonts, borders)
     * - No formulas or charts
     * - Limited data types (everything is text)
     * - No multiple sheets
     *
     * File structure:
     * firstName,lastName,email,phoneNumber
     * John,Doe,john@example.com,+2348012345678
     * Jane,Smith,jane@example.com,+2348012345679
     *
     * Best for:
     * - Importing to other systems
     * - Database migrations
     * - Simple data sharing
     * - Automated processing
     * - Large datasets (millions of rows)
     *
     * MIME type: text/csv
     * File extension: .csv
     */
    CSV,

    /**
     * EXCEL - Microsoft Excel Spreadsheet
     *
     * Binary/XML format with rich formatting and features
     *
     * Advantages:
     * - Formatting (colors, fonts, borders, cell styles)
     * - Formulas and calculations
     * - Multiple sheets in one file
     * - Charts and graphs
     * - Business-friendly (most users familiar with Excel)
     *
     * Disadvantages:
     * - Larger file size
     * - Slower to generate
     * - Requires Excel or compatible software to edit
     * - More complex to parse programmatically
     *
     * File structure:
     * - Multiple worksheets
     * - Cells with data types (number, date, text)
     * - Formatting and styles
     * - Formulas: =SUM(A1:A10)
     *
     * Best for:
     * - Business reporting
     * - Data analysis with pivot tables
     * - Charts and visualizations
     * - Formatted reports for management
     * - Datasets with multiple related tables
     *
     * MIME type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
     * File extension: .xlsx
     */
    EXCEL,

    /**
     * PDF - Portable Document Format
     *
     * Fixed-layout document format
     *
     * Advantages:
     * - Professional appearance
     * - Preserves exact layout and formatting
     * - Cannot be easily edited (good for official documents)
     * - Universal viewing (any device with PDF reader)
     * - Good for printing
     *
     * Disadvantages:
     * - Cannot easily extract/edit data
     * - Not suitable for data analysis
     * - Larger file size than CSV
     * - Data is not machine-readable
     *
     * File structure:
     * - Formatted pages with text and images
     * - Tables with borders and styling
     * - Headers and footers
     * - Page numbers
     *
     * Best for:
     * - Official reports and statements
     * - Member contribution statements
     * - Archival documents
     * - Printable reports
     * - Compliance documents
     *
     * MIME type: application/pdf
     * File extension: .pdf
     */
    PDF
}

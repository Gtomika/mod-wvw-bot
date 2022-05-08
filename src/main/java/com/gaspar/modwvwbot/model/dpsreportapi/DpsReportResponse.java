package com.gaspar.modwvwbot.model.dpsreportapi;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response of getting a JSON file from dps.report API.
 * @see com.gaspar.modwvwbot.services.dpsreportapi.DpsReportService
 */
@Data
@AllArgsConstructor
public class DpsReportResponse {

    /**
     * Generated permalink of the report.
     */
    private String permalink;

    /**
     * Generated JSON as a string.
     */
    private String logJson;

}

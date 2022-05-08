package com.gaspar.modwvwbot.model.dpsreportapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * Dps.report APIs response when calling /uploadContent endpoint.
 */
@Data
@JsonIgnoreProperties
public class UploadContentResponse {

    /**
     * Identifier of the generated dps report.
     */
    private String id;

    /**
     * Permalink to this upload.
     */
    private String permalink;

    /**
     * Optional error field populated by dps.report if there was an issue.
     */
    @Nullable
    private String error;

}

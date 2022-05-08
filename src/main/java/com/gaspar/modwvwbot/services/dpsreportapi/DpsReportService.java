package com.gaspar.modwvwbot.services.dpsreportapi;

import com.gaspar.modwvwbot.exception.DpsReportApiException;
import com.gaspar.modwvwbot.model.dpsreportapi.DpsReportResponse;
import com.gaspar.modwvwbot.model.dpsreportapi.UploadContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/**
 * Communicates with dps.report API.
 */
@Service
@Slf4j
public class DpsReportService {

    private final RestTemplate restTemplate;

    public DpsReportService(@Qualifier("dpsreport") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Upload a ZEVTC or EVTC log to dps.report and make it generate a detailed
     * JSON of it.
     * @param logFile The log file to be uplaoded.
     * @return JSON as string.
     * @throws DpsReportApiException If the API fails.
     */
    public DpsReportResponse getLogJson(File logFile) throws DpsReportApiException {
        //upload, create permalink, get ready for downloading JSON
        UploadContentResponse uploadContentResponse = uploadLogFile(logFile);
        //get JSON using the id
        String getJsonUrl = "/getJson?id=" + uploadContentResponse.getId();
        var response = restTemplate.getForEntity(getJsonUrl, String.class);
        if(response.getBody() == null) {
            log.error("No response body received from dps.report");
            throw new DpsReportApiException("No response body.");
        }
        return new DpsReportResponse(uploadContentResponse.getPermalink(), response.getBody());
    }

    /**
     * Upload log file to dps.report. This generates a permalink, but the result is not the JSON file.
     * @return Response.
     * @throws DpsReportApiException If the API fails.
     */
    public UploadContentResponse uploadLogFile(File logFile) throws DpsReportApiException {
        String uploadUrl = "/uploadContent?json=1&generator=ei&detailedwvw=true";
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var formParams = new LinkedMultiValueMap<String, Object>();
        formParams.add("file", new FileSystemResource(logFile));

        var request = new HttpEntity<>(formParams, headers);

        var response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                request,
                UploadContentResponse.class
        );

        if(response.getBody() == null) {
            log.error("No response body received from dps.report");
            throw new DpsReportApiException("No response body.");
        }
        if(response.getBody().getError() != null) {
            log.error("Dps.report API responded with error: {}", response.getBody().getError());
            throw new DpsReportApiException(response.getBody().getError());
        }
        return response.getBody();
    }
}

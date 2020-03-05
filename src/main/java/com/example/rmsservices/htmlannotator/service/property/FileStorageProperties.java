package com.example.rmsservices.htmlannotator.service.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    private String uploadDir;
    private String annotateUploadDir;
    private String jsonUploadDir;
    private String csvUploadDir;
    private String newFileUploadDir;
    private String ERRORMD5_mainuploadDir;
    private String ERRORMD5_newuploadDir;

    public String getNewFileUploadDir() {
		return newFileUploadDir;
	}

	public void setNewFileUploadDir(String newFileUploadDir) {
		this.newFileUploadDir = newFileUploadDir;
	}

	public String getAnnotateUploadDir() {
        return annotateUploadDir;
    }

    public void setAnnotateUploadDir(String annotateUploadDir) {
        this.annotateUploadDir = annotateUploadDir;
    }

    public String getJsonUploadDir() {
        return jsonUploadDir;
    }

    public void setJsonUploadDir(String jsonUploadDir) {
        this.jsonUploadDir = jsonUploadDir;
    }

    public String getCsvUploadDir() {
        return csvUploadDir;
    }

    public void setCsvUploadDir(String csvUploadDir) {
        this.csvUploadDir = csvUploadDir;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

	public String getERRORMD5_mainuploadDir() {
		return ERRORMD5_mainuploadDir;
	}

	public void setERRORMD5_mainuploadDir(String eRRORMD5_mainuploadDir) {
		ERRORMD5_mainuploadDir = eRRORMD5_mainuploadDir;
	}

	public String getERRORMD5_newuploadDir() {
		return ERRORMD5_newuploadDir;
	}

	public void setERRORMD5_newuploadDir(String eRRORMD5_newuploadDir) {
		ERRORMD5_newuploadDir = eRRORMD5_newuploadDir;
	}
}

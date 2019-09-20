package com.example.rmsservices.htmlannotator.service.service;


import com.example.rmsservices.htmlannotator.service.exception.FileStorageException;
import com.example.rmsservices.htmlannotator.service.exception.MyFileNotFoundException;
import com.example.rmsservices.htmlannotator.service.mapper.DtoMapper;
import com.example.rmsservices.htmlannotator.service.pojo.AnnotationDetailsForCSV;
import com.example.rmsservices.htmlannotator.service.pojo.AnnotationDetailsFromJSON;
import com.example.rmsservices.htmlannotator.service.property.FileStorageProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileStorageService {
    
    @Autowired
    private DtoMapper dtoMapper;


    public final Path fileStorageLocation;
    public final Path annotatedFileStorageLocation;
    public final Path jsonFileStorageLocation;
    public final Path csvFileStorageLocation;

    public static final String TYPE_MAIN_FILE = "mainHTML";
    public static final String TYPE_ANNOTATED_FILE = "annotatedHTML";
    public static final String TYPE_JSON_FILE = "json";
    public static final String TYPE_CSV_FILE = "csv";
    private static final String CSV_SEPARATOR = ",";
    public static final String ANNOTATED_FILE = "annotated_";
    public static final String TYPE_CSV = ".csv";
    public static final String TYPE_JSON = ".json";
    public static final String TYPE_HTML = ".html";

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath()
                        .normalize();
        this.annotatedFileStorageLocation = Paths.get(fileStorageProperties.getAnnotateUploadDir())
                        .toAbsolutePath().normalize();
        this.jsonFileStorageLocation = Paths.get(fileStorageProperties.getJsonUploadDir())
                        .toAbsolutePath().normalize();
        this.csvFileStorageLocation = Paths.get(fileStorageProperties.getCsvUploadDir())
                        .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            Files.createDirectories(this.annotatedFileStorageLocation);
            Files.createDirectories(this.jsonFileStorageLocation);
            Files.createDirectories(this.csvFileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException(
                            "Could not create the directory where the uploaded files will be stored.",
                            ex);
        }
    }

    public String storeFile(MultipartFile file, String type, Boolean isAnnotated) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException(
                                "Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = null;
            ArrayList<String> fileNames = null;
            switch (type) {
                case TYPE_MAIN_FILE:
                    fileNames = (ArrayList<String>) getList();
                    fileName = fileNames.size() + "_" + fileName;
                    targetLocation = this.fileStorageLocation
                                    .resolve(fileName).normalize();
                    break;
                case TYPE_ANNOTATED_FILE:
                    fileNames = (ArrayList<String>) getList();
                    if(isAnnotated) {
                        File oldFile = new File(this.annotatedFileStorageLocation
                                        .resolve(fileName).normalize().toString()); 
                        
                        if(oldFile.delete()){ 
                            logger.info("File : " + this.annotatedFileStorageLocation
                                            .resolve(fileName).normalize().toString() + " deleted successfully"); 
                        }
                        if(fileName.indexOf(ANNOTATED_FILE) != -1) {
                            fileName = ANNOTATED_FILE + fileNames.size() + "_" + fileName;
                        }
                        
                        
                    } else {
                        fileName = fileNames.size() + "_" + fileName;
                    }
                    
                    targetLocation = this.annotatedFileStorageLocation
                                    .resolve(fileName).normalize();
                    break;
                case TYPE_JSON_FILE:
                    targetLocation = this.jsonFileStorageLocation.resolve(fileName);
                    break;
                case TYPE_CSV_FILE:
                    targetLocation = this.csvFileStorageLocation.resolve(fileName);
                    break;
            }
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException(
                            "Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName, String type) {
        try {
            // Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Path filePath = null;
            switch (type) {
                case TYPE_MAIN_FILE:

                    filePath = this.fileStorageLocation.resolve(fileName).normalize();
                    break;
                case TYPE_ANNOTATED_FILE:
                    filePath = this.annotatedFileStorageLocation.resolve(fileName).normalize();
                    break;
                case TYPE_JSON_FILE:
                    filePath = this.jsonFileStorageLocation.resolve(fileName).normalize();
                    break;
                case TYPE_CSV_FILE:
                    filePath = this.csvFileStorageLocation.resolve(fileName).normalize();
                    break;
                default:
                    throw new Exception("Type is incorrect!!!");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            //throw new MyFileNotFoundException("File not found " + fileName, ex);
            logger.error("File not found in loadFileAsResource at " + fileName, ex);
        } catch (Exception ex) {
            logger.error("Error occurred in loadFileAsResource at " + fileName, ex);
        }
        return null;
    }

    public List<String> getList() {
        ArrayList<String> fileNames = new ArrayList<>();

        final File folder = new File(annotatedFileStorageLocation.toString());
        for (final File fileEntry : folder.listFiles()) {
            fileNames.add(fileEntry.getName());
        }
        logger.info(fileNames.toString());
        return fileNames;
    }

    public void generateCSV(String annotatedFileName, String jsonFileName, String regExpToBeRemoved) throws Exception {
        Path annotatedFilePath = this.annotatedFileStorageLocation.resolve(annotatedFileName);
        Path jsonFilePath = this.jsonFileStorageLocation.resolve(jsonFileName);

        Stream<String> annotatedlines = Files.lines(annotatedFilePath);
        String annotatedData = annotatedlines.collect(Collectors.joining("\n"));
        annotatedlines.close();
        annotatedData = annotatedData.trim();

//        Stream<String> jsonlines = Files.lines(jsonFilePath);
//        String jsonData = jsonlines.collect(Collectors.joining("\n"));
//        jsonlines.close();
//        jsonData = jsonData.trim();
        //String csvData = convertJSONToCSV(jsonFileName);
        Map<String, AnnotationDetailsFromJSON> annotationDetails = DtoMapper.getMapFromJsonPath(jsonFilePath.toString(), new TypeReference<Map<String, AnnotationDetailsFromJSON>>() {});
        String fileName = replaceWithPattern(annotatedFileName, FileStorageService.ANNOTATED_FILE, "");
        fileName = replaceWithPattern(fileName, TYPE_HTML, "");
        updateAnnotationDetailsForCSV(annotatedData, new ArrayList<AnnotationDetailsFromJSON>(annotationDetails.values()), regExpToBeRemoved, fileName);
        

    }

    private void updateAnnotationDetailsForCSV(String annotatedData,
                    ArrayList<AnnotationDetailsFromJSON> annotationDetails, String regExpToBeRemoved, String fileName) throws Exception {
        Pattern pattern = Pattern.compile(regExpToBeRemoved);
        Matcher matcher = null;
        Integer diffStart = 0;
        Integer diffEnd = 0;
        ArrayList<AnnotationDetailsForCSV> annotationDetailsForCSVs = new ArrayList<>();
        try {
            for(AnnotationDetailsFromJSON annotationDetail: annotationDetails) {
                String expectedValue = annotationDetail.getValue();
                //String[] positions = annotationDetail.getPosition().split("-");
                Integer startIndex = annotationDetail.getStart();
                Integer endIndex = annotationDetail.getEnd();
                String actualValue = annotatedData.substring(startIndex, endIndex);
                
                if(expectedValue.compareTo(actualValue) == 0) {
                    String subAnnotatedData = annotatedData.substring(0, startIndex);
                    matcher = pattern.matcher(subAnnotatedData);
                    ArrayList<String> matches = new ArrayList<>();
                    while (matcher.find()) {
                        matches.add(matcher.group(0));
                    }
                    diffStart = String.join("", matches).length();
                    
                    matcher = pattern.matcher(expectedValue);
                    while (matcher.find()) {
                        matches.add(matcher.group(0));
                    }
                    diffEnd = String.join("", matches).length();
                    annotationDetailsForCSVs.add(dtoMapper.getAnnotationDetailsForCSV(annotationDetail, fileName, "User_1", diffStart, diffEnd));
                    
                    
                } else {
                    String msg = annotationDetail.getValue() + " is not found at : " + startIndex + "-" + endIndex + ",  and the found value is : " + actualValue;
                    throw new Exception(msg);
                    
                }
                
            }
            matcher = pattern.matcher(annotatedData);
            String mainHTML = matcher.replaceAll(regExpToBeRemoved);
            writeDataToFile(mainHTML, this.fileStorageLocation.resolve(fileName + TYPE_HTML).toString());
            writeToCSV(annotationDetailsForCSVs, this.csvFileStorageLocation.resolve(fileName + TYPE_CSV).toString());
        } catch (Exception ex) {
            
            logger.error("Error occurred in updateAnnotationDetailsForCSV.", ex);
        }
    }
    
    
    private static Boolean writeToCSV(ArrayList<AnnotationDetailsForCSV> details, String csvFileName)
    {
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFileName), "UTF-8")))
        {
            
            for (AnnotationDetailsForCSV detail : details)
            {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(detail.getDocument());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(detail.getPosition());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(detail.getValue());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(detail.getType());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(detail.getUser());
                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            //bw.close();
        } catch (Exception ex){
            logger.error("Error occurred in writeToCSV at " + csvFileName, ex);
            return false;
        }
        return true;
    }
    
    public void writeDataToFile(String data, String fileName) throws IOException {
      
      try(FileOutputStream outputStream = new FileOutputStream(fileName)){
          byte[] strToBytes = data.getBytes();
          outputStream.write(strToBytes);
         // outputStream.close();
      } catch(Exception ex) {
          logger.error("Error occurred in writeDataToFile at " + fileName, ex);
      }
      
   }

       
//    private Boolean convertJSONToCSV(String jsonFileName) throws JsonProcessingException, IOException {
//        Path jsonFilePath = this.jsonFileStorageLocation.resolve(jsonFileName);
//        Stream<String> jsonlines = Files.lines(jsonFilePath);
//        String jsonData = jsonlines.collect(Collectors.joining("\n"));
//        jsonlines.close();
//        jsonData = jsonData.trim();
//        ObjectMapper mapper = new ObjectMapper();
////        ArrayList<AnnotationDetailsForCSV> emp = mapper.readValue(jsonData, ArrayList<AnnotationDetailsForCSV>.class);
////        mapper.re
//        try{
//            JsonNode jsonTree = new ObjectMapper().readTree(new File((this.jsonFileStorageLocation.resolve(jsonFileName)).toString()));
//        
//            Builder csvSchemaBuilder = CsvSchema.builder();
//            JsonNode firstObject = jsonTree.elements().next();
//            firstObject.fieldNames().forEachRemaining(fieldName -> {csvSchemaBuilder.addColumn(fieldName);} );
//            //CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();
//            CsvMapper csvMapper = new CsvMapper();
//            CsvSchema csvSchema = csvMapper
//                            .schemaFor(AnnotationDetailsFromJSON.class)
//                            .withHeader(); 
//            String csvFileName = replaceWithPattern(jsonFileName, ".json", ".csv");
//            csvMapper.writerFor(JsonNode.class)
//              .with(csvSchema)
//              .writeValue(new File((this.csvFileStorageLocation.resolve(csvFileName)).toString()), jsonTree);
//            
//            Stream<String> csvLines = Files.lines(this.csvFileStorageLocation.resolve(csvFileName));
//            ArrayList<String> csvArrs = (ArrayList<String>) csvLines.collect(Collectors.toList());
//            for (String csvRow: csvArrs) {
//                
//                
//            }
//            return true;
//            
//        } catch(Exception exception) {
//            return false;
//        }
//    }

    public String replaceWithPattern(String str, String regExp, String replace) {
        Pattern ptn = Pattern.compile(regExp);// "\\s+");
        Matcher mtch = ptn.matcher(str);
        return mtch.replaceAll(replace);
    }
}

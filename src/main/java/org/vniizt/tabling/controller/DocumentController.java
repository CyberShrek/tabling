package org.vniizt.tabling.controller;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.vniizt.tabling.entity.DocumentParams;
import org.vniizt.tabling.service.DocumentService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * @author Alexander Ilyin
 */

@Controller
@RequestMapping("/document")
public class DocumentController {

    @Autowired
    private DocumentService service;

    private String documentName;
    private XWPFDocument document;

    @PostMapping("/build")
    public String buildDocument(DocumentParams params) throws Exception {
        documentName = params.getFileName().trim();
        documentName = (documentName.equals(""))
                ? "database catalog"
                : URLEncoder.encode(documentName, "UTF-8").replace('+', ' ');
        document = service.createDocument(params);
        return "redirect:/document/download";
    }

    @GetMapping("/download")
    public void getDocument(HttpServletResponse response) throws IOException {
        writeDocumentToResponse(response);
    }

    @GetMapping("/template/auth")
    @ResponseBody
    public String accessToTemplate() {
        return "Access is allowed";
    }

    @GetMapping("/template/download")
    public void getTemplate(HttpServletResponse response) throws IOException {
        documentName = "template";
        try {
            document = service.getTemplate();
        } catch (Exception e){
            document = new XWPFDocument();
        }
        writeDocumentToResponse(response);
    }

    @PostMapping("/template/upload")
    @ResponseBody
    public String setTemplate(@RequestParam("file") MultipartFile file) throws IOException {
        service.setTemplate(new XWPFDocument(file.getInputStream()));
        return "Template successfully updated";
    }

    private void writeDocumentToResponse(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-disposition", "attachment; filename=" + documentName + ".docx");
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        try (OutputStream stream = response.getOutputStream()){
            document.write(stream);
        }
    }
}
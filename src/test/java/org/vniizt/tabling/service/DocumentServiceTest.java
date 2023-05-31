package org.vniizt.tabling.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vniizt.tabling.entity.DocumentParams;

/**
 * @author Alexander Ilyin
 */

@SpringBootTest
class DocumentServiceTest {

    @Autowired
    DocumentService documentService;

    DocumentParams params;

//    @Test
//    public void documentExample() throws SQLException, XmlException, IOException, InvalidFormatException {
//        String [] tables = {"rawd.mq_mud", "rawd.t1_0220", "rawd.t1_common", "ngotchet.db2wg"};
//        params = new DocumentParams();
//        params.setTables(tables);
//        XWPFDocument document = documentService.createDocument(params);
//        try (FileOutputStream output = new FileOutputStream(new File("example.docx"));){
//            document.write(output);
//            output.flush();
//        } catch (IOException exception) {
//            exception.printStackTrace();
//        }
//    }
}
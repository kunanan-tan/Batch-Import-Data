package com.example.batch.data.serviceImpl;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.example.batch.data.constants.Constants;
import com.example.batch.data.entity.TestDb;
import com.example.batch.data.repository.TestDbRepository;
import com.example.batch.data.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author kunanan.t
 */
@Slf4j
@Service
@Transactional
public class TransferDataServiceImpl {

    @Value("${txt.file.in.path}")
    private String PATH_FILE;
    @Value("${txt.err.in.path}")
    private String ERR_FILE;
    @Autowired
    private static TestDbRepository testDbRepository; // example db
    int sum = 0;
    int success = 0;
    int fail = 0;

    public void transferNameSimple(){
        try {
            long queryStartTime = System.currentTimeMillis();
            deleteNameSimple();
            insertToTestDB(PATH_FILE, ERR_FILE, Constants.TXT_FILE.CHARSET_ENCODING);
            long queryEndTime = System.currentTimeMillis() - queryStartTime;
            long endTime = System.currentTimeMillis() - queryStartTime;
            this.reportSummary(" data : NameSimple", queryEndTime, endTime);
        } catch (Exception e) {
            this.fail++;
            e.getMessage();
        }
    }

    private void deleteNameSimple() {
        try {
            testDbRepository.deleteAllInBatch(); // example db
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
            log.error("Delete NameSimple error with  : ", e);
        }
    }

    private void insertToTestDB(String filePath, String errPath, String encoding) {
        Date dtd = new Date();
        Calendar c = Calendar.getInstance();
        // set date t-1 (yesterday)
        //		c.setTime(dtd);
        //		c.add(Calendar.DATE, -1);
        dtd = c.getTime();
        SimpleDateFormat ft = new SimpleDateFormat(Constants.TXT_FILE.DATE_FORMAT);
        String dt = ft.format(dtd);
        String fileName = filePath+"test_simple"+dt+ "." +Constants.TXT_FILE.FILE_EXTENSION_CSV;
        String errFileName = errPath+"test_simple"+dt+ "." +Constants.TXT_FILE.ERR_FILE_EXTENSION;

        CSVReader reader=null;
        CSVWriter writer=null;
        File errFile = new File(fileName);
        try {
            reader = new CSVReader(new FileReader(fileName), '|' , CSVWriter.NO_QUOTE_CHARACTER , 1);
            String[] tmp;
            while ((tmp = reader.readNext()) != null) {
                try {
                    TestDb entity = new TestDb();
                    if(tmp[0].length()>0){
                        entity.setOne(tmp[0]);
                    }
                    if(tmp[1].length()>0){
                        entity.setTwo(Utils.convertDateImportTxt(tmp[1]));
                    }
                    if(tmp[2].length()>0){
                        entity.setThree(Utils.getBigDecimal(tmp[2]));
                    }
                    testDbRepository.save(entity);

                    this.success++;
                    this.sum++;


                } catch (Exception e) {
                    this.fail++;
                    TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
                    log.info(e.toString());
                    log.error(e.toString(), e);

                    if (!errFile.exists()) {
                        errFile.createNewFile();
                    }
                    if (null == writer ) {
                        writer = new CSVWriter(new FileWriter(errFileName), '|',
                                CSVWriter.NO_QUOTE_CHARACTER,
                                CSVWriter.NO_ESCAPE_CHARACTER,
                                System.getProperty("line.separator"));
                    }
                    writer.writeNext(tmp);
                }
            }

        } catch (IOException e) {
            log.error("File not found");
            e.printStackTrace();
        } finally {
            try {
                if (null != reader)
                    reader.close();
                if (null != writer)
                    writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    private void reportSummary(String function, long queryEndTime, long endTime) {
        log.info("**************** Summary of find : {} ****************", function);
        log.info("          Find record with time = {}.{} Minutes ", Utils.getMinutes(queryEndTime), Utils.getSeconds(queryEndTime));
        log.info("          Total time in function = {}.{} Minutes ", Utils.getMinutes(endTime), Utils.getSeconds(endTime));
        log.info("          Total find record = {}", this.sum);
        log.info("          Success = {}", this.success);
        log.info("          fail = {}", this.fail);
        log.info("************************************************************");
        clearData();
    }


    private void clearData() {
        this.sum = 0;
        this.success = 0;
        this.fail = 0;
    }
}

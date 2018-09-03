package ru.abr.etp.controller;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import ru.abr.etp.service.DataPackageConsumer;
import ru.abr.utils.Signer;

@RestController
public class PackageRESTController {

    static final Logger logger = LogManager.getLogger(PackageRESTController.class);

    @Autowired
    DataPackageConsumer dataPackageConsumer;

    @RequestMapping( value = "/callback", method = RequestMethod.POST,
    produces = MediaType.TEXT_XML_VALUE,
    consumes = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> producePackage( @RequestBody String data){

        //System.out.println( data);
        StringBuffer faultString = new StringBuffer();
        HttpStatus httpStatus = dataPackageConsumer.process( data, faultString);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/xml; charset=UTF-8");
        if( httpStatus != HttpStatus.OK ) {
            logger.info( "Отправляем сообщение об ошибке " + faultString.toString());
            return new ResponseEntity<String>(faultString.toString(), headers,  HttpStatus.INTERNAL_SERVER_ERROR);
        } ;

        return new ResponseEntity( headers, HttpStatus.OK);
    }

    @RequestMapping( value = "/ping", method = RequestMethod.GET)
    @ResponseBody
    public String ping(){
        return "hello";
    }



}

package ru.abr.etp.service;

import org.springframework.http.HttpStatus;

public interface DataPackageConsumer {
    HttpStatus process(String data, StringBuffer faultString);
}

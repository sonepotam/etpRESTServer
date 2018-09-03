package ru.abr.etpClient;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import ru.abr.etp.controller.PackageRESTController;
import ru.abr.utils.EtpHolder;
import ru.abr.utils.InputDirectoryScanner;
import ru.abr.utils.Signer;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;


@ComponentScan( "ru.abr.etp.controller")
@ComponentScan( "ru.abr.etp.service")
@SpringBootApplication
public class etpStarter  {

    static final Logger logger = LogManager.getLogger(PackageRESTController.class);

    public static void main(String[] args)  {
        SpringApplication.run( etpStarter.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            logger.info( "Загружаем настройки");
            try {
                EtpHolder.getInstance();
                logger.info( "Сертификаты загружены");
            }
            catch ( Exception e){
                e.printStackTrace();
            }
            try {
                Signer.getInstance();
                logger.info( "signer создан");
            } catch ( Exception e){
                e.printStackTrace();
            }
            InputDirectoryScanner scanner = InputDirectoryScanner.getInstance();
            scanner.start();
            logger.info( "Сканер каталогов запущен");

        };
    }


 //   @Override
 //   protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
 //       logger.info( "Started in WEB environment");
  //      return application.sources( Application.class);
 //   }


}

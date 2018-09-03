package ru.abr.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.abr.etp.model.VO.Etp;
import ru.abr.etp.service.DataPackageConsumerImpl;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

public class EtpHolder {

    static final Logger logger = LogManager.getLogger(EtpHolder.class);

    private static EtpHolder etpHolder = null;

   private Hashtable<String, Etp> certs = null;

    private EtpHolder(){}

    public static Etp get( String etpName){
        EtpHolder holder = getInstance();
        return holder.certs.get( etpName);
    }

    public static synchronized EtpHolder getInstance(){
        if ( etpHolder == null) {
            etpHolder = new EtpHolder();
            logger.info( "Загружаем сертификаты и открытые ключи из файлов");
            if( etpHolder.certs == null){
                Properties properties = new Properties();
                InputStream is = DataPackageConsumerImpl.class.getClassLoader().getResourceAsStream("config.properties");
                try {
                    properties.load(is);
                }
                catch ( Exception e){
                    logger.error( "Не удалось загрузить настройки ", e);
                    System.exit(1);
                }

                etpHolder.certs = new Hashtable<>();
                Enumeration e = properties.propertyNames();
                while ( e.hasMoreElements()){
                    String elem = (String) e.nextElement();
                    String etpName = "";
                    if( elem.endsWith( ".thumbPrint") || elem.endsWith( ".url") || elem.endsWith( ".skipCrypto") || elem.endsWith( ".send")){
                        String value = properties.getProperty( elem);
                        etpName = elem.substring( 0, elem.indexOf( "."));
                        logger.info( etpName + "  -> " + elem +"=" + value);
                        Etp etp = etpHolder.certs.get( etpName);
                        if( etp == null){
                            etp = new Etp();
                            etpHolder.certs.put( etpName, etp);
                        }

                        if( elem.endsWith( ".thumbPrint")){ etp.setThumbPrint( value);  }
                        if( elem.endsWith( ".url")       ){ etp.setUrl( value);  }
                        if( elem.endsWith( ".skipCrypto")){ etp.setSkipCrypto( value);  }
                        if( elem.endsWith( ".send")      ){ etp.setSend( value);  }
                    }
                    }
                }
            }
        return etpHolder;
    }

}

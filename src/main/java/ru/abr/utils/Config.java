package ru.abr.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

public class Config {

    static final Logger logger = LogManager.getLogger(Config.class);


    private static Config config = null;

    private String to_ETP_Path;
    private String fileMask ;
    private String from_ETP_Path;
    private String tempPath;

    private int    delay;

    private String alias;
    private String password;


    private String xsdSkipValidation;
    private String xsdDocument;
    private String xsdProc;
    private String xsdFault;

    private String Bank_ID;

    public Config() { }


    public static Config getConfig() {return config;        }
    public String getTo_ETP_Path()   {return to_ETP_Path;   }
    public String getFileMask()      {return fileMask;      }
    public String getFrom_ETP_Path() {return from_ETP_Path; }
    public String getTempPath()      {return tempPath;      }
    public int getDelay()            {return delay;         }
    public String getAlias()         {return alias;         }
    public String getPassword()      {return password;      }


    public String getXsdSkipValidation() { return xsdSkipValidation; }
    public String getXsdDocument()       { return xsdDocument;       }
    public String getXsdProc()           { return xsdProc;           }
    public String getXsdFault()          { return xsdFault;          }
    public String getBank_ID()           { return Bank_ID;           }

    public static synchronized Config getInstance() {
        if (config == null) {
            try {

                config = new Config();
                InputStream is = InputDirectoryScanner.class.getClassLoader().getResourceAsStream("config.properties");
                Properties properties = new Properties();
                properties.load(is);

                config.to_ETP_Path   = properties.getProperty( "directory.TO_ETP_DIR");
                config.fileMask      = properties.getProperty( "directory.TO_ETP_FILE_MASK");
                config.from_ETP_Path = properties.getProperty( "directory.FROM_ETP_DIR");
                config.tempPath      = properties.getProperty( "directory.TEMP_DIR");
                config.delay         = Integer.parseInt( properties.getProperty( "directory.scan.timeout")) * 1000;
                config.alias         = properties.getProperty( "crypto.key.alias");
                config.password      = properties.getProperty( "crypto.key.password");

                config.xsdSkipValidation = properties.getProperty( "xsd.skipValidation");
                config.xsdDocument       = properties.getProperty( "xsd.document");
                config.xsdProc           = properties.getProperty( "xsd.proc");
                config.xsdFault          = properties.getProperty( "xsd.fault");
                config.Bank_ID           = properties.getProperty("BANK_ID");

            }
            catch ( Exception e){
                logger.error( "Не удалось прочитать настройки", e );
                System.exit(1);
            }
        }
        return config;
    }

}

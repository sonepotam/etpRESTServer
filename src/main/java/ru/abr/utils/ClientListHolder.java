package ru.abr.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import ru.abr.etp.model.VO.*;
import ru.abr.etp.model.docs.FundsHoldRs;
import ru.abr.etp.model.docs.Status;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import static java.lang.Math.abs;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static ru.abr.utils.utils.getTagValue;
import static ru.abr.utils.utils.readFile;

public class ClientListHolder {

    static final Logger logger = LogManager.getLogger(ClientListHolder.class);


     private EtpClientList etpClientList = null;

    static private ClientListHolder holder = null;

    private ClientListHolder(){}


    public synchronized static ClientListHolder getInstance() {
        if (holder == null) {
            holder = new ClientListHolder();
            holder.etpClientList = new EtpClientList();
        }
        return holder;
    }


    public void readClients( String fileName){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EtpClientList.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String str = utils.readFile(fileName, "UTF-8");
            StringReader reader = new StringReader(str);
            etpClientList = (EtpClientList) unmarshaller.unmarshal(reader);
        }
        catch ( Exception e){
            logger.error( "Ошибка чтения файла клиентов", e);
        }

    }


    public void clearClients(){
        etpClientList = new EtpClientList();
    }


    public synchronized void processAbsFiles( String to_ETP_Path){
        File clientList = new File( to_ETP_Path + "\\bank_CLIENT_LIST.xml");
        File absStarted = new File( to_ETP_Path + "\\absstarted.xml");
        ClientListHolder holder = ClientListHolder.getInstance();
        if( clientList.exists()) {
            holder.readClients(clientList.getAbsolutePath());
            //clientList.delete();
        }
        if( absStarted.exists()){
            holder.clearClients();
            //absStarted.delete();
        }

    }


    //
    // метод для обработки пакетов.
    // false - сохранить полученный запрос в файл
    // true  - метод сам сохранил запрос в файл. дополнительных действий не требуется
    //
    public synchronized  ArrayList<FundsHoldRs> processReceivedPackage( String typeDocument, Document doc){
        ArrayList<FundsHoldRs> list = new ArrayList<>();

        logger.info( "Обрабатываем запрос " + typeDocument);

        if( etpClientList.getEtpClientList().isEmpty()) {
            return list;
        }
        logger.info( "В списке клиентов есть записи " + etpClientList.getEtpClientList().size());


        int summa   = 0;
        switch ( typeDocument){
            case "FundsHoldRq":
                    summa   = - Integer.parseInt(  getTagValue( doc, "AmountToHoldx100"));
                    break;
            case "FundsReleaseRq":
                    summa   = + Integer.parseInt(  getTagValue( doc, "AmountToReleasex100"));
                    break;
            case "OperatorFeeRq":
                    summa   = - Integer.parseInt(  getTagValue( doc, "AmountToTransferx100"));
                    break;
            default:
                return list;
        }


        String fileName= "";
        String Name         = utils.getTagValue( doc, "Name");
        String INN          = utils.getTagValue( doc, "INN");
        String KPP          = utils.getTagValue( doc, "KPP");
        String Account      = utils.getTagValue( doc, "Account");
        String OperatorName = utils.getTagValue( doc, "OperatorName");
        String AppID        = utils.getTagValue( doc, "AppID");

        EtpClient client2Search = new EtpClient( Name, INN, KPP, Account, 0);
        String msgID   = utils.getTagValue( doc, "MsgID");
        String msgTm   = utils.getTagValue( doc, "MsgTm");

        logger.info( "В запросе содержится сумма " + abs(summa));

        //
        // ищем в списке пришедший запрос
        //
        int pos = etpClientList.getEtpClientList().indexOf( client2Search);
        //
        // пришедщий клиент есть в списке
        //
        Status status = null;
        if( pos >= 0){
            EtpClient etpClient = etpClientList.getEtpClientList().get( pos);
            PackageVO packageVO = new PackageVO( msgID, msgTm, typeDocument, fileName, summa);
            //
            // ищем у клиента такой запрос. если он есть - пропускаем, если нет добавляем
            //
            int p = etpClient.getPackages().indexOf( packageVO);
            if( p == -1){
                logger.info( "Запрос msgID=" + msgID + " на сумму " + summa + " еще не обрабатывался");
                //
                // вычисляем остаток на основе ранее переданных пакетов
                //
                int saldo = etpClient.getActualSaldo();
                logger.info( "Сальдо клиента [" + etpClient.getName() + "/ИНН=" + etpClient.getInn() + "]= " + saldo);
                //
                // остаток можно обработать
                //
                if( (saldo + summa) > 0){
                    //
                    // гененрируем положительный ответ
                    //
                    status = new Status( 0,"");
                } else {
                    //
                    // генерируем отрицательный ответ
                    //
                    status = new Status( 2,"Запрос не выполнен, недостаточно средств на счете");
                }

            }
        else{
            //
            // генерируем отрицательный ответ
            //
            status = new Status( 2, "Запрос не выполнен, не найден клиент с указанными реквизитами");
            }
        }
        if( status != null){
            Config config = Config.getInstance();
            String r_msgID = UUID.randomUUID().toString().replace("-", "");
            String r_msgTm = ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now());
            String r_correlationID = msgID;
            String r_operatorName  = OperatorName;
            String r_appID         = AppID;
            String r_bankID        = config.getBank_ID();
            //
            // делаем квитанцию
            //
            FundsHoldRs fundsHoldRsAck = new FundsHoldRs(
                    r_msgID,
                    r_msgTm,
                    r_correlationID,
                    r_operatorName,
                    r_appID,
                    r_bankID,
                    status);
            list.add( fundsHoldRsAck);
            //
            // ждем 1 секунду
            //
            try {
                Thread.sleep(1 * 1000);
            }
            catch ( InterruptedException e){
                logger.error( e);
            }
            //
            // делаем бизнес-ответ
            //
            r_msgID = UUID.randomUUID().toString().replace("-", "");
            r_msgTm = ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now());
            FundsHoldRs fundsHoldRsBus = new FundsHoldRs(
                    r_msgID,
                    r_msgTm,
                    r_correlationID,
                    r_operatorName,
                    r_appID,
                    r_bankID,
                    status);
            list.add( fundsHoldRsBus);


        }


        return list;
    }
}

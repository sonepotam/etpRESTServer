package ru.abr.utils;

import CMS_samples.CMSVerify;
import CMS_samples.CMStools;
import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;
import com.objsys.asn1j.runtime.Asn1BerEncodeBuffer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.ContentInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.DigestAlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignedData;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignerInfo;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.tools.Array;
import ru.abr.etp.service.DataPackageConsumerImpl;

import javax.xml.bind.DatatypeConverter;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.UUID;

import static CMS_samples.CMSSign.createHashCMSEx;
import static CMS_samples.CMSSign.urlEncoding;
import static CMS_samples.CMSVerify.verifyOnCert;


/*
Перевод на ГОСТ 24.10-2012 https://habr.com/company/alfa/blog/341476/
проверка подписи на сайте госуслуг https://www.gosuslugi.ru/pgu/eds/
 */

public  class Signer {

    static final Logger logger = LogManager.getLogger(Signer.class);

    final PrivateKey[] keys = new PrivateKey[1];
    final Certificate[] certs = new Certificate[1];


    private static Signer signer = null;
    private Config config = null;

    private Signer(){ }

    public static synchronized Signer getInstance()throws Exception{
        if ( signer == null){
            signer = new Signer();
            signer.config = Config.getInstance();

            logger.info("Загружаем ключ и сертификат из контейнера " + signer.config.getAlias());
            try {
                signer.keys[0] = CMStools.loadKey(signer.config.getAlias(), signer.config.getPassword().toCharArray());
                signer.certs[0] = CMStools.loadCertificate(signer.config.getAlias());
            } catch(Exception e){
                logger.info( "Ошибка при загрузке ключа или сертификата\n" + e.toString());
            }
            logger.info("Ключ и сертификат загружены");

        }
        return signer;
    }




    public String checkSign( String buffer, byte[] signature, String thumpPrint) throws Exception{
        byte[] data = buffer.getBytes( "UTF-8");

        //CMSVerify.CMSVerifyEx(signature, certs, data, CMStools.DIGEST_OID_2012_512,
        //        CMStools.DIGEST_ALG_NAME_2012_512, JCP.GOST_SIGN_DH_2012_512_NAME,
        //        JCP.PROVIDER_NAME);
        //
        // код создан на основе
        //
        String result =
                customCMSVerifyEx(signature, thumpPrint, data, CMStools.DIGEST_OID_2012_512,
                CMStools.DIGEST_ALG_NAME_2012_512, JCP.GOST_SIGN_DH_2012_512_NAME,
                JCP.PROVIDER_NAME);

        return result;
    }

    //
    // в отличие от CMSVerifyEx проверяется и сертификат подписи и сертификат из файла,
    // кроме того функция возвращает строку с ошибкой или OK а не генерирует ошибки
    //
    public String customCMSVerifyEx(byte[] buffer, String thumpPrint,
                                   byte[] data, String digestOidValue, String digestAlg,
                                   String signAlg, String providerName) throws Exception {

        String result = "OK";

        final Asn1BerDecodeBuffer asnBuf = new Asn1BerDecodeBuffer(buffer);
        final ContentInfo all = new ContentInfo();
        all.decode(asnBuf);

        if (!new OID(CMStools.STR_CMS_OID_SIGNED).eq(all.contentType.value)) {
            return "OID " + CMStools.STR_CMS_OID_SIGNED + " не поддерживается";
        };
        if( data == null){
            return "Нет содержимого для проверки подписи";
        };


        final SignedData cms = (SignedData) all.content;
        final byte[] text;

        if (cms.encapContentInfo.eContent != null) {
            text = cms.encapContentInfo.eContent.value;
        } // if
        else {
            text = data;
        } // else

        logger.info("Source data: " + new String(text));

        OID digestOid = null;
        final DigestAlgorithmIdentifier digestAlgorithmIdentifier =
                new DigestAlgorithmIdentifier(new OID(digestOidValue).value);

        for (int i = 0; i < cms.digestAlgorithms.elements.length; i++) {
            if (cms.digestAlgorithms.elements[i].algorithm
                    .equals(digestAlgorithmIdentifier.algorithm)) {
                digestOid = new OID(cms.digestAlgorithms.elements[i].algorithm.value);
                break;
            } // if
        } // for

        if (digestOid == null) {
            return  "Неизвестный OID подписи";
        } // if

        final OID eContTypeOID = new OID(cms.encapContentInfo.eContentType.value);
        //


        //Проверка на вложенных сертификатах
        if (cms.certificates != null) {
           logger.info("Начинаем проверку сертификатов CMS.");

           for (int i = 0; i < cms.certificates.elements.length; i++) {

                final Asn1BerEncodeBuffer encBuf = new Asn1BerEncodeBuffer();
                cms.certificates.elements[i].encode(encBuf);

                final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                final X509Certificate cert = (X509Certificate) cf
                        .generateCertificate(encBuf.getInputStream());

                for (int j = 0; j < cms.signerInfos.elements.length; j++) {

                    final SignerInfo info = cms.signerInfos.elements[j];
                    if (!digestOid.equals(new OID(info.digestAlgorithm.algorithm.value))) {
                        throw new Exception("Not signed on certificate.");
                    } // if

                    final boolean checkResult = verifyOnCert(cert,
                            cms.signerInfos.elements[j], text, eContTypeOID,
                            true, digestAlg, signAlg, providerName);

                    if( checkResult) {
                        logger.info("sign[" + j + "] - Корректная подпись сертификата [" + i + "] ( " + cert.getIssuerX500Principal() + ")");
                    } else {
                        return "sign[" + j + "] - Некорректная подпись сертификата [" + i + "] ( " + cert.getIssuerX500Principal() + ")";
                    }
                } // for

            } // for

        } // if
        //
        // переходим к проверке внешнего сертификата
        //
        logger.info("Начинаем проверку отпечатка сертификата");
        boolean isOK = false;
        for (int i = 0; i < certs.length; i++) {
            final X509Certificate cert = (X509Certificate) certs[i];
            String print = getThumbprint( cert);
            if( print.equals( thumpPrint)){
                isOK = true;
            }
        } // else
        if( !isOK){
            logger.info( "В списке сертификатов подписи не удалось найти сертификат с отпечатком " + thumpPrint);
            return "В списке сертификатов подписи не удалось найти сертификат с отпечатком " + thumpPrint;
        }
        return "OK";
    }


    public String sign( String buffer) throws Exception{
        logger.info( "Начинаем подпись данных");
        final byte[] data = buffer.getBytes( "UTF-8");

        final Asn1BerDecodeBuffer asnBuf = new Asn1BerDecodeBuffer(data);
        final ContentInfo all = new ContentInfo();

        String tempPath      = config.getTempPath();
        String randomFile    = UUID.randomUUID().toString();
        String cadesFileName = tempPath + "\\" +  randomFile+ ".cades";
        String savedBuffer   = tempPath + "\\" +  randomFile+ ".buffer";
        logger.info( "Создан файл для подписи " + cadesFileName);

        Array.writeFile( savedBuffer, buffer.getBytes( "UTF-8") );

        try {
            all.decode(asnBuf);
        } catch (Exception e) {

            // Создаем CAdES-BES подпись, имея хеш сообщения.
            logger.info( "создаем подпись");
            createHashCMSEx(data
                    ,false
                    ,keys
                    , certs
                    , cadesFileName
                    ,true
                    , true
                    , JCP.GOST_DIGEST_2012_512_OID   // CMStools.DIGEST_OID
                    , JCP.GOST_PARAMS_SIG_2012_512_KEY_OID     // CMStools.SIGN_OID
                    , CMStools.DIGEST_ALG_NAME_2012_512 //  CMStools.DIGEST_ALG_NAME_2012_512
                    , JCP.GOST_SIGN_DH_2012_512_NAME // JCP.GOST_EL_SIGN_NAME          // это не понятно
                    , JCP.PROVIDER_NAME);
        }
        logger.info( "Читаем файл с подписью " + cadesFileName);
        byte[] signdata = Array.readFile(cadesFileName);
        logger.info( "Проверяем созданную подпись");
        CMSVerify.CMSVerifyEx(signdata, certs, data, CMStools.DIGEST_OID_2012_512,
                CMStools.DIGEST_ALG_NAME_2012_512, JCP.GOST_SIGN_DH_2012_512_NAME,
                JCP.PROVIDER_NAME);

        String strSignature = Base64.getEncoder().encodeToString( signdata);
        strSignature = urlEncoding( strSignature, false);
        logger.info( "Сформирована подпись:\n" + strSignature);
        signdata   = strSignature.getBytes( "UTF-8");
        Array.writeFile( cadesFileName + ".sig", signdata);
        logger.info( "Подпись успешно записа в файл : " + cadesFileName + ".sig");

        return strSignature;
    }



    public static String getThumbprint(X509Certificate cert)
            throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        String digestHex = DatatypeConverter.printHexBinary(digest);
        return digestHex.toLowerCase();
    }


}

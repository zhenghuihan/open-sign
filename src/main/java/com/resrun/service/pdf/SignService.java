package com.resrun.service.pdf;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import com.resrun.service.pojo.CertificateProperty;
import com.resrun.service.pojo.RealPositionProperty;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

/**
 * @Description: 签署业务
 * @Package: com.resrun.service.pdf
 * @ClassName: SignService
 * @copyright 北京资源律动科技有限公司
 */
@Service
public class SignService {


    public byte[] signingContract(byte[] pdfFile, byte[] signBadge, CertificateProperty cert,
                                  RealPositionProperty position) throws GeneralSecurityException, IOException, DocumentException {
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory","com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        Security.addProvider(new BouncyCastleProvider());
        //1、解析证书
        // Java 安全属性文件中指定的默认 keystore 类型；如果不存在此类属性，则返回字符串 "jks"。 PKCS12
        KeyStore ks = KeyStore.getInstance(cert.getCertType());
        try {
            char[] chars = cert.getPassword().toCharArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cert.getCertFile());
            ks.load(byteArrayInputStream, chars);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 获取keystore中的所有别名
        String alias = (String) ks.aliases().nextElement();
        // 返回：请求的密钥， 入力参数：别名，用于恢复密钥的密码
        PrivateKey pk = (PrivateKey) ks.getKey(alias, cert.getPassword().toCharArray());
        // 证书链（按用户证书在前，根证书授权在后的顺序）
        Certificate[] chain = ks.getCertificateChain(alias);

        byte[] signedFileByte = null ;
        PdfReader reader = null ;
        ByteArrayOutputStream signedFile = null ;
        PdfStamper stamper = null ;
        try {
            //2、读取PDF文件
            reader = new PdfReader(pdfFile);
            signedFile = new ByteArrayOutputStream();
            stamper = PdfStamper.createSignature(reader, signedFile, '\0', null, true);
            //3、给签署属性服务
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            if (signBadge == null || position == null) {
                appearance.setCertificationLevel(certificationLevel);
            } else {
                int pageNum = 0;
                if (inspect) {
                    //如果检查就会抛出检查异常
                    pageNum = position.getPageNum();
                    if (pageNum == 0)
                        throw new IllegalArgumentException("Pdf page number must be greater than one....!!!");
                } else {
                    pageNum = position.getPageNum() <= 0 ? 1 : position.getPageNum();
                }
                appearance.setVisibleSignature(new Rectangle(position.getStartx(), position.getStarty(), position.getEndx(), position.getEndy()), pageNum, null);
                // 添加签章图片
                Image img = Image.getInstance(signBadge);
                appearance.setSignatureGraphic(img);
                appearance.setImageScale(-1);
                appearance.setCertificationLevel(certificationLevel);
                appearance.setRenderingMode(renderingMode);
            }
            appearance.setReason(reason);
            appearance.setLocation(location);
            //4、调用签署  Creating the signature
            ExternalSignature pks = new PrivateKeySignature(pk, hashAlgorithm, BouncyCastleProvider.PROVIDER_NAME);
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, ocspClient, tsaClient, 0, cryptoStandard);
            signedFileByte = signedFile.toByteArray();
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭流
            if (stamper != null) stamper.close();
            if (signedFile != null) signedFile.close();
            if (reader != null) reader.close();
        }
        return signedFileByte ;
    }



    //是否判断校验不校验PDF页码
    private boolean inspect = true;

    private int certificationLevel = PdfSignatureAppearance.NOT_CERTIFIED;

    private PdfSignatureAppearance.RenderingMode renderingMode = PdfSignatureAppearance.RenderingMode.GRAPHIC;

    private String hashAlgorithm = DigestAlgorithms.SHA256;

    private MakeSignature.CryptoStandard cryptoStandard = MakeSignature.CryptoStandard.CMS;

    private String reason = "防伪造防篡改数字校验"; //原因

    private String location; //位置

    private TSAClient tsaClient; //时间戳服务

    private OcspClient ocspClient;




    public boolean isInspect() {
        return inspect;
    }

    public void setInspect(boolean inspect) {
        this.inspect = inspect;
    }

    public int getCertificationLevel() {
        return certificationLevel;
    }

    public void setCertificationLevel(int certificationLevel) {
        this.certificationLevel = certificationLevel;
    }

    public PdfSignatureAppearance.RenderingMode getRenderingMode() {
        return renderingMode;
    }

    public void setRenderingMode(PdfSignatureAppearance.RenderingMode renderingMode) {
        this.renderingMode = renderingMode;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public MakeSignature.CryptoStandard getCryptoStandard() {
        return cryptoStandard;
    }

    public void setCryptoStandard(MakeSignature.CryptoStandard cryptoStandard) {
        this.cryptoStandard = cryptoStandard;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public TSAClient getTsaClient() {
        return tsaClient;
    }

    public void setTsaClient(TSAClient tsaClient) {
        this.tsaClient = tsaClient;
    }

    public OcspClient getOcspClient() {
        return ocspClient;
    }

    public void setOcspClient(OcspClient ocspClient) {
        this.ocspClient = ocspClient;
    }




}
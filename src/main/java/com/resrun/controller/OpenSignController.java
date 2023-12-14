package com.resrun.controller;

import com.resrun.enums.SignTypeEnum;
import com.resrun.service.pojo.CertificateProperty;
import com.resrun.service.pojo.GenerateCertificateInfo;
import com.resrun.service.pojo.RealPositionProperty;
import com.resrun.service.pojo.SourcePositionProperty;
import com.resrun.service.cert.CertService;
import com.resrun.service.image.EntSealClipService;
import com.resrun.service.image.EntSealGenerateService;
import com.resrun.service.pdf.CalculatePositionService;
import com.resrun.service.pdf.SignService;
import com.resrun.service.verify.SignVerifyService;
import com.resrun.utils.Base64;
import com.resrun.controller.vo.base.Result;
import com.resrun.controller.vo.request.*;
import com.resrun.controller.vo.response.SealResponse;
import com.resrun.controller.vo.response.SignResponse;
import com.resrun.controller.vo.response.VerifyResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: OpenSignController
 * @Package: com.resrun.controller
 * @ClassName: OpenSignController
 * @copyright 北京资源律动科技有限公司
 */
@Api(tags = "开放签-演示demo")
@RestController
public class OpenSignController {


    @Autowired
    private SignVerifyService signVerifyService ;
    @Autowired
    private CalculatePositionService calculatePositionService ;
    @Autowired
    private SignService signService ;
    @Autowired
    private EntSealGenerateService entSealGenerateService ;
    @Autowired
    private EntSealClipService entSealClipService ;
    @Autowired
    private CertService certService ;

    @ApiOperation("生成企业签章-上传生成")
    @RequestMapping(value = "/clip/seal",method = RequestMethod.POST)
    public Result<SealResponse> generateUpload(@RequestBody ClipSealRequest request){


        if(request.getImage() == null || request.getImage().length() == 0){
            return Result.error("图片数据为空",null);
        }
        byte[] decode = Base64.decode(request.getImage());
        if(decode == null || decode.length == 0){
            return Result.error("签章制作失败",null);
        }

        byte[] entSealByte = entSealClipService.clip(decode, request.getColorRange());
        if(entSealByte == null){
            return Result.error("签章制作失败",null);
        }
        String encode = Base64.encode(entSealByte);
        SealResponse response = new SealResponse();
        response.setEntSeal(encode);
        return Result.OK(response) ;

    }

    @ApiOperation("生成企业签章-参数生成")
    @RequestMapping(value = "/generate/seal",method = RequestMethod.POST)
    public Result<SealResponse> seal(@RequestBody GenerateSealRequest request){


        if(request == null || request.getMiddleText() == null || request.getTopText() == null){
            return Result.error("参数缺失",null) ;
        }
        byte[] entSealByte = entSealGenerateService.generateEntSeal(request.getTopText(), request.getMiddleText());
        if(entSealByte == null){
            return Result.error("签章制作失败",null);
        }
        String encode = Base64.encode(entSealByte);
        SealResponse response = new SealResponse();
        response.setEntSeal(encode);
        return Result.OK(response) ;

    }

    @ApiOperation("签署")
    @RequestMapping(value = "/sign",method = RequestMethod.POST)
    public Result<SignResponse> sign(@RequestBody SignRequest request){

        String fileName = "开源工具版说明.pdf" ;
        byte[] signFileBytes = null ;
        byte[] entSealBytes = null ;
        byte[] personalBytes = null ;
        CertificateProperty entCert = null ;
        CertificateProperty personalCert = null ;
        List<RealPositionProperty> entPositionList = null;
        List<RealPositionProperty> personalPositionList = null;
        int entSealWidth = 200 ;
        int entSealHeight = 200 ;
        int personalSealWidth = 150 ;
        int personalSealHeight = 70 ;
        //获取本地签署文件
        try {
            signFileBytes = getResourceFiles(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(signFileBytes == null){
            return Result.error("签署失败",null);
        }
        //生成企业证书和个人证书
        try {
            if(request.getEntName() != null && request.getEntName().length() > 0){
                String subject = "C=CN,ST=北京,L=北京,O=开放签 CA,OU=产品部,CN=开放签@" + request.getEntName();
                GenerateCertificateInfo generateCertificateInfo = certService.generateCertificate(null, subject, 10);
                if(generateCertificateInfo != null){
                    entCert = new CertificateProperty();
                    entCert.setCertType("PKCS12");
                    entCert.setCertFile(generateCertificateInfo.getPfx());
                    entCert.setPassword(generateCertificateInfo.getPassword());
                }
                if(entCert == null){
                    return Result.error("签署失败",null);
                }
            }
            if(request.getPersonalName() != null && request.getPersonalName().length() > 0){
                String subject = "C=CN,ST=北京,L=北京,O=开放签 CA,OU=产品部,CN=开放签@" + request.getPersonalName();
                GenerateCertificateInfo generateCertificateInfo = certService.generateCertificate(null, subject, 10);
                if(generateCertificateInfo != null){
                    personalCert = new CertificateProperty();
                    personalCert.setCertType("PKCS12");
                    personalCert.setCertFile(generateCertificateInfo.getPfx());
                    personalCert.setPassword(generateCertificateInfo.getPassword());
                }
                if(personalCert == null){
                    return Result.error("签署失败",null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //生成企业签章和个人签章
        if(request.getEntSeal() != null){
            entSealBytes = Base64.decode(request.getEntSeal());
        }
        if(request.getPersonalSeal() != null){
            personalBytes = Base64.decode(request.getPersonalSeal());
        }

        //计算企业签署位置和个人签署位置
        if(SignTypeEnum.POSITION.getCode().equals(request.getSignType())){
            if((request.getEntPositionList() == null || request.getEntPositionList().size() == 0 ) &&
                    (request.getPersonalPositionList() == null || request.getPersonalPositionList().size() == 0)){
                return Result.error("签署失败",null);
            }
            //计算企业签署位置
            if(request.getEntPositionList() != null && request.getEntPositionList().size() > 0){
                List<SourcePositionProperty> convert = convert(request.getEntPositionList());
                entPositionList = calculatePositionService.calculatePositions(convert, signFileBytes);
            }
            //计算个人签署位置
            if(request.getPersonalPositionList() != null && request.getPersonalPositionList().size() > 0){
                List<SourcePositionProperty> convert = convert(request.getPersonalPositionList());
                personalPositionList = calculatePositionService.calculatePositions(convert, signFileBytes);
            }
        }else if(SignTypeEnum.KEYWORD.getCode().equals(request.getSignType())){
            if((request.getEntKeyword() == null || request.getEntKeyword().length() == 0 ) &&
                    (request.getPersonalKeyword() == null || request.getPersonalKeyword().length() == 0)){
                return Result.error("签署失败",null);
            }
            //根据关键字计算所有企业签署位置
            if(request.getEntKeyword() != null && request.getEntKeyword().length() > 0){
                entPositionList = calculatePositionService.getAllPositionByKeyWords(signFileBytes, request.getEntKeyword(), entSealWidth, entSealHeight);
            }
            //根据关键字计算所有个人签署位置
            if(request.getPersonalKeyword() != null && request.getPersonalKeyword().length() > 0){
                personalPositionList = calculatePositionService.getAllPositionByKeyWords(signFileBytes,request.getPersonalKeyword(),personalSealWidth,personalSealHeight);
            }
            if((personalPositionList == null || personalPositionList.size() == 0 ) &&
                    (personalPositionList == null || personalPositionList.size() == 0)){
                return Result.error("签署失败！签署关键字在文件中不存在，请准确设置关键字后再签署",null);
            }
        }

        //进行签署操作
        byte[] operationByte = signFileBytes ;
        try {
            //所有企业位置签署
            if(entPositionList.size() > 0){
                for(RealPositionProperty realPositionProperty : entPositionList){
                    operationByte = signService.signingContract(operationByte, entSealBytes, entCert, realPositionProperty);
                }
            }
            //所有个人位置签署
            if(personalPositionList.size() > 0){
                for(RealPositionProperty realPositionProperty : personalPositionList){
                    operationByte = signService.signingContract(operationByte, personalBytes, personalCert, realPositionProperty);
                }
            }
        }catch (Exception e){

        }
        if(operationByte == null){
            return Result.error("签署失败",null);
        }

//        try {
//            org.apache.commons.io.IOUtils.write(operationByte,new FileOutputStream(new File("/Users/gongfenglai/Desktop/test/pdf/" + System.currentTimeMillis() + ".pdf")));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        String encode = Base64.encode(operationByte);
        SignResponse response = new SignResponse();
        response.setSignFile(encode);
        return Result.OK(response);

    }

    @ApiOperation("文件验签")
    @RequestMapping(value = "/verify",method = RequestMethod.POST)
    public Result<VerifyResponse> verify(@RequestBody VerifyRequest request){

        String verifyFile = request.getVerifyFile();
        byte[] decode = Base64.decode(verifyFile);

        VerifyResponse imageFromPdf = signVerifyService.getImageFromPdf(decode, request.getFileName());
        return Result.OK(imageFromPdf) ;
    }


    public List<SourcePositionProperty> convert(List<PositionRequest> positionRequestList){
        List<SourcePositionProperty> list = new ArrayList<>();
        for(PositionRequest request : positionRequestList){
            SourcePositionProperty position = new SourcePositionProperty();
            position.setOffsetX(Float.valueOf(request.getOffsetX()));
            position.setOffsetY(Float.valueOf(request.getOffsetY()));
            position.setPage(request.getPage());
            position.setWidth(Float.valueOf(request.getWidth()));
            position.setHeight(Float.valueOf(request.getHeight()));
            position.setPageHeight(Float.valueOf(request.getPageHeight()));
            position.setPageWidth(Float.valueOf(request.getPageWidth()));
            list.add(position);
        }
        return list ;
    }


    public byte [] getResourceFiles(String path) {
        try {
            InputStream inputStream = ResourceUtils.class.getClassLoader()
                    .getResourceAsStream(path);
            return read(inputStream);
        }catch (Exception e){
            System.err.println(path);
            e.printStackTrace();
        }
        return null;
    }


    public byte[] read(InputStream inputStream) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int num = inputStream.read(buffer);
            while (num != -1) {
                baos.write(buffer, 0, num);
                num = inputStream.read(buffer);
            }
            baos.flush();
            return baos.toByteArray();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

}
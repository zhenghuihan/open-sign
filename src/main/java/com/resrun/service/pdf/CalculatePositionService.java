package com.resrun.service.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfReader;
import com.resrun.service.pojo.RealPositionProperty;
import com.resrun.service.pojo.SelectKeywords;
import com.resrun.service.pojo.SourcePositionProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 计算签署位置业务
 * @Package: com.resrun.service.pdf
 * @ClassName: CalculatePositionService
 * @copyright 北京资源律动科技有限公司
 */
@Service
public class CalculatePositionService {

    /**
     * @Description #批量计算真实签署位置
     * @Param [sourcePositionProperties]
     * @return java.util.List<com.resrun.modules.sign.service.tool.pojo.RealPositionProperty>
     **/
    public List<RealPositionProperty> calculatePositions(List<SourcePositionProperty> sourcePositionProperties, byte[] pdfFileByte){
        List<RealPositionProperty> realPositionProperties = new ArrayList<>();


        PdfReader reader = null ;
        try {
            //将pdf文件读入PdfReader工具类
            reader = new PdfReader(pdfFileByte);
            for(SourcePositionProperty sourcePositionProperty : sourcePositionProperties){
                RealPositionProperty realPositionProperty = calculatePosition(sourcePositionProperty,pdfFileByte);
                Document document = new Document(reader.getPageSize(sourcePositionProperty.getPage()));
                //获取真实pdf文件指定页的真实文档宽高
                float realPdfHeight = document.getPageSize().getHeight();
                float realPdfWidth = document.getPageSize().getWidth();
                //获取页面上文档的宽高
                float sourcePageWidth = sourcePositionProperty.getPageWidth();
                float sourcePageHeight = sourcePositionProperty.getPageHeight();
                //计算真实文档的宽高和页面文档的宽高的比率
                float rateHeight = realPdfHeight / sourcePageHeight;
                float rateWidth = realPdfWidth / sourcePageWidth;
                //计算页面上的横纵坐标,由于页面上给出的是左上角的坐标，所以需要再转换计算一下
                //左下角
                float pageStartX = sourcePositionProperty.getOffsetX();
                float pageStartY = sourcePositionProperty.getOffsetY() + sourcePositionProperty.getHeight() ;
                //右上角
                float pageEndX = sourcePositionProperty.getOffsetX() + sourcePositionProperty.getWidth();
                float pageEndY = sourcePositionProperty.getOffsetY();
                //根据比率去计算真实文档上的坐标位置
                float startX = pageStartX * rateWidth ;
                float startY = pageStartY * rateHeight;
                float endX = pageEndX * rateWidth ;
                float endY = pageEndY * rateHeight ;
                //由于页面的纵坐标和pdf的纵坐标是相反的，所以真实的pdf的纵坐标在计算的时候需要再反转一下
                startY = realPdfHeight - startY ;
                endY = realPdfHeight - endY ;
                //封装返回数据
                realPositionProperty.setStartx(startX);
                realPositionProperty.setStarty(startY);
                realPositionProperty.setEndx(endX);
                realPositionProperty.setEndy(endY);
                realPositionProperty.setPageNum(sourcePositionProperty.getPage());
                document.close();
                realPositionProperties.add(realPositionProperty);
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realPositionProperties ;
    }



    /**
     * @Description #单独计算真实签署位置
     * @Param [sourcePositionProperty]
     * @return com.resrun.modules.sign.service.tool.pojo.RealPositionProperty
     **/
    public RealPositionProperty calculatePosition(SourcePositionProperty sourcePositionProperty, byte[] pdfFileByte){
        RealPositionProperty realPositionProperty = new RealPositionProperty();
        PdfReader reader = null ;
        Document document = null ;
        try {
            //将pdf文件读入PdfReader工具类
            reader = new PdfReader(pdfFileByte);
            document = new Document(reader.getPageSize(sourcePositionProperty.getPage()));
            //获取真实pdf文件指定页的真实文档宽高
            float realPdfHeight = document.getPageSize().getHeight();
            float realPdfWidth = document.getPageSize().getWidth();
            //获取页面上文档的宽高
            float sourcePageWidth = sourcePositionProperty.getPageWidth();
            float sourcePageHeight = sourcePositionProperty.getPageHeight();
            //计算真实文档的宽高和页面文档的宽高的比率
            float rateHeight = realPdfHeight / sourcePageHeight;
            float rateWidth = realPdfWidth / sourcePageWidth;
            //计算页面上的横纵坐标,由于页面上给出的是左上角的坐标，所以需要再转换计算一下
            //左下角
            float pageStartX = sourcePositionProperty.getOffsetX();
            float pageStartY = sourcePositionProperty.getOffsetY() + sourcePositionProperty.getHeight() ;
            //右上角
            float pageEndX = sourcePositionProperty.getOffsetX() + sourcePositionProperty.getWidth();
            float pageEndY = sourcePositionProperty.getOffsetY();
            //根据比率去计算真实文档上的坐标位置
            float startX = pageStartX * rateWidth ;
            float startY = pageStartY * rateHeight;
            float endX = pageEndX * rateWidth ;
            float endY = pageEndY * rateHeight ;
            //由于页面的纵坐标和pdf的纵坐标是相反的，所以真实的pdf的纵坐标在计算的时候需要再反转一下
            startY = realPdfHeight - startY ;
            endY = realPdfHeight - endY ;
            //封装返回数据
            realPositionProperty.setStartx(startX);
            realPositionProperty.setStarty(startY);
            realPositionProperty.setEndx(endX);
            realPositionProperty.setEndy(endY);
            realPositionProperty.setPageNum(sourcePositionProperty.getPage());

            document.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realPositionProperty ;
    }


    public RealPositionProperty calculatePosition(SourcePositionProperty sourcePositionProperty){
        RealPositionProperty realPositionProperty = new RealPositionProperty();
        //获取真实pdf文件指定页的真实文档宽高
        float realPdfHeight = sourcePositionProperty.getRealHeight();
        float realPdfWidth = sourcePositionProperty.getRealWidth();
        //获取页面上文档的宽高
        float sourcePageWidth = sourcePositionProperty.getPageWidth();
        float sourcePageHeight = sourcePositionProperty.getPageHeight();
        //计算真实文档的宽高和页面文档的宽高的比率
        float rateHeight = realPdfHeight / sourcePageHeight;
        float rateWidth = realPdfWidth / sourcePageWidth;
        //计算页面上的横纵坐标,由于页面上给出的是左上角的坐标，所以需要再转换计算一下
        //左下角
        float pageStartX = sourcePositionProperty.getOffsetX();
        float pageStartY = sourcePositionProperty.getOffsetY() + sourcePositionProperty.getHeight() ;
        //右上角
        float pageEndX = sourcePositionProperty.getOffsetX() + sourcePositionProperty.getWidth();
        float pageEndY = sourcePositionProperty.getOffsetY();
        //根据比率去计算真实文档上的坐标位置
        float startX = pageStartX * rateWidth ;
        float startY = pageStartY * rateHeight;
        float endX = pageEndX * rateWidth ;
        float endY = pageEndY * rateHeight ;
        //由于页面的纵坐标和pdf的纵坐标是相反的，所以真实的pdf的纵坐标在计算的时候需要再反转一下
        startY = realPdfHeight - startY ;
        endY = realPdfHeight - endY ;
        //封装返回数据
        realPositionProperty.setStartx(startX);
        realPositionProperty.setStarty(startY);
        realPositionProperty.setEndx(endX);
        realPositionProperty.setEndy(endY);
        realPositionProperty.setPageNum(sourcePositionProperty.getPage());
        return realPositionProperty ;
    }




    /**
     * 通过查询关键字来获得签名位置信息
     * @param pdfFile 签署源文件
     * @param keyWords 关键字
     * @param width 签章宽度
     * @param height 签章高度
     * @return 签署位置信息
     * @throws IOException
     */
    public RealPositionProperty getPositionByKeyWords(byte[] pdfFile, String keyWords, int width, int height) {
        RealPositionProperty positionProperty = new RealPositionProperty();
        //调用通过关键字查询位置的方法
        float[] result = new float[0];
        try {
            result = new SelectKeywords().selectKeyword(pdfFile,keyWords);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(result !=null){

            positionProperty.setStartx(result[0]);
            positionProperty.setStarty(result[1]+height/4);
            positionProperty.setPageNum((int)result[2]);
            positionProperty.setEndx(result[0]+width/2);
            positionProperty.setEndy(result[1]-height/4);

        }
        return positionProperty;
    }

    /**
     * 通过查询关键字来获得签名位置信息<br/>
     *
     * 同一个关键字出现在多处会一次性全部找出
     *
     * @param pdfFile 签署源文件
     * @param keyWords 关键字
     * @param width 签章宽度
     * @param height 签章高度
     * @return 签署位置信息
     * @throws IOException
     */
    public List<RealPositionProperty> getAllPositionByKeyWords(byte[] pdfFile,String keyWords,int width,int height) {
        List<RealPositionProperty> positions = new ArrayList<RealPositionProperty>();
        //调用通过关键字查询位置的方法
        List<float[]> results = null;
        try {
            results = new SelectKeywords().selectAllKeyword(pdfFile, keyWords);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(results !=null && results.size()>0){
            for (float[] result : results) {
                RealPositionProperty positionProperty = new RealPositionProperty();

                positionProperty.setStartx(result[0]);
                positionProperty.setStarty(result[1]+height/4);
                positionProperty.setPageNum((int)result[2]);
                positionProperty.setEndx(result[0]+width/2);
                positionProperty.setEndy(result[1]-height/4);


                positions.add(positionProperty);
            }
        }
        return positions;
    }


}
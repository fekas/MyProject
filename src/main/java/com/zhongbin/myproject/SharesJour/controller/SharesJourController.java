package com.zhongbin.myproject.SharesJour.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.util.WorkBookUtil;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteWorkbook;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongbin.myproject.SharesJour.entity.SharesJour;
import com.zhongbin.myproject.SharesJour.service.ISharesJourService;
import com.zhongbin.myproject.constant.ExcelConstant;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/shares")
@AllArgsConstructor
@Slf4j
public class SharesJourController {

    ISharesJourService sharesJourService;

    @GetMapping("/addBatch")
    public Boolean addBatch(){
        sharesJourService.addBatch();
        return true;
    }


    @GetMapping("/list")
    public List<SharesJour> list(){
        List<SharesJour> list = sharesJourService.queryAll();
        return list;
    }

    /**
     * 数据量大不可用，OOM
     * @param response
     * @throws IOException
     */
    @GetMapping("/excel/download")
    public void download(HttpServletResponse response) throws IOException {
        List<SharesJour> list = sharesJourService.queryAll();

        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + "excelDemo" + ".xlsx");
        EasyExcel.write(response.getOutputStream(), SharesJour.class).sheet("模板").doWrite(list);
    }

    /**
     * 分批读取数据/分sheet页导出
     * @param response
     * @throws IOException
     */
    @GetMapping("/excel/download1")
    public void download1(HttpServletResponse response) throws IOException {
        Long count = sharesJourService.count(new QueryWrapper<>());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + "excelDemo1" + ".xlsx");

        //计算sheet页数量
        int sheetNums = (int)(count / ExcelConstant.PER_SHEET_ROW_COUNT);
        //计算每个sheet循环次数
        int cycleCount = ExcelConstant.PER_SHEET_ROW_COUNT / ExcelConstant.PER_WRITE_ROW_COUNT;
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();

        for(int i = 0; i < sheetNums; i++){
            WriteSheet sheet = new WriteSheet();
            sheet.setSheetName("sheet" + i);
            sheet.setSheetNo(i);
            sheet.setClazz(SharesJour.class);
            for(int j = 0; j < cycleCount; j++){
                Page<SharesJour> page = new Page<>((long) i * cycleCount + (long) j,
                        ExcelConstant.PER_WRITE_ROW_COUNT);
                //读取数据
                List<SharesJour> list = sharesJourService.query(page);
                excelWriter.write(list, sheet);
            }
            log.info("第" + i + "个sheet页数据导出完成");
        }
        excelWriter.finish();
    }

    /**
     * 多线程导出
     *
     * easyExcel使用excelWriter的write方法写入数据在多线程情况下会报错，主要原因是调用createSheet方法时回取到相同的sheetNumber
     */
    @GetMapping("/excel/download2")
    public void download2(HttpServletResponse response) throws Exception {
        //计算导出条数
        Long count = sharesJourService.count(new QueryWrapper<SharesJour>().lt("position_str", ExcelConstant.PER_SHEET_ROW_COUNT * 10));

        //计算sheet页数量
        int sheetNums = (int)(count / ExcelConstant.PER_SHEET_ROW_COUNT);
        //计算每个sheet循环次数
        int cycleCount = ExcelConstant.PER_SHEET_ROW_COUNT / ExcelConstant.PER_WRITE_ROW_COUNT;
        CountDownLatch latch = new CountDownLatch(sheetNums);
        long start = System.currentTimeMillis();

        ThreadPoolExecutor executor = new ThreadPoolExecutor
                (16, 32, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(32), new ThreadPoolExecutor.AbortPolicy());

        try(SXSSFWorkbook workbook = new SXSSFWorkbook(200)){
            for(int i = 0; i < sheetNums; i++){
                int finalI = i;
                SXSSFSheet sheet = workbook.createSheet("sheet-" + i);

                executor.execute(() ->{
                    Page<SharesJour> page = new Page<>(finalI, ExcelConstant.PER_SHEET_ROW_COUNT);
                    log.info("========线程{}已执行，当前sheet为{}===========", Thread.currentThread(), sheet.getSheetName());
                    List<SharesJour> query = sharesJourService.query(page);
                    for (int i1 = 0; i1 < query.size(); i1++) {
                        SXSSFRow row = sheet.createRow(i1 + 1);
                        Field[] fields = SharesJour.class.getDeclaredFields();
                        for (int i2 = 0; i2 < fields.length; i2++) {
                            fields[i2].setAccessible(true);
                            SXSSFCell cell = row.createCell(i2);
                            try {
                                if(fields[i2].getType().equals(Date.class)){
                                    cell.setCellValue((Date) fields[i2].get(query.get(i1)));
                                }else if(fields[i2].getType().equals(BigDecimal.class)){
                                    cell.setCellValue(fields[i2].get(query.get(i1)).toString());
                                } else {
                                    cell.setCellValue((String) fields[i2].get(query.get(i1)));
                                }
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    latch.countDown();
                });
            }
            latch.await();
            workbook.write(response.getOutputStream());
            workbook.dispose();
        }catch (Exception e){
            throw new Exception();
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + "excelDemo2" + ".xlsx");


        long end = System.currentTimeMillis();
        log.info("导出{}条数据花费时间为{}", count, end - start);
    }
}

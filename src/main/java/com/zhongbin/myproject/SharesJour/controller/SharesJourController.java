package com.zhongbin.myproject.SharesJour.controller;

import com.alibaba.excel.EasyExcel;
import com.zhongbin.myproject.SharesJour.entity.SharesJour;
import com.zhongbin.myproject.SharesJour.service.ISharesJourService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/shares")
@AllArgsConstructor
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


}

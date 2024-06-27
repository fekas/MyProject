package com.zhongbin.myproject.SharesJour.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("demo.shares_jour")
public class SharesJour {

    @ExcelProperty("定位串")
    @TableId
    private String positionStr;

    @ExcelProperty("交易时间")
    private Date tradeDate;

    @ExcelProperty("销售商")
    private String agencyNo;

    @ExcelProperty("产品代码")
    private String stockCode;

    @ExcelProperty("发生份额")
    private BigDecimal occurShares;

    @ExcelProperty("发生金额")
    private BigDecimal occurBalance;

    @ExcelProperty("剩余份额")
    private BigDecimal realShares;

}

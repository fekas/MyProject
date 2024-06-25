package com.zhongbin.myproject.SharesJour.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhongbin.myproject.SharesJour.entity.SharesJour;
import com.zhongbin.myproject.SharesJour.mapper.SharesJourMapper;
import com.zhongbin.myproject.SharesJour.service.ISharesJourService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SharesJourServiceImpl implements ISharesJourService {

    SharesJourMapper mapper;

    @Override
    public List<SharesJour> queryAll() {
        List<SharesJour> sharesJours = mapper.selectList(new QueryWrapper<>());
        return sharesJours;
    }

    @Override
    public void addBatch() {
        Random random = new Random();
        for(int i = 0; i < 30000000; i++){
            SharesJour sharesJour = new SharesJour();
            sharesJour.setPositionStr(String.valueOf(i));
            sharesJour.setAgencyNo(String.valueOf(random.nextInt(900) + 100));
            sharesJour.setCurrentDate(DateUtil.offsetDay(DateUtil.date(), random.nextInt(1000)));
            sharesJour.setStockCode(UUID.randomUUID().toString().substring(0, 5).toUpperCase());
            sharesJour.setOccurShares(new BigDecimal(random.nextDouble()));
            sharesJour.setOccurBalance(new BigDecimal(random.nextDouble()));
            sharesJour.setRealShares(new BigDecimal(random.nextDouble()));

            mapper.insert(sharesJour);
        }
    }
}

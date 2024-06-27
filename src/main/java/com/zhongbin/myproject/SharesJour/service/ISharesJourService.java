package com.zhongbin.myproject.SharesJour.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongbin.myproject.SharesJour.entity.SharesJour;

import java.util.List;

public interface ISharesJourService {
    List<SharesJour> queryAll();

    void addBatch();

    Long count(QueryWrapper<SharesJour> sharesJourQueryWrapper);

    List<SharesJour> query(Page<SharesJour> page);
}

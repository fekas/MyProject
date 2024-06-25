package com.zhongbin.myproject.SharesJour.service;

import com.zhongbin.myproject.SharesJour.entity.SharesJour;

import java.util.List;

public interface ISharesJourService {
    List<SharesJour> queryAll();

    void addBatch();
}

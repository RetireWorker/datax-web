package com.wugui.datax.admin.service;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.wugui.datatx.core.biz.model.ReturnT;
import com.wugui.datax.admin.dto.DataXBatchJsonBuildDto;
import com.wugui.datax.admin.entity.JobInfo;

/**
 * core job action for datax-web
 *
 * @author xuxueli 2016-5-28 15:30:33
 */
public interface JobService {

    /**
     * page list
     *
     * @param start
     * @param length
     * @param jobGroup
     * @param jobDesc
     * @param glueType
     * @param userId
     * @return
     */
    Map<String, Object> pageList(int start, int length, int jobGroup, int triggerStatus, String jobDesc, String glueType, int userId,Integer[] projectIds);

    List<JobInfo> list();

    /**
     * add job
     *
     * @param jobInfo
     * @return
     */
    ReturnT<String> add(JobInfo jobInfo);

    /**
     * update job
     *
     * @param jobInfo
     * @return
     */
    ReturnT<String> update(JobInfo jobInfo);

    /**
     * remove job
     * *
     *
     * @param id
     * @return
     */
    ReturnT<String> remove(int id);

    /**
     * start job
     *
     * @param id
     * @return
     */
    ReturnT<String> start(int id);

    /**
     * stop job
     *
     * @param id
     * @return
     */
    ReturnT<String> stop(int id);

    /**
     * dashboard info
     *
     * @return
     */
    Map<String, Object> dashboardInfo();

    /**
     * chart info
     *
     * @return
     */
    ReturnT<Map<String, Object>> chartInfo();

    /**
     * batch add
     * @param dto
     * @return
     */
    ReturnT<String> batchAdd(DataXBatchJsonBuildDto dto) throws IOException;

    /**
     * 批量启动任务
     * @param ids 任务ID列表
     * @return
     */
    ReturnT<String> batchStart(List<Integer> ids);

    /**
     * 批量删除任务
     * @param ids 任务ID列表
     * @return
     */
    ReturnT<String> batchRemove(List<Integer> ids);
}

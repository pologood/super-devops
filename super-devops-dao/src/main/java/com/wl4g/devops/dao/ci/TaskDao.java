package com.wl4g.devops.dao.ci;

import com.wl4g.devops.common.bean.ci.Task;
import com.wl4g.devops.common.bean.scm.CustomPage;

import java.util.List;

public interface TaskDao {
    int deleteByPrimaryKey(Integer id);

    int insert(Task record);

    int insertSelective(Task record);

    Task selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Task record);

    int updateByPrimaryKeyWithBLOBs(Task record);

    int updateByPrimaryKey(Task record);

    List<Task> list(CustomPage customPage);
}
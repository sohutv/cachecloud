package com.sohu.cache.dao;

import com.sohu.cache.entity.AppClientLatencyCommand;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by rucao on 2019/12/16
 */
@Repository
public interface AppClientLatencyCommandDao {
    int batchSave(List<AppClientLatencyCommand> appClientLatencyCommandList);

    List<Map<String, Object>> getLatencyCommandByIds(@Param("ids") List<Long> ids);
}

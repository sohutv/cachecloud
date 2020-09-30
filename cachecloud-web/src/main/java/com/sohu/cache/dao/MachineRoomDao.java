package com.sohu.cache.dao;

import com.sohu.cache.entity.MachineRoom;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by chenshi on 2018/10/16.
 */
public interface MachineRoomDao {

    @Select("select * from machine_room where status=1")
    public List<MachineRoom> getEffectiveRoom();

    @Select("select * from machine_room")
    public List<MachineRoom> getAllRoom();
}

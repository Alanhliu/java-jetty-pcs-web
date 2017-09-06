package com.siasun.pcsweb.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.siasun.pcsweb.beans.EmsInfo;

public interface EmsMapper {

	/**
	 * 查询多条EMS信息
	 * 
	 * @return
	 * @throws Exception
	 */
	List<EmsInfo> ListEms(Map<String, String> map) throws Exception;

	/**
	 * 查询日期内多条EMS信息
	 * 
	 * @return
	 * @throws Exception
	 */
	List<EmsInfo> ListEmsByDate(String date) throws Exception;

	/**
	 * 更新ems信息
	 * 
	 * @return
	 * @throws Exception
	 */
	void updateEmsInfo(EmsInfo info) throws SQLException;

}
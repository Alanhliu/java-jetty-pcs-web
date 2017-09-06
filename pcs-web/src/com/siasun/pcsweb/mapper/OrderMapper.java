package com.siasun.pcsweb.mapper;

import java.util.List;
import java.util.Map;

import com.siasun.pcsweb.beans.OrderInfo;

public interface OrderMapper {

	/**
	 * 查询多条订单信息
	 * 
	 * @return
	 * @throws Exception
	 */
	List<OrderInfo> ListOrder(Map<String, String> map) throws Exception;

}
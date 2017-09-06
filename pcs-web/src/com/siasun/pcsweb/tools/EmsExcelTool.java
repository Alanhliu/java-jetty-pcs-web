package com.siasun.pcsweb.tools;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siasun.pcsweb.base.Constants;
import com.siasun.pcsweb.beans.EmsInfo;
import com.siasun.pcsweb.mapper.EmsMapper;

import jxl.Sheet;
import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class EmsExcelTool {

	private final static Logger logger = LoggerFactory.getLogger(EmsExcelTool.class.getName());
	private final static String FILE_PATH = Constants.USER_PATH + "/temp/";
	private final static String EmsProp = Constants.EmsProp;
	private final static String EmsCity = Constants.EmsCity;
	private final static String EmsDefaultweight = Constants.EmsDefaultweight;

	public static String generateEmsImportExcel(String date) {

		if (StringUtils.isEmpty(date)) {
			logger.error("Request date is EMPTY, invalid.");
			return null;
		}

		List<EmsInfo> list = null;
		SqlSession session = DBTools.getSession();
		EmsMapper emsMapper = session.getMapper(EmsMapper.class);

		try {
			list = emsMapper.ListEmsByDate(date);
			session.commit();
		} catch (Exception e) {
			logger.error("Sql Exception.", e);
			return null;
		} finally {
			session.close();
		}

		if (list.isEmpty()) {
			logger.warn("No ems data [{}] !!!", date);
			return null;
		}

		String fileName = String.format("MailInfo%s.xls", date);
		String excelName = FILE_PATH + fileName;

		WritableWorkbook book = null;
		try {
			File excelFile = new File(excelName);
			// 如果文件存在就删除它
			if (excelFile.exists()) {
				excelFile.delete();
			}
			// 打开文件
			book = Workbook.createWorkbook(excelFile);
			// 参数0表示这是第一页
			WritableSheet sheet = book.createSheet(fileName, 0);
			// 合并单元格
//			sheet.mergeCells(5, 5, 6, 6);
			// 文字样式
			jxl.write.WritableFont wfc = new jxl.write.WritableFont(WritableFont.TAHOMA, 8, WritableFont.NO_BOLD, false,
					UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);
			jxl.write.WritableCellFormat wcfFC = new jxl.write.WritableCellFormat(wfc);

			// 设置单元格样式
			wcfFC.setBackground(jxl.format.Colour.GRAY_25);// 单元格颜色
			wcfFC.setAlignment(jxl.format.Alignment.LEFT);// 单元格左对齐
			wcfFC.setVerticalAlignment(jxl.format.VerticalAlignment.TOP);// 顶端对齐
			wcfFC.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);// 细边框

			// 在Label对象的构造子中指名单元格位置是第一列第一行(0,0)
			// 以及单元格内容为
			sheet.addCell(new Label(0, 0, "邮件号*", wcfFC));
			sheet.addCell(new Label(1, 0, "内件号", wcfFC));
			sheet.addCell(new Label(2, 0, "内件名称", wcfFC));
			sheet.addCell(new Label(3, 0, "内件性质*", wcfFC));
			sheet.addCell(new Label(4, 0, "重量*", wcfFC));
			sheet.addCell(new Label(5, 0, "保险金额", wcfFC));
			sheet.addCell(new Label(6, 0, "保价金额", wcfFC));
			sheet.addCell(new Label(7, 0, "收件人城市*", wcfFC));
			sheet.addCell(new Label(8, 0, "收件人邮编*", wcfFC));
			sheet.addCell(new Label(9, 0, "收件人单位", wcfFC));
			sheet.addCell(new Label(10, 0, "收件人姓名", wcfFC));
			sheet.addCell(new Label(11, 0, "收件人街道", wcfFC));
			sheet.addCell(new Label(12, 0, "收件人电话1", wcfFC));
			sheet.addCell(new Label(13, 0, "收件人电话2", wcfFC));
			sheet.addCell(new Label(14, 0, "寄件人姓名", wcfFC));
			sheet.addCell(new Label(15, 0, "寄件人单位", wcfFC));
			sheet.addCell(new Label(16, 0, "寄件人邮编", wcfFC));
			sheet.addCell(new Label(17, 0, "寄件人电话1", wcfFC));
			sheet.addCell(new Label(18, 0, "寄件人电话2", wcfFC));
			sheet.addCell(new Label(19, 0, "寄件人街道", wcfFC));
			sheet.addCell(new Label(20, 0, "其他费", wcfFC));
			sheet.addCell(new Label(21, 0, "体积长", wcfFC));
			sheet.addCell(new Label(22, 0, "体积宽", wcfFC));
			sheet.addCell(new Label(23, 0, "体积高", wcfFC));
			sheet.addCell(new Label(24, 0, "返单邮件号", wcfFC));
			sheet.addCell(new Label(25, 0, "内件数", wcfFC));
			sheet.addCell(new Label(26, 0, "ETA时间", wcfFC));
			sheet.addCell(new Label(27, 0, "运输方式", wcfFC));
			sheet.addCell(new Label(28, 0, "带换货标识", wcfFC));

			jxl.write.WritableCellFormat wcfFC2 = new jxl.write.WritableCellFormat(wfc);

			// 设置单元格样式
			wcfFC2.setAlignment(jxl.format.Alignment.LEFT);// 单元格左对齐
			wcfFC2.setVerticalAlignment(jxl.format.VerticalAlignment.TOP);// 顶端对齐
			wcfFC2.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);// 细边框

			for (int i = 0; i < list.size(); i++) {

				// PcsOrderContactInfo info =
				// JSON.parseObject(list.get(i).getOrder_contact(),
				// PcsOrderContactInfo.class);
				sheet.addCell(new Label(0, i + 1, list.get(i).getMail_id(), wcfFC2));
				sheet.addCell(new Label(1, i + 1, list.get(i).getOrder_sn(), wcfFC2));
				sheet.addCell(new Label(2, i + 1, list.get(i).getOrder_name(), wcfFC2));// info.getRemark(),
																						// wcfFC2));
				sheet.addCell(new Label(3, i + 1, EmsProp, wcfFC2));
				sheet.addCell(new Label(4, i + 1, EmsDefaultweight, wcfFC2));
				sheet.addCell(new Label(5, i + 1, "0", wcfFC2));
				sheet.addCell(new Label(6, i + 1, "0", wcfFC2));
				sheet.addCell(new Label(7, i + 1, EmsCity, wcfFC2));
				sheet.addCell(new Label(8, i + 1, list.get(i).getContact_code(), wcfFC2));
				sheet.addCell(new Label(9, i + 1, list.get(i).getContact_corp(), wcfFC2));
				sheet.addCell(new Label(10, i + 1, list.get(i).getContact_name(), wcfFC2));
				sheet.addCell(new Label(11, i + 1, list.get(i).getContact_addr(), wcfFC2));
				sheet.addCell(new Label(12, i + 1, list.get(i).getContact_mobile1(), wcfFC2));
				sheet.addCell(new Label(13, i + 1, list.get(i).getContact_mobile2(), wcfFC2));
				sheet.addCell(new Label(14, i + 1, list.get(i).getPost_name(), wcfFC2));
				sheet.addCell(new Label(15, i + 1, list.get(i).getPost_corp(), wcfFC2));
				sheet.addCell(new Label(16, i + 1, list.get(i).getPost_code(), wcfFC2));
				sheet.addCell(new Label(17, i + 1, list.get(i).getPost_mobile1(), wcfFC2));
				sheet.addCell(new Label(18, i + 1, list.get(i).getPost_mobile2(), wcfFC2));
				sheet.addCell(new Label(19, i + 1, list.get(i).getPost_addr(), wcfFC2));
				sheet.addCell(new Label(20, i + 1, "0", wcfFC2));
				sheet.addCell(new Label(21, i + 1, "0", wcfFC2));
				sheet.addCell(new Label(22, i + 1, "0", wcfFC2));
				sheet.addCell(new Label(23, i + 1, "0", wcfFC2));
				sheet.addCell(new Label(24, i + 1, "", wcfFC2));
				sheet.addCell(new Label(25, i + 1, "0", wcfFC2));
				sheet.addCell(new Label(26, i + 1, "", wcfFC2));
				sheet.addCell(new Label(27, i + 1, "", wcfFC2));
				sheet.addCell(new Label(28, i + 1, "", wcfFC2));
			}

			// 写入数据并关闭文件
			book.write();
			book.close();
			logger.debug("EMS excel file: {} created.", fileName);

		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
		return excelName;
	}

	public static int analysisEmsImportExcel(String filename) {

		List<EmsInfo> listInfo = new ArrayList<>();

		Workbook workbook = null;
		try {
			workbook = Workbook.getWorkbook(new File(filename));
		} catch (Exception e) {
			logger.error("", e);
			return 0;
		}
		Sheet sheet1 = workbook.getSheet(0);
		int rows = sheet1.getRows();
		logger.debug("File row = {}.", rows);
		for (int i = 1; i < rows; i++) {

			if (StringUtils.isEmpty(sheet1.getCell(0, i).getContents())) {
				continue;
			}
			EmsInfo info = new EmsInfo();
			info.setMail_id(sheet1.getCell(0, i).getContents());
			info.setOrder_sn(sheet1.getCell(1, i).getContents());
			info.setOrder_name(sheet1.getCell(2, i).getContents());
			info.setOrder_prop(sheet1.getCell(3, i).getContents());
			info.setWeight(sheet1.getCell(4, i).getContents());
			info.setInsure_amount(sheet1.getCell(5, i).getContents());
			info.setInsure_value(sheet1.getCell(6, i).getContents());
			info.setContact_city(sheet1.getCell(7, i).getContents());
			info.setContact_code(sheet1.getCell(8, i).getContents());
			info.setContact_corp(sheet1.getCell(9, i).getContents());
			info.setContact_name(sheet1.getCell(10, i).getContents());
			info.setContact_addr(sheet1.getCell(11, i).getContents());
			info.setContact_mobile1(sheet1.getCell(12, i).getContents());
			info.setContact_mobile2(sheet1.getCell(13, i).getContents());
			info.setPost_name(sheet1.getCell(14, i).getContents());
			info.setPost_corp(sheet1.getCell(15, i).getContents());
			info.setPost_code(sheet1.getCell(16, i).getContents());
			info.setPost_mobile1(sheet1.getCell(17, i).getContents());
			info.setPost_mobile2(sheet1.getCell(18, i).getContents());
			info.setPost_addr(sheet1.getCell(19, i).getContents());
			info.setExtra_amount(sheet1.getCell(20, i).getContents());
			info.setLength(sheet1.getCell(21, i).getContents());
			info.setWide(sheet1.getCell(22, i).getContents());
			info.setHeight(sheet1.getCell(23, i).getContents());
			info.setBack_mail_id(sheet1.getCell(24, i).getContents());
			info.setOrder_count(sheet1.getCell(25, i).getContents());
			info.setEva_time(sheet1.getCell(26, i).getContents());
			info.setShipping_type(sheet1.getCell(27, i).getContents());
			info.setReplace_identity(sheet1.getCell(28, i).getContents());
			// info.setMail_state(Constants.MailState.getMailState("MAIL_DELIVERED"));
			// // 状态

			listInfo.add(info);
		}

		if (listInfo.isEmpty()) {
			logger.error("{} is empty: no data.", filename);
			return 0;
		}

		SqlSession session = DBTools.getSession();
		EmsMapper mapper = session.getMapper(EmsMapper.class);

		EmsInfo temp = null;

		try {

			for (int i = 0; i < listInfo.size(); i++) {

				temp = listInfo.get(i);
				// 去掉mail的mac计算
				// try {
				// temp.setMac(calcMailMAC(temp.getOrder_sn(),
				// Constants.MailState.getMailState("MAIL_DELIVERED"))); // mac
				// } catch (Exception e) {
				// logger.error("Check mail mac failed, order_sn:" +
				// temp.getOrder_sn(), e);
				// //continue;
				// return 0;
				// }
				temp.setMail_state("3");
				/**
				 * 发短信
				 * 
				 * */
				if (StringUtils.isNotEmpty(temp.getContact_mobile1()) &&
						StringUtils.isNotEmpty(temp.getOrder_sn()) && StringUtils.isNotEmpty(temp.getMail_id())) {
							
					MsgKit message = new MsgKit(temp.getContact_mobile1(), temp.getOrder_sn(), "", temp.getMail_id(), MsgKit.METHOD_JJ);
					message.run();
				} else {
					logger.warn("mobile:{}, orderSn:{}, MailId:{}, some data empty.", temp.getContact_mobile1(),
							temp.getOrder_sn(), temp.getMail_id());
				}
				mapper.updateEmsInfo(temp);
				session.commit();
			}

		} catch (SQLException e) {
			logger.error("Sql Exception.", e);
			return 0;
		} finally {
			session.close();
		}
		return 1;
	}

	/**
	 * 计算邮寄订单的mac
	 */
	// public static String calcMailMAC(String order_sn, int mail_state) throws
	// Exception {
	//
	// if (mail_state <= Constants.MailState.getMailState("ORDER_INIT")) { //
	// <=0
	// logger.warn("Req invalid mail state[{}].", mail_state);
	// throw new Exception("Invalid requState.");
	// }
	//
	// SqlSession session = DBTools.getSession();
	// PcsEmsMapper mapper = session.getMapper(PcsEmsMapper.class);
	//
	// String old_mac = null;
	// try {
	// old_mac = mapper.getMailMac(order_sn);
	// session.commit();
	// } catch (SQLException e) {
	// logger.error("Sql Exception.", e);
	// } finally {
	// session.close();
	// }
	//
	// if (StringUtils.isEmpty(old_mac)) {
	// logger.error("DB has no pre-step mac, the order not in our system.");
	// throw new Exception("DB has no pre-step mac.");
	// }
	//
	// String calcOldMac = PBOCDES1.pboc_3DESMACHex(order_sn +
	// String.format("%02d", mail_state - 1), order_mac_key);
	// if (!old_mac.equalsIgnoreCase(calcOldMac)) {
	// logger.error("Calc mac:{} is different from db mac:{}.", calcOldMac,
	// old_mac);
	// throw new Exception("Diff mac.");
	// }
	//
	// return PBOCDES1.pboc_3DESMACHex(order_sn + String.format("%02d",
	// mail_state), order_mac_key);
	// }
}
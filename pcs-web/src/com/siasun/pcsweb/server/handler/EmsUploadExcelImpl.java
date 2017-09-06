package com.siasun.pcsweb.server.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siasun.pcsweb.base.Constants;
import com.siasun.pcsweb.mapper.UserMapper;
import com.siasun.pcsweb.tools.DBTools;
import com.siasun.pcsweb.tools.EmsExcelTool;


public class EmsUploadExcelImpl implements UrlHandlerInter {

	private final static Logger logger = LoggerFactory.getLogger(EmsUploadExcelImpl.class.getName());

	@SuppressWarnings("unchecked")
	@Override
	public void UrlHandler(HttpServletRequest request, HttpServletResponse response) {

		String jsoncallback = request.getParameter("jsoncallback");
		HashMap<String, Object> output = new HashMap<String, Object>();
		PrintWriter out = null;
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			out = response.getWriter();
		} catch (Exception e) {
			logger.error("Server getWriter failed.", e);
			out.write("服务器异常.");
			out.flush();
			return;
		}

		String username = request.getParameter("username");
		String token = request.getParameter("token");
		logger.debug("username:{}, token:{}.", username, token);

		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(token)) {
			logger.warn("Request some data is NULL, please check the Req URL.");
			out.write("请求参数为空.");
			out.flush();
			return;
		}

		// check token
		SqlSession sessionToken = DBTools.getSession();
		UserMapper mapperToken = sessionToken.getMapper(UserMapper.class);
		String tokenDb = "";
		try {
			tokenDb = mapperToken.getToken(username);
			sessionToken.commit();
		} catch (Exception e) {
			logger.error("Sql exception.", e);
			out.write("数据库异常.");
			out.flush();
			return;
		} finally {
			sessionToken.close();
		}

		if (!token.equals(tokenDb)) {
			logger.warn("Requ token illegal.");
			out.write("请求TOKEN超时, 请重新登陆.");
			out.flush();
			return;
		}
		
		
		// 得到上传文件的保存目录，将上传的文件存放于WEB-INF目录下，不允许外界直接访问，保证上传文件的安全
		String savePath = Constants.USER_PATH + "/upload";
		// 上传时生成的临时文件保存目录
		String tempPath = Constants.USER_PATH + "/temp";
		File tmpFile = new File(tempPath);
		if (!tmpFile.exists()) {
			// 创建临时目录
			tmpFile.mkdir();
		}

		try {
			// 使用Apache文件上传组件处理文件上传步骤：
			// 1、创建一个DiskFileItemFactory工厂
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// 设置工厂的缓冲区的大小，当上传的文件大小超过缓冲区的大小时，就会生成一个临时文件存放到指定的临时目录当中。
			factory.setSizeThreshold(1024 * 100);// 设置缓冲区的大小为100KB，如果不指定，那么缓冲区的大小默认是10KB
			// 设置上传时生成的临时文件的保存目录
			factory.setRepository(tmpFile);
			// 2、创建一个文件上传解析器
			ServletFileUpload upload = new ServletFileUpload(factory);
			// 监听文件上传进度
			upload.setProgressListener(new ProgressListener() {
				public void update(long pBytesRead, long pContentLength, int arg2) {
					logger.info("文件大小为: {}, 当前已处理: {}.", pContentLength, pBytesRead);
					/**
					 * 文件大小为：14608,当前已处理：4096 文件大小为：14608,当前已处理：7367
					 * 文件大小为：14608,当前已处理：11419 文件大小为：14608,当前已处理：14608
					 */
				}
			});
			// 解决上传文件名的中文乱码
			upload.setHeaderEncoding("UTF-8");
			// 3、判断提交上来的数据是否是上传表单的数据
			if (!ServletFileUpload.isMultipartContent(request)) {
				// 按照传统方式获取数据
				return;
			}

			// 设置上传单个文件的大小的最大值，目前是设置为1024*1024字节，也就是1MB
			upload.setFileSizeMax(1024 * 1024);
			// 设置上传文件总量的最大值，最大值=同时上传的多个文件的大小的最大值的和，目前设置为10MB
			upload.setSizeMax(1024 * 1024 * 10);
			// 4、使用ServletFileUpload解析器解析上传数据，解析结果返回的是一个List<FileItem>集合，每一个FileItem对应一个Form表单的输入项
			List<FileItem> list = upload.parseRequest(request);
			for (FileItem item : list) {
				// 如果fileitem中封装的是普通输入项的数据
				if (item.isFormField()) {
					String name = item.getFieldName();
					// 解决普通输入项的数据的中文乱码问题
					String value = item.getString("UTF-8");
					// value = new String(value.getBytes("iso8859-1"),"UTF-8");
					logger.info("{} = {}", name, value);
				} else {// 如果fileitem中封装的是上传文件,得到上传的文件名称.
					String filename = item.getName();
					logger.info(filename);
					if (filename == null || filename.trim().equals("")) {
						continue;
					}
					// 注意：不同的浏览器提交的文件名是不一样的，有些浏览器提交上来的文件名是带有路径的，如:c:\a\b\1.txt，而有些只是单纯的文件名，如：1.txt
					// 处理获取到的上传文件的文件名的路径部分，只保留文件名部分
					filename = filename.substring(filename.lastIndexOf("\\") + 1);
					// 得到上传文件的扩展名
					String fileExtName = filename.substring(filename.lastIndexOf(".") + 1);
					// 如果需要限制上传的文件类型，那么可以通过文件的扩展名来判断上传的文件类型是否合法
					logger.info("上传的文件的扩展名是: {}.", fileExtName);
					// 获取item中的上传文件的输入流
					InputStream in = item.getInputStream();
					// 得到文件保存的名称
					String saveFilename = makeFileName(filename);
					// 得到文件的保存目录
					String realSavePath = makePath(saveFilename, savePath);
					// 创建一个文件输出流
					
					// 20170822 判断操作系统类型
					boolean isWindow = System.getProperty("os.name").toLowerCase().startsWith("win");// 判断是否是windows系统
					logger.debug("OS isWin:{}.", isWindow);
					FileOutputStream fileOut = null;
					if(isWindow) {
						fileOut = new FileOutputStream(realSavePath + "\\" + saveFilename);
					} else {
						fileOut = new FileOutputStream(realSavePath + "/" + saveFilename);
					}	
					
					// 创建一个缓冲区
					byte buffer[] = new byte[1024];
					// 判断输入流中的数据是否已经读完的标识
					int len = 0;
					// 循环将输入流读入到缓冲区当中，(len=in.read(buffer))>0就表示in里面还有数据
					while ((len = in.read(buffer)) > 0) {
						// 使用FileOutputStream输出流将缓冲区的数据写入到指定的目录(savePath + "\\"
						// + filename)当中
						fileOut.write(buffer, 0, len);
					}
					// 关闭输入流
					in.close();
					// 关闭输出流
					fileOut.close();
					// 删除处理文件上传时生成的临时文件
					// item.delete();

					//20170822
					int result = 0;
					if(isWindow) {
						logger.info("{} 上传成功.", realSavePath + "\\" + saveFilename);
						result = EmsExcelTool.analysisEmsImportExcel(realSavePath + "\\" + saveFilename);
					} else {
						logger.info("{} 上传成功.", realSavePath + "/" + saveFilename);
						result = EmsExcelTool.analysisEmsImportExcel(realSavePath + "/" + saveFilename);
					}	
					
					String message = (1 == result) ? "上传完成." : "数据异常.";
					logger.info(message);
					
					out.write(message);
					out.flush();
					return;
				}
			}

		} catch (FileUploadBase.FileSizeLimitExceededException e) {
			logger.error("", e);
			out.write("单个文件超出最大值.");
			out.flush();
			return;
			
		} catch (FileUploadBase.SizeLimitExceededException e) {
			logger.error("", e);
			out.write("上传文件的总的大小超出限制的最大值.");
			out.flush();
			return;
		} catch (Exception e) {
			logger.error("", e);
			out.write("服务器内部错误.");
			out.flush();
			return;
		}
	}

	/**
	 * @Method: makeFileName
	 * @Description: 生成上传文件的文件名，文件名以：uuid+"_"+文件的原始名称
	 * @Anthor:
	 * @param filename
	 *            文件的原始名称
	 * @return uuid+"_"+文件的原始名称
	 */
	private String makeFileName(String filename) {
		// 2.jpg 为防止文件覆盖的现象发生,要为上传文件产生一个唯一的文件名
		return UUID.randomUUID().toString() + "_" + filename;
	}

	/**
	 * 为防止一个目录下面出现太多文件，要使用hash算法打散存储
	 * 
	 * @Method: makePath
	 * @Description:
	 * @Anthor:
	 *
	 * @param filename
	 *            文件名，要根据文件名生成存储目录
	 * @param savePath
	 *            文件存储路径
	 * @return 新的存储目录
	 */
	private String makePath(String filename, String savePath) {
		// 得到文件名的hashCode的值，得到的就是filename这个字符串对象在内存中的地址
		int hashcode = filename.hashCode();
		int dir1 = hashcode & 0xf; // 0--15
		int dir2 = (hashcode & 0xf0) >> 4; // 0-15
		
		// 构造新的保存目录
		String dir = null;
		boolean isWindow = System.getProperty("os.name").toLowerCase().startsWith("win");// 判断是否是windows系统
		if(isWindow) {
			dir = savePath + "\\" + dir1 + "\\" + dir2;
		} else {
			dir = savePath + "/" + dir1 + "/" + dir2;
		}	
		
		//String dir = savePath + "\\" + dir1 + "\\" + dir2; // upload\2\3
															// upload\3\5
		// File既可以代表文件也可以代表目录
		File file = new File(dir);
		// 如果目录不存在
		if (!file.exists()) {
			// 创建目录
			file.mkdirs();
		}
		return dir;
	}

}
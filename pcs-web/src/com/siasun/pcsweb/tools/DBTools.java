package com.siasun.pcsweb.tools;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBTools {

	private static final Logger logger = LoggerFactory.getLogger(DBTools.class);
	private static SqlSessionFactory sessionFactory;

	static {
		Reader reader = null;
		try {
			// 使用MyBatis提供的Resources类加载mybatis的配置文件
			reader = Resources.getResourceAsReader("mybatis.cfg.xml");
			// 构建sqlSession的工厂
			sessionFactory = new SqlSessionFactoryBuilder().build(reader);
		} catch (IOException e) {
			logger.error("DBTools get resource from xml.", e);
		} finally {
			if (null != reader) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("DBTools close reader.", e);
				}
			}
		}

	}

	// 创建能执行映射文件中sql的sqlSession
	public static SqlSession getSession() {
		return sessionFactory.openSession();
	}

}
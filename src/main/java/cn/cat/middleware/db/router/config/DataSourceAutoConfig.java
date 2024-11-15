package cn.cat.middleware.db.router.config;

import cn.cat.middleware.db.router.DBRouterConfig;
import cn.cat.middleware.db.router.dynamic.DynamicDataSource;
import cn.cat.middleware.db.router.util.PropertyUtil;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
public class DataSourceAutoConfig implements EnvironmentAware {
    // 数据源信息
    private final Map<String, Map<String, Object>> dataSourceMap = new HashMap<>();
    // 分库数
    private int dbCount;
    // 分表数
    private int tbCount;

    @Bean
    public DBRouterConfig dbRouterConfig() {
        return new DBRouterConfig(dbCount, tbCount);
    }

    // 创建数据源 注册到 Spring 容器
    @Bean
    public DataSource dataSource() {
        // 创建数据源
        Map<Object, Object> targetDataSources = new HashMap<>();
        dataSourceMap.forEach((dbInfo, v) -> {
            Map<String, Object> objMap = dataSourceMap.get(dbInfo);
            targetDataSources.put(dbInfo,
                    new DriverManagerDataSource(
                            objMap.get("url").toString(),
                            objMap.get("username").toString(),
                            objMap.get("password").toString()
                    )
            );
        });
        // 设置数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        return dynamicDataSource;
    }

    /**
     * 读取配置文件中的数据源信息
     *
     * @param environment 自定义环境变量
     */
//     示例（yml）：
//      router:
//        jdbc:
//          datasource:
//            dbCount: 2
//            tbCount: 4
//            list: db01,db02
//            db01:
//                 driver-class-name: com.mysql.jdbc.Driver
//                 url: jdbc:mysql://127.0.0.1:3306/cat_01?useUnicode=true
//                 username: root
//                 password: 123456
//            db02:
//                 driver-class-name: com.mysql.jdbc.Driver
//                 url: jdbc:mysql://127.0.0.1:3306/cat_02?useUnicode=true
//                 username: root
//                 password: 123456
    @Override
    public void setEnvironment(Environment environment) {
        String prefix = "router.jdbc.datasource.";

        // 获取分库 分表 数
        dbCount = Integer.parseInt(Objects.requireNonNull(environment.getProperty(prefix + "dbCount")));
        tbCount = Integer.parseInt(Objects.requireNonNull(environment.getProperty(prefix + "tbCount")));

        // 获取分库信息
        String dataSources = environment.getProperty(prefix + "list");
        assert dataSources != null;
        for (String dbInfo : dataSources.split(",")) {
            Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + dbInfo, Map.class);
            dataSourceMap.put(dbInfo, dataSourceProps);
        }
    }
}

package cn.cat.middleware.db.router.dynamic;

import cn.cat.middleware.db.router.DBContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        // 获取路由设置到 ThreadLocal 的结果
        return "db" + DBContextHolder.getDbKey();
    }
}

package cn.cat.middleware.db.router;

public class DBRouterBase {

    private String tbIdx;

    public String getTbIdx() {
        return DBContextHolder.getTbKey();
    }

}


package com.depth.util;

import java.util.HashMap;
import java.util.Map;

public class PageUtil {

    public static Map<String, Object> buildPageResult(int totalCount, int pageNum, int pageSize) {
        Map<String, Object> page = new HashMap<>();
        page.put("total", totalCount);
        page.put("pageNum", pageNum);
        page.put("pageSize", pageSize);
        page.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));
        page.put("hasPrevious", pageNum > 1);
        page.put("hasNext", pageNum < (int) Math.ceil((double) totalCount / pageSize));
        return page;
    }

    public static int getStart(int pageNum, int pageSize) {
        return (pageNum - 1) * pageSize;
    }

    public static boolean isValidPageNum(int pageNum) {
        return pageNum >= 1;
    }

    public static boolean isValidPageSize(int pageSize) {
        return pageSize >= 1 && pageSize <= 100;
    }
}
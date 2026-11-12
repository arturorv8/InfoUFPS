package org.infoufps.domain.shared.pagination;

public record PageRequest(int page, int size) {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 50;

    public PageRequest {
        if (page < 1) {
            page = DEFAULT_PAGE;
        }
        if (size < 1) {
            size = DEFAULT_SIZE;
        } else if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
    }

    public static PageRequest of(Integer page, Integer size) {
        int p = (page != null && page > 0) ? page : DEFAULT_PAGE;
        int s = (size != null && size > 0) ? size : DEFAULT_SIZE;
        return new PageRequest(p, s);
    }

    public static PageRequest first() {
        return new PageRequest(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public int getOffset() {
        return (page - 1) * size;
    }

    public long offset() {
        return (long) (page - 1) * size;
    }
}


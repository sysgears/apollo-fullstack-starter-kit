package com.sysgears.core.pagination;

import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class OffsetPageRequest implements Pageable {
    private final int offset;
    private final int limit;
    private final Sort sort;

    public OffsetPageRequest(int offset, int limit) {
        this(offset, limit, Sort.unsorted());
    }

    public OffsetPageRequest(int offset, int limit, Sort sort) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset index must not be less than zero!");
        }

        if (limit < 1) {
            throw new IllegalArgumentException("Limit must not be less than one!");
        }
        this.offset = offset;
        this.limit = limit;
        this.sort = sort;
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetPageRequest((int) getOffset() + getPageSize(), getPageSize(), getSort());
    }

    public OffsetPageRequest previous() {
        return hasPrevious() ? new OffsetPageRequest((int) getOffset() - getPageSize(), getPageSize(), getSort()) : this;
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @Override
    public Pageable first() {
        return new OffsetPageRequest(0, getPageSize(), getSort());
    }

    @Override
    public boolean hasPrevious() {
        return offset > limit;
    }
}

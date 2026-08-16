package io.poby_house.dto.dashboard;

import lombok.Getter;

import java.util.List;

/**
 * 대시보드 카드 한 개의 내용.
 *
 * 화면에 보일 줄(rows)과 전체 건수(total)를 함께 들고 있다.
 * 전체 건수를 버리면, 밀린 기록을 잡아내려고 만든 화면이 오히려 밀린 것을 감춘다.
 * 5줄만 보여 주고 "그 외 12개" 를 말해 주지 않으면 아무도 나머지를 찾아가지 않는다.
 */
@Getter
public class DashCard<T> {

    private final List<T> rows;
    private final int total;
    private final boolean expanded;

    private DashCard(List<T> rows, int total, boolean expanded) {
        this.rows = rows;
        this.total = total;
        this.expanded = expanded;
    }

    /**
     * 전체를 받아 화면에 보일 만큼만 남긴다.
     * 30줄이 쏟아지면 아무것도 안 읽는다. 잘라내는 것이 이 화면의 기능이다.
     */
    public static <T> DashCard<T> of(List<T> all, int limit, boolean expanded) {
        if (expanded || all.size() <= limit) {
            return new DashCard<>(List.copyOf(all), all.size(), expanded);
        }
        return new DashCard<>(List.copyOf(all.subList(0, limit)), all.size(), false);
    }

    public boolean isEmpty() {
        return total == 0;
    }

    /** 잘렸는가. 화면은 이때만 "더 보기" 를 낸다 */
    public boolean isTruncated() {
        return rows.size() < total;
    }

    public int getHiddenCount() {
        return total - rows.size();
    }
}

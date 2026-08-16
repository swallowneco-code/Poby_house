package io.poby_house.dto.report;

import java.time.LocalDate;
import java.util.List;

/**
 * 리포트 4. 통한 것 / 안 통한 것.
 *
 * 안 통한 것을 숨기지 않는다. 통한 것만 적은 문서는 광고문이고, 학부모는 그것을 안다.
 * 무엇을 시도해서 무엇을 그만두었는지가 이 리포트의 신뢰다.
 *
 * 결과 미확인(result=null)을 진행중(ONGOING)과 합치지 않는다.
 * 미확인은 "아직 확인하지 않았다", 진행중은 "확인했고 지켜보는 중" 이다.
 * 합치면 "결과를 적어야 리포트에 나간다" 는 안내를 낼 자리가 사라진다.
 */
public record InterventionGroups(
        long total,
        /** 결과는 적었지만 근거(resultNote)가 없는 건수 */
        long evidenceMissing,
        /** 5개 묶음이 늘 들어 있다. 비어 있어도 빼지 않는다. "안 통한 것 0건" 도 정보다 */
        List<Group> groups,
        /** "통함 3 · 일부 1 · 안 통함 2 · 진행중 0 · 결과 미확인 1" */
        String countLine) {

    public record Group(String key, String label, String meaning, List<Item> items) {

        public boolean empty() {
            return items == null || items.isEmpty();
        }

        public int count() {
            return items == null ? 0 : items.size();
        }
    }

    /**
     * @param watchedDays 시도한 날부터 결과를 확인한 날까지. "며칠 지켜봤나" 가 근거가 된다. 모르면 null
     * @param evidenceMissing 결과는 있는데 근거가 비었다. 이대로면 리포트에 쓸 수 없다
     */
    public record Item(LocalDate triedOn, LocalDate reviewedOn, String problem, String action,
                       String resultLabel, String resultNote, boolean evidenceMissing,
                       Long watchedDays, String authorName) {
    }
}

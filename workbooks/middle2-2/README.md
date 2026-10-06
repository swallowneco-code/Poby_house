# 중2-2 단원별 유형 문제지

`output/workbook.pdf` — 문제(45쪽) → 해설(12쪽) → 빠른 정답(2쪽) 순서의 A4 문서.

| 단원 | 문항 수 |
| --- | --- |
| 1. 삼각형의 성질 | 32 |
| 2. 사각형의 성질 | 28 |
| 3. 도형의 닮음 | 27 |
| 4. 평행선 사이의 선분의 길이의 비 | 33 |
| 5. 경우의 수 | 28 |
| 6. 확률 | 28 |

- 각 단원은 탄탄 단원 다지기 → 예제·유제(풀이·답 없이 문제만) → 서술형 연습(주관식으로 변환) 순서로 번호가 이어진다.
- 한 쪽에 4문제(2×2) 배치.

## 다시 만들기

```bash
cd src
python3 build.py                          # ../output/workbook.html
NODE_PATH=$(npm root -g) node render.cjs  # ../output/workbook.pdf (Playwright Chromium, Noto Sans CJK KR 글꼴 필요)
```

문항 데이터와 그림은 `src/u1.py` ~ `src/u6.py` 에, 그림 그리기 도구는 `src/common.py` 에 있다.
